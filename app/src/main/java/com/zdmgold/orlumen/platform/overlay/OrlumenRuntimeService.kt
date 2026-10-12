package com.zdmgold.orlumen.platform.overlay

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.IntentCompat
import com.zdmgold.orlumen.MainActivity
import com.zdmgold.orlumen.OrlumenApplication
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.model.SpectrumFrame
import com.zdmgold.orlumen.core.model.StockPalettes
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.repository.ConfigRepository
import com.zdmgold.orlumen.core.repository.NowPlayingRepository
import com.zdmgold.orlumen.core.repository.PaletteRepository
import com.zdmgold.orlumen.core.repository.resolvePalette
import com.zdmgold.orlumen.designs.visualizer.VisualizerCanvas
import com.zdmgold.orlumen.platform.aod.AodController
import com.zdmgold.orlumen.platform.aod.ScreenStateReceiver
import com.zdmgold.orlumen.platform.audio.AudioPipeline
import com.zdmgold.orlumen.platform.audio.PlaybackCaptureSpectrumSource
import com.zdmgold.orlumen.platform.audio.SpectrumSourceException
import com.zdmgold.orlumen.platform.permissions.PermissionChecker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "OrlumenRuntimeService"
private const val NOTIFICATION_ID = 1001
private const val VISIBILITY_POLL_MS = 1000L

private data class OverlayUiState(val config: VisualizerConfig, val colors: List<Color>, val corners: CornerRadii)

@AndroidEntryPoint
class OrlumenRuntimeService : Service() {
    @Inject lateinit var audioPipeline: AudioPipeline

    @Inject lateinit var playbackCapture: PlaybackCaptureSpectrumSource

    @Inject lateinit var configRepository: ConfigRepository

    @Inject lateinit var preferencesRepository: PreferencesRepository

    @Inject lateinit var paletteRepository: PaletteRepository

    @Inject lateinit var nowPlayingRepository: NowPlayingRepository

    @Inject lateinit var windowManagerHelper: WindowManagerHelper

    @Inject lateinit var foregroundAppDetector: ForegroundAppDetector

    @Inject lateinit var permissionChecker: PermissionChecker

    @Inject lateinit var controller: VisualizerController

    @Inject lateinit var aodController: AodController

    @Inject lateinit var mediaProjectionManager: MediaProjectionManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val frame = MutableStateFlow<SpectrumFrame?>(null)
    private val uiState =
        MutableStateFlow(OverlayUiState(VisualizerConfig.defaults(), emptyList(), CornerRadii.uniform(0f)))
    private var host: OverlayComposeHost? = null
    private var projection: MediaProjection? = null
    private var running = false
    private var corners = CornerRadii.uniform(0f)
    private var screenReceiver: ScreenStateReceiver? = null
    private var appliedKeepScreenOn = false
    private var appliedDim = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when {
            intent?.action == ACTION_STOP -> stopSelf()
            !running -> begin(intent)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        host?.let { overlay ->
            windowManagerHelper.removeOverlay(overlay.view)
            overlay.destroy()
        }
        host = null
        screenReceiver?.unregister(this)
        screenReceiver = null
        projection?.stop()
        projection = null
        playbackCapture.detachProjection()
        controller.reportStopped()
        super.onDestroy()
    }

    private fun begin(intent: Intent?) {
        val resultCode = intent?.getIntExtra(EXTRA_PROJECTION_RESULT_CODE, Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED
        val projectionData = intent?.let {
            IntentCompat.getParcelableExtra(it, EXTRA_PROJECTION_DATA, Intent::class.java)
        }
        val hasConsent = resultCode == Activity.RESULT_OK && projectionData != null
        val aodOnly = intent?.getBooleanExtra(EXTRA_AOD_ONLY, false) == true
        if (!enterForeground(hasConsent, aodOnly)) {
            controller.report(RuntimeState.Failed(RuntimeFailure.START_NOT_ALLOWED))
            stopSelf()
            return
        }
        running = true
        foregroundAppDetector.reset()
        if (hasConsent && projectionData != null) attachProjection(resultCode, projectionData)
        showOverlay()
        registerScreenReceiver()
        startCollectors(aodOnly)
        if (aodOnly) controller.report(RuntimeState.Running(null))
    }

    private fun registerScreenReceiver() {
        val receiver = ScreenStateReceiver(
            object : ScreenStateReceiver.Listener {
                override fun onScreenOff() {
                    scope.launch { aodController.onScreenOff() }
                }
            }
        )
        receiver.register(this)
        screenReceiver = receiver
    }

    private fun enterForeground(withProjection: Boolean, aodOnly: Boolean): Boolean {
        var type = if (aodOnly) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        }
        if (withProjection && !aodOnly) type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        return try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type)
            true
        } catch (denied: SecurityException) {
            Log.w(TAG, "Foreground service type was not permitted", denied)
            false
        } catch (notAllowed: IllegalStateException) {
            Log.w(TAG, "Foreground service start was not allowed", notAllowed)
            false
        }
    }

    private fun attachProjection(resultCode: Int, data: Intent) {
        val granted = mediaProjectionManager.getMediaProjection(resultCode, data) ?: return
        granted.registerCallback(
            object : MediaProjection.Callback() {
                override fun onStop() {
                    playbackCapture.detachProjection()
                    projection = null
                }
            },
            Handler(Looper.getMainLooper())
        )
        projection = granted
        playbackCapture.attachProjection(granted)
    }

    private fun showOverlay() {
        if (!permissionChecker.canDrawOverlays()) {
            controller.report(RuntimeState.Failed(RuntimeFailure.OVERLAY_PERMISSION_MISSING))
            stopSelf()
            return
        }
        corners = windowManagerHelper.measureGeometry().corners
        val overlay = OverlayComposeHost(this)
        overlay.setContent { OverlayContent() }
        val params = windowManagerHelper.overlayParams(keepScreenOn = false, dimBackground = false)
        if (windowManagerHelper.addOverlay(overlay.view, params)) {
            overlay.onShown()
            host = overlay
        } else {
            overlay.destroy()
            controller.report(RuntimeState.Failed(RuntimeFailure.OVERLAY_PERMISSION_MISSING))
            stopSelf()
        }
    }

    @Composable
    private fun OverlayContent() {
        val current by frame.collectAsState()
        val state by uiState.collectAsState()
        VisualizerCanvas(frame = current, config = state.config, colors = state.colors, corners = state.corners)
    }

    private fun startCollectors(aodOnly: Boolean) {
        scope.launch { observeSettings() }
        scope.launch { pollVisibility() }
        if (aodOnly) return
        scope.launch { runPipeline() }
        scope.launch {
            audioPipeline.activeKind.collect { kind ->
                if (kind != null) controller.report(RuntimeState.Running(kind))
            }
        }
    }

    private suspend fun runPipeline() {
        val mode = preferencesRepository.preferences.first().audioSourceMode
        try {
            audioPipeline.frames(mode).collect { frame.value = it }
        } catch (failure: SpectrumSourceException) {
            Log.w(TAG, "No audio source could be started", failure)
            controller.report(RuntimeState.Failed(RuntimeFailure.SOURCE_UNAVAILABLE))
            stopSelf()
        }
    }

    private suspend fun observeSettings() {
        combine(
            configRepository.activeVisualizerConfig,
            preferencesRepository.preferences,
            paletteRepository.palettes,
            nowPlayingRepository.albumPalette
        ) { config, prefs, palettes, album ->
            val palette = resolvePalette(config.paletteId, palettes, album, prefs.autoApplyAlbumColors)
            audioPipeline.updateTuning(config.sensitivity, config.smoothing)
            applyWindowFlags(prefs.keepScreenOn, prefs.dimBackground)
            OverlayUiState(config, palette.colorsArgb.map { Color(it) }, corners)
        }.collect { uiState.value = it }
    }

    private fun applyWindowFlags(keepScreenOn: Boolean, dimBackground: Boolean) {
        val overlay = host ?: return
        if (keepScreenOn == appliedKeepScreenOn && dimBackground == appliedDim) return
        appliedKeepScreenOn = keepScreenOn
        appliedDim = dimBackground
        windowManagerHelper.updateOverlay(overlay.view, windowManagerHelper.overlayParams(keepScreenOn, dimBackground))
    }

    private suspend fun pollVisibility() {
        while (currentCoroutineContext().isActive) {
            val prefs = preferencesRepository.preferences.first()
            val foreground = if (permissionChecker.hasUsageAccess()) {
                foregroundAppDetector.currentForegroundPackage()
            } else {
                null
            }
            val input = VisibilityInput(
                foregroundPackage = foreground,
                category = foreground?.let { foregroundAppDetector.categoryOf(it) } ?: AppCategoryHint.OTHER,
                isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
                ownPackage = packageName
            )
            val rules = VisibilityRules(prefs.hideOverFullscreen, prefs.visualizerAppPackages)
            val visible = OverlayVisibilityPolicy.isVisible(input, rules)
            host?.view?.visibility = if (visible) View.VISIBLE else View.GONE
            delay(VISIBILITY_POLL_MS)
        }
    }

    private fun buildNotification(): android.app.Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this,
            0,
            Intent(this, OrlumenRuntimeService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, OrlumenApplication.RUNTIME_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_orlumen)
            .setContentTitle(getString(R.string.notification_title))
            .setContentIntent(openApp)
            .addAction(0, getString(R.string.notification_stop), stop)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        const val ACTION_STOP = "com.zdmgold.orlumen.action.STOP"
        const val EXTRA_PROJECTION_RESULT_CODE = "projection_result_code"
        const val EXTRA_PROJECTION_DATA = "projection_data"
        const val EXTRA_AOD_ONLY = "aod_only"

        fun startIntent(
            context: Context,
            projectionResultCode: Int?,
            projectionData: Intent?,
            aodOnly: Boolean = false
        ): Intent = Intent(context, OrlumenRuntimeService::class.java).apply {
            putExtra(EXTRA_AOD_ONLY, aodOnly)
            if (projectionResultCode != null && projectionData != null) {
                putExtra(EXTRA_PROJECTION_RESULT_CODE, projectionResultCode)
                putExtra(EXTRA_PROJECTION_DATA, projectionData)
            }
        }
    }
}

package com.zdmgold.orlumen.platform.aod

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.text.format.DateFormat
import android.util.Log
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.core.model.AodHostMode
import com.zdmgold.orlumen.core.model.NowPlaying
import com.zdmgold.orlumen.core.repository.ConfigRepository
import com.zdmgold.orlumen.core.repository.NowPlayingRepository
import com.zdmgold.orlumen.core.repository.PaletteRepository
import com.zdmgold.orlumen.core.repository.resolvePalette
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

private const val TAG = "AodController"
private const val MILLIS_PER_MINUTE = 60_000L
private const val MIN_RELAUNCH_MS = 1500L
private const val MAX_SHIFT_PX = 20

data class AodUiState(
    val config: AodConfig,
    val colors: List<Int>,
    val nowPlaying: NowPlaying,
    val battery: BatteryInfo,
    val timeMillis: Long,
    val elapsedRealtimeMs: Long,
    val use24Hour: Boolean,
    val shift: ShiftOffset
)

private class AodSettings(val config: AodConfig, val colors: List<Int>)

@Singleton
class AodController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val configRepository: ConfigRepository,
    private val paletteRepository: PaletteRepository,
    private val nowPlayingRepository: NowPlayingRepository,
    private val batteryInfoProvider: BatteryInfoProvider
) {
    private val burnInProtection = BurnInProtection(MAX_SHIFT_PX, MAX_SHIFT_PX, MILLIS_PER_MINUTE)
    private val _isActivityActive = MutableStateFlow(false)
    val isActivityActive: StateFlow<Boolean> = _isActivityActive.asStateFlow()

    @Volatile
    private var lastLaunchMillis = 0L

    fun setActivityActive(active: Boolean) {
        _isActivityActive.value = active
    }

    /** Called when the screen turns off. Launches the AOD activity if the user enabled it. */
    suspend fun onScreenOff(nowMillis: Long = System.currentTimeMillis()) {
        val prefs = preferencesRepository.preferences.first()
        val shouldLaunch = prefs.aodEnabled &&
            prefs.aodHostMode == AodHostMode.ACTIVITY &&
            nowMillis - lastLaunchMillis >= MIN_RELAUNCH_MS
        if (!shouldLaunch) return
        lastLaunchMillis = nowMillis
        val intent = Intent(context, AodActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION
        )
        runCatching { context.startActivity(intent) }
            .onFailure { Log.w(TAG, "AOD activity could not be launched", it) }
    }

    fun uiState(): Flow<AodUiState> {
        val settings = combine(
            configRepository.activeAodConfig,
            preferencesRepository.preferences,
            paletteRepository.palettes,
            nowPlayingRepository.albumPalette
        ) { config, prefs, palettes, album ->
            val palette = resolvePalette(config.paletteId, palettes, album, prefs.autoApplyAlbumColors)
            AodSettings(config, palette.colorsArgb)
        }
        return combine(
            settings,
            minuteTicker(),
            nowPlayingRepository.nowPlaying,
            batteryInfoProvider.batteryInfo()
        ) { current, now, playing, battery ->
            AodUiState(
                config = current.config,
                colors = current.colors,
                nowPlaying = playing,
                battery = battery,
                timeMillis = now,
                elapsedRealtimeMs = SystemClock.elapsedRealtime(),
                use24Hour = DateFormat.is24HourFormat(context),
                shift = burnInProtection.offsetAt(now)
            )
        }
    }

    private fun minuteTicker(): Flow<Long> = flow {
        while (currentCoroutineContext().isActive) {
            val now = System.currentTimeMillis()
            emit(now)
            delay(MILLIS_PER_MINUTE - now % MILLIS_PER_MINUTE)
        }
    }
}

package com.zdmgold.orlumen.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.model.AodHostMode
import com.zdmgold.orlumen.core.model.AudioSourceMode
import com.zdmgold.orlumen.core.model.NowPlaying
import com.zdmgold.orlumen.core.repository.NowPlayingRepository
import com.zdmgold.orlumen.platform.aod.AodController
import com.zdmgold.orlumen.platform.audio.AudioPipeline
import com.zdmgold.orlumen.platform.overlay.RuntimeFailure
import com.zdmgold.orlumen.platform.overlay.RuntimeState
import com.zdmgold.orlumen.platform.overlay.VisualizerController
import com.zdmgold.orlumen.platform.permissions.PermissionChecker
import com.zdmgold.orlumen.platform.permissions.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MS = 5_000L
private const val SILENT_PEAK = 0.02f

enum class DiagnosticHint {
    NONE,
    PERMISSIONS_MISSING,
    SOURCE_FAILED,
    NO_SIGNAL_WHILE_PLAYING
}

data class DiagnosticsUiState(
    val runtime: RuntimeState,
    val peak: Float,
    val permissions: PermissionState,
    val nowPlaying: NowPlaying,
    val aodEnabled: Boolean,
    val aodHostMode: AodHostMode,
    val audioSourceMode: AudioSourceMode,
    val aodScreenShowing: Boolean,
    val lastAodLaunchMillis: Long,
    val hint: DiagnosticHint
)

/** Picks the single most useful hint for what the diagnostics are currently showing. */
internal fun diagnosticHint(
    runtime: RuntimeState,
    peak: Float,
    isPlaying: Boolean,
    permissions: PermissionState
): DiagnosticHint = when {
    !requiredPermissionsGranted(permissions) -> DiagnosticHint.PERMISSIONS_MISSING
    runtime is RuntimeState.Failed && runtime.reason == RuntimeFailure.SOURCE_UNAVAILABLE ->
        DiagnosticHint.SOURCE_FAILED
    runtime is RuntimeState.Running && runtime.source != null && isPlaying && peak < SILENT_PEAK ->
        DiagnosticHint.NO_SIGNAL_WHILE_PLAYING
    else -> DiagnosticHint.NONE
}

private class LiveSignals(
    val runtime: RuntimeState,
    val peak: Float,
    val aodScreenShowing: Boolean,
    val lastAodLaunchMillis: Long
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    controller: VisualizerController,
    audioPipeline: AudioPipeline,
    aodController: AodController,
    nowPlayingRepository: NowPlayingRepository,
    preferencesRepository: PreferencesRepository,
    private val permissionChecker: PermissionChecker
) : ViewModel() {
    private val permissions = MutableStateFlow(permissionChecker.snapshot())

    private val live = combine(
        controller.state,
        audioPipeline.latestPeak,
        aodController.isActivityActive,
        aodController.lastLaunchAttempt
    ) { runtime, peak, showing, lastLaunch -> LiveSignals(runtime, peak, showing, lastLaunch) }

    val uiState: StateFlow<DiagnosticsUiState?> = combine(
        live,
        permissions.asStateFlow(),
        nowPlayingRepository.nowPlaying,
        preferencesRepository.preferences
    ) { signals, granted, playing, prefs ->
        DiagnosticsUiState(
            runtime = signals.runtime,
            peak = signals.peak,
            permissions = granted,
            nowPlaying = playing,
            aodEnabled = prefs.aodEnabled,
            aodHostMode = prefs.aodHostMode,
            audioSourceMode = prefs.audioSourceMode,
            aodScreenShowing = signals.aodScreenShowing,
            lastAodLaunchMillis = signals.lastAodLaunchMillis,
            hint = diagnosticHint(signals.runtime, signals.peak, playing.isPlaying, granted)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    fun refreshPermissions() {
        permissions.value = permissionChecker.snapshot()
    }
}

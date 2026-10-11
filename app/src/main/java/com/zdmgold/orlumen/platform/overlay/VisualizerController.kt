package com.zdmgold.orlumen.platform.overlay

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.zdmgold.orlumen.platform.audio.SpectrumSourceKind
import com.zdmgold.orlumen.platform.permissions.PermissionChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "VisualizerController"

enum class RuntimeFailure {
    OVERLAY_PERMISSION_MISSING,
    AUDIO_PERMISSION_MISSING,
    START_NOT_ALLOWED,
    SOURCE_UNAVAILABLE
}

sealed interface RuntimeState {
    data object Idle : RuntimeState

    data object Starting : RuntimeState

    data class Running(val source: SpectrumSourceKind?) : RuntimeState

    data class Failed(val reason: RuntimeFailure) : RuntimeState
}

@Singleton
class VisualizerController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionChecker: PermissionChecker
) {
    private val _state = MutableStateFlow<RuntimeState>(RuntimeState.Idle)
    val state: StateFlow<RuntimeState> = _state.asStateFlow()

    /** Returns null when the service was started, otherwise the reason it could not be. */
    fun start(
        projectionResultCode: Int? = null,
        projectionData: Intent? = null,
        aodOnly: Boolean = false
    ): RuntimeFailure? {
        val missing = when {
            !permissionChecker.canDrawOverlays() -> RuntimeFailure.OVERLAY_PERMISSION_MISSING
            !aodOnly && !permissionChecker.hasRecordAudio() -> RuntimeFailure.AUDIO_PERMISSION_MISSING
            else -> null
        }
        if (missing != null) {
            _state.value = RuntimeState.Failed(missing)
            return missing
        }
        val intent = OrlumenRuntimeService.startIntent(context, projectionResultCode, projectionData, aodOnly)
        return try {
            ContextCompat.startForegroundService(context, intent)
            _state.value = RuntimeState.Starting
            null
        } catch (blocked: IllegalStateException) {
            Log.w(TAG, "Android refused to start the foreground service", blocked)
            _state.value = RuntimeState.Failed(RuntimeFailure.START_NOT_ALLOWED)
            RuntimeFailure.START_NOT_ALLOWED
        }
    }

    fun stop() {
        context.stopService(Intent(context, OrlumenRuntimeService::class.java))
    }

    internal fun report(state: RuntimeState) {
        _state.value = state
    }

    internal fun reportStopped() {
        if (_state.value !is RuntimeState.Failed) _state.value = RuntimeState.Idle
    }
}

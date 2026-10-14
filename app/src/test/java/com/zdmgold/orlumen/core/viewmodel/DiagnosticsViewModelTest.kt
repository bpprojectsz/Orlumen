package com.zdmgold.orlumen.core.viewmodel

import com.zdmgold.orlumen.platform.audio.SpectrumSourceKind
import com.zdmgold.orlumen.platform.overlay.RuntimeFailure
import com.zdmgold.orlumen.platform.overlay.RuntimeState
import com.zdmgold.orlumen.platform.permissions.PermissionState
import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticsViewModelTest {
    private val granted = PermissionState(true, true, true, true, true, true)
    private val running = RuntimeState.Running(SpectrumSourceKind.GLOBAL_MIX)

    @Test
    fun missingRequiredPermissionsComeFirst() {
        val noOverlay = granted.copy(overlay = false)
        assertEquals(
            DiagnosticHint.PERMISSIONS_MISSING,
            diagnosticHint(RuntimeState.Idle, 0f, isPlaying = false, permissions = noOverlay)
        )
        assertEquals(
            DiagnosticHint.PERMISSIONS_MISSING,
            diagnosticHint(running, 0f, isPlaying = true, permissions = granted.copy(recordAudio = false))
        )
    }

    @Test
    fun aFailedSourceSuggestsAppAudioCapture() {
        assertEquals(
            DiagnosticHint.SOURCE_FAILED,
            diagnosticHint(
                RuntimeState.Failed(RuntimeFailure.SOURCE_UNAVAILABLE),
                0f,
                isPlaying = false,
                permissions = granted
            )
        )
    }

    @Test
    fun silenceWhileMusicPlaysIsFlaggedOnlyForAudioSources() {
        assertEquals(DiagnosticHint.NO_SIGNAL_WHILE_PLAYING, diagnosticHint(running, 0.001f, true, granted))
        assertEquals(DiagnosticHint.NONE, diagnosticHint(running, 0.001f, false, granted))
        assertEquals(DiagnosticHint.NONE, diagnosticHint(running, 0.5f, true, granted))
        assertEquals(DiagnosticHint.NONE, diagnosticHint(RuntimeState.Running(null), 0f, true, granted))
    }

    @Test
    fun otherFailuresAndIdleShowNoHint() {
        assertEquals(DiagnosticHint.NONE, diagnosticHint(RuntimeState.Idle, 0f, true, granted))
        assertEquals(
            DiagnosticHint.NONE,
            diagnosticHint(RuntimeState.Failed(RuntimeFailure.START_NOT_ALLOWED), 0f, true, granted)
        )
    }
}

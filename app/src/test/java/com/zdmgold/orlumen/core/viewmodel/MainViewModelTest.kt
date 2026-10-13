package com.zdmgold.orlumen.core.viewmodel

import com.zdmgold.orlumen.platform.audio.SpectrumSourceKind
import com.zdmgold.orlumen.platform.overlay.RuntimeFailure
import com.zdmgold.orlumen.platform.overlay.RuntimeState
import org.junit.Assert.assertEquals
import org.junit.Test

class MainViewModelTest {
    @Test
    fun enablingAodStartsTheRuntimeWhenNothingIsRunning() {
        assertEquals(AodToggleAction.START_AOD_ONLY, aodToggleAction(true, RuntimeState.Idle))
        assertEquals(
            AodToggleAction.START_AOD_ONLY,
            aodToggleAction(true, RuntimeState.Failed(RuntimeFailure.SOURCE_UNAVAILABLE))
        )
    }

    @Test
    fun enablingAodLeavesARunningVisualizerAlone() {
        assertEquals(
            AodToggleAction.NONE,
            aodToggleAction(true, RuntimeState.Running(SpectrumSourceKind.GLOBAL_MIX))
        )
        assertEquals(AodToggleAction.NONE, aodToggleAction(true, RuntimeState.Starting))
    }

    @Test
    fun disablingAodStopsOnlyAnAodOnlyRuntime() {
        assertEquals(AodToggleAction.STOP, aodToggleAction(false, RuntimeState.Running(null)))
        assertEquals(
            AodToggleAction.NONE,
            aodToggleAction(false, RuntimeState.Running(SpectrumSourceKind.PLAYBACK_CAPTURE))
        )
        assertEquals(AodToggleAction.NONE, aodToggleAction(false, RuntimeState.Idle))
    }
}

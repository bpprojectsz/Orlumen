package com.zdmgold.orlumen.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualizerPreviewCanvasTest {
    @Test
    fun demoFrameHasTheRequestedBandsInsideTheUnitRange() {
        val frame = demoFrame(123_456_789L)
        assertEquals(32, frame.size)
        for (index in 0 until frame.size) assertTrue(frame[index] in 0f..1f)
        assertEquals(12, demoFrame(0L, bands = 12).size)
    }

    @Test
    fun demoFrameIsDeterministicAndMovesOverTime() {
        assertEquals(demoFrame(5_000_000_000L).copyValues().toList(), demoFrame(5_000_000_000L).copyValues().toList())
        assertNotEquals(demoFrame(0L).copyValues().toList(), demoFrame(700_000_000L).copyValues().toList())
    }

    @Test
    fun demoFrameFadesTowardTheHighBands() {
        val frame = demoFrame(0L)
        assertTrue(frame[0] > 0f)
        assertTrue(frame.peak <= 1f)
    }
}

package com.zdmgold.orlumen.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorExtensionsTest {
    private val black = 0xFF000000.toInt()
    private val white = 0xFFFFFFFF.toInt()
    private val violet = 0xFF9B6BFF.toInt()

    @Test
    fun brightnessSpansZeroToOne() {
        assertEquals(0f, black.perceivedBrightness(), 0.001f)
        assertEquals(1f, white.perceivedBrightness(), 0.001f)
    }

    @Test
    fun darkColorsAreLiftedUntilVisible() {
        val lifted = 0xFF101020.toInt().liftedForDarkBackground(0.45f)
        assertTrue(lifted.perceivedBrightness() >= 0.44f)
    }

    @Test
    fun brightColorsAreUnchanged() {
        assertEquals(violet, violet.liftedForDarkBackground(0.3f))
        assertEquals(white, white.liftedForDarkBackground(0.9f))
    }

    @Test
    fun liftKeepsColorOpaque() {
        val lifted = black.liftedForDarkBackground(0.5f)
        assertEquals(0xFF, lifted ushr 24)
    }

    @Test
    fun listLiftAppliesToEveryColor() {
        val lifted = listOf(black, white).liftedForDarkBackground()
        assertEquals(2, lifted.size)
        assertTrue(lifted[0].perceivedBrightness() > 0.4f)
        assertEquals(white, lifted[1])
    }
}

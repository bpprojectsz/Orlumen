package com.zdmgold.orlumen.core.utils

import java.time.Instant
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoonPhaseTest {
    private fun fractionAt(iso: String): Float = MoonPhase.fractionAt(Instant.parse(iso).toEpochMilli())

    private fun circularDistance(first: Float, second: Float): Float {
        val difference = abs(first - second)
        return minOf(difference, 1f - difference)
    }

    @Test
    fun referenceNewMoonIsPhaseZero() {
        assertTrue(circularDistance(0f, fractionAt("2000-01-06T18:14:00Z")) < 0.001f)
    }

    @Test
    fun matchesKnownNewMoons() {
        assertTrue(circularDistance(0f, fractionAt("2024-01-11T11:57:00Z")) < 0.03f)
        assertTrue(circularDistance(0f, fractionAt("2024-02-09T22:59:00Z")) < 0.03f)
    }

    @Test
    fun matchesKnownFullMoons() {
        assertTrue(circularDistance(0.5f, fractionAt("2000-01-21T04:40:00Z")) < 0.03f)
        assertTrue(circularDistance(0.5f, fractionAt("2024-01-25T17:54:00Z")) < 0.03f)
    }

    @Test
    fun illuminationRunsFromDarkToFull() {
        assertEquals(0f, MoonPhase.illumination(0f), 0.001f)
        assertEquals(0.5f, MoonPhase.illumination(0.25f), 0.001f)
        assertEquals(1f, MoonPhase.illumination(0.5f), 0.001f)
        assertEquals(0.5f, MoonPhase.illumination(0.75f), 0.001f)
    }

    @Test
    fun waxingMeansTheFirstHalfOfTheCycle() {
        assertTrue(MoonPhase.isWaxing(0.1f))
        assertFalse(MoonPhase.isWaxing(0.5f))
        assertFalse(MoonPhase.isWaxing(0.9f))
    }

    @Test
    fun fractionStaysInsideTheUnitIntervalIncludingBeforeTheReference() {
        for (index in -2000..2000) {
            val fraction = MoonPhase.fractionAt(index * 86_400_000L / 7)
            assertTrue(fraction >= 0f && fraction < 1f)
        }
    }
}

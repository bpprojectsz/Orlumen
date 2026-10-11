package com.zdmgold.orlumen.platform.aod

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BurnInProtectionTest {
    private val step = 60_000L

    @Test
    fun offsetsStayWithinTheConfiguredLimits() {
        val protection = BurnInProtection(12, 8, step)
        for (index in 0..2000) {
            val offset = protection.offsetAt(index * step)
            assertTrue(offset.x in -12..12)
            assertTrue(offset.y in -8..8)
        }
    }

    @Test
    fun offsetIsStableInsideOneStep() {
        val protection = BurnInProtection(12, 12, step)
        assertEquals(protection.offsetAt(5 * step), protection.offsetAt(5 * step + step - 1))
    }

    @Test
    fun offsetMovesBetweenSteps() {
        val protection = BurnInProtection(12, 12, step)
        assertNotEquals(protection.offsetAt(0L), protection.offsetAt(step))
    }

    @Test
    fun reachesBothExtremesOverAFullPeriod() {
        val protection = BurnInProtection(10, 10, step)
        val xs = (0 until 40).map { protection.offsetAt(it * step).x }
        assertTrue(xs.contains(-10))
        assertTrue(xs.contains(10))
    }

    @Test
    fun axesAreOutOfPhase() {
        val protection = BurnInProtection(10, 10, step)
        val offsets = (0 until 40).map { protection.offsetAt(it * step) }
        assertTrue(offsets.any { it.x != it.y })
    }

    @Test
    fun zeroLimitsMeanNoMovement() {
        val protection = BurnInProtection(0, 0, step)
        assertEquals(ShiftOffset(0, 0), protection.offsetAt(123_456_789L))
    }

    @Test
    fun rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException::class.java) { BurnInProtection(-1, 0, step) }
        assertThrows(IllegalArgumentException::class.java) { BurnInProtection(1, 1, 0L) }
    }
}

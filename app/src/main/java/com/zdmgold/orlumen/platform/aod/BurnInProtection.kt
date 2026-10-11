package com.zdmgold.orlumen.platform.aod

data class ShiftOffset(val x: Int, val y: Int)

private const val PERIOD_FACTOR = 4L
private const val HALF_PERIOD_FACTOR = 2L
private const val THREE_QUARTERS = 3L

/**
 * Moves content one pixel per step along a triangle wave on each axis so no pixel stays lit
 * at a fixed position on an AMOLED panel. The axes run a quarter period apart.
 */
class BurnInProtection(private val maxShiftX: Int, private val maxShiftY: Int, private val stepMillis: Long) {
    init {
        require(maxShiftX >= 0 && maxShiftY >= 0) { "shift limits must not be negative" }
        require(stepMillis > 0) { "stepMillis must be positive" }
    }

    fun offsetAt(nowMillis: Long): ShiftOffset {
        val step = nowMillis.coerceAtLeast(0L) / stepMillis
        return ShiftOffset(triangle(step, maxShiftX), triangle(step + maxShiftY, maxShiftY))
    }

    private fun triangle(step: Long, amplitude: Int): Int {
        if (amplitude == 0) return 0
        val position = step % (PERIOD_FACTOR * amplitude)
        val value = if (position < HALF_PERIOD_FACTOR * amplitude) {
            position - amplitude
        } else {
            THREE_QUARTERS * amplitude - position
        }
        return value.toInt()
    }
}

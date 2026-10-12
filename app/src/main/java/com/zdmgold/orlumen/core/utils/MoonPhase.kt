package com.zdmgold.orlumen.core.utils

import kotlin.math.PI
import kotlin.math.cos

private const val SYNODIC_MONTH_DAYS = 29.530588853
private const val MILLIS_PER_DAY = 86_400_000.0
private const val REFERENCE_NEW_MOON_MILLIS = 947_182_440_000.0
private const val HALF = 0.5
private const val TWO_PI = 2.0 * PI

/** Mean-lunation moon phase. Accurate to roughly half a day, which is plenty for a clock face. */
object MoonPhase {
    /** 0 is a new moon, 0.25 first quarter, 0.5 full moon and 0.75 last quarter. */
    fun fractionAt(epochMillis: Long): Float {
        val cycles = (epochMillis - REFERENCE_NEW_MOON_MILLIS) / MILLIS_PER_DAY / SYNODIC_MONTH_DAYS
        val fraction = cycles - Math.floor(cycles)
        return fraction.toFloat().coerceIn(0f, Math.nextDown(1f))
    }

    fun illumination(fraction: Float): Float = ((1.0 - cos(TWO_PI * fraction)) * HALF).toFloat()

    fun isWaxing(fraction: Float): Boolean = fraction < HALF
}

package com.zdmgold.orlumen.core.utils

import java.text.DateFormatSymbols
import java.util.Locale

data class ClockParts(val hours: String, val minutes: String, val marker: String?)

object TimeFormatter {
    private const val HOURS_PER_DAY = 24
    private const val HOURS_PER_HALF_DAY = 12
    private const val MINUTES_PER_HOUR = 60
    private const val SECONDS_PER_MINUTE = 60
    private const val SECONDS_PER_HOUR = MINUTES_PER_HOUR * SECONDS_PER_MINUTE
    private const val MILLIS_PER_SECOND = 1000L
    private const val DIGIT_WIDTH = 2

    fun parts(hour: Int, minute: Int, use24Hour: Boolean, locale: Locale = Locale.getDefault()): ClockParts {
        require(hour in 0 until HOURS_PER_DAY) { "hour out of range: $hour" }
        require(minute in 0 until MINUTES_PER_HOUR) { "minute out of range: $minute" }
        if (use24Hour) {
            return ClockParts(pad(hour), pad(minute), null)
        }
        val remainder = hour % HOURS_PER_HALF_DAY
        val displayHour = if (remainder == 0) HOURS_PER_HALF_DAY else remainder
        val markers = DateFormatSymbols.getInstance(locale).amPmStrings
        val marker = if (hour < HOURS_PER_HALF_DAY) markers[0] else markers[1]
        return ClockParts(pad(displayHour), pad(minute), marker)
    }

    fun format(hour: Int, minute: Int, use24Hour: Boolean, locale: Locale = Locale.getDefault()): String {
        val clock = parts(hour, minute, use24Hour, locale)
        val time = "${clock.hours}:${clock.minutes}"
        return if (clock.marker == null) time else "$time ${clock.marker}"
    }

    fun formatDuration(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0L) / MILLIS_PER_SECOND
        val hours = (totalSeconds / SECONDS_PER_HOUR).toInt()
        val minutes = ((totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE).toInt()
        val seconds = (totalSeconds % SECONDS_PER_MINUTE).toInt()
        return if (hours > 0) "$hours:${pad(minutes)}:${pad(seconds)}" else "$minutes:${pad(seconds)}"
    }

    private fun pad(value: Int): String = value.toString().padStart(DIGIT_WIDTH, '0')
}

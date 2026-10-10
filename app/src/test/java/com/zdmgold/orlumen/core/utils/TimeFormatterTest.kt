package com.zdmgold.orlumen.core.utils

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TimeFormatterTest {
    private val locale = Locale.US

    @Test
    fun midnightIn24HourMode() {
        val parts = TimeFormatter.parts(0, 5, true, locale)
        assertEquals("00", parts.hours)
        assertEquals("05", parts.minutes)
        assertNull(parts.marker)
    }

    @Test
    fun midnightAndNoonIn12HourMode() {
        assertEquals("12:00 AM", TimeFormatter.format(0, 0, false, locale))
        assertEquals("12:00 PM", TimeFormatter.format(12, 0, false, locale))
    }

    @Test
    fun afternoonIn12HourMode() {
        assertEquals("01:45 PM", TimeFormatter.format(13, 45, false, locale))
        assertEquals("11:59 PM", TimeFormatter.format(23, 59, false, locale))
    }

    @Test
    fun rejectsInvalidTime() {
        assertThrows(IllegalArgumentException::class.java) { TimeFormatter.parts(24, 0, true, locale) }
        assertThrows(IllegalArgumentException::class.java) { TimeFormatter.parts(0, 60, true, locale) }
    }

    @Test
    fun formatsTrackDurations() {
        assertEquals("0:00", TimeFormatter.formatDuration(0L))
        assertEquals("1:05", TimeFormatter.formatDuration(65_000L))
        assertEquals("1:02:05", TimeFormatter.formatDuration(3_725_000L))
        assertEquals("0:00", TimeFormatter.formatDuration(-5L))
    }
}

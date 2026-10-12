package com.zdmgold.orlumen.ui.components

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorPickerWheelTest {
    private val red = 0xFFFF0000.toInt()
    private val green = 0xFF00FF00.toInt()
    private val blue = 0xFF0000FF.toInt()

    private fun channelDistance(first: Int, second: Int): Int = listOf(16, 8, 0).maxOf { shift ->
        abs((first shr shift and 0xFF) - (second shr shift and 0xFF))
    }

    @Test
    fun primariesConvertToTheExpectedHues() {
        assertEquals(0f, ColorWheelMath.argbToHsv(red)[0], 0.01f)
        assertEquals(120f, ColorWheelMath.argbToHsv(green)[0], 0.01f)
        assertEquals(240f, ColorWheelMath.argbToHsv(blue)[0], 0.01f)
    }

    @Test
    fun hsvConvertsBackToThePrimaries() {
        assertEquals(red, ColorWheelMath.hsvToArgb(0f, 1f, 1f))
        assertEquals(green, ColorWheelMath.hsvToArgb(120f, 1f, 1f))
        assertEquals(blue, ColorWheelMath.hsvToArgb(240f, 1f, 1f))
    }

    @Test
    fun whiteBlackAndGreyHaveNoSaturation() {
        assertEquals(0xFFFFFFFF.toInt(), ColorWheelMath.hsvToArgb(0f, 0f, 1f))
        assertEquals(0xFF000000.toInt(), ColorWheelMath.hsvToArgb(200f, 1f, 0f))
        assertEquals(0f, ColorWheelMath.argbToHsv(0xFF808080.toInt())[1], 0.001f)
    }

    @Test
    fun roundTripsStayWithinOneChannelStep() {
        listOf(0xFF9B6BFF, 0xFFFF5FD2, 0xFF2DE2C9, 0xFFFFC857, 0xFF050508).forEach { value ->
            val argb = value.toInt()
            val hsv = ColorWheelMath.argbToHsv(argb)
            val back = ColorWheelMath.hsvToArgb(hsv[0], hsv[1], hsv[2])
            assertTrue("$argb -> $back", channelDistance(argb, back) <= 1)
        }
    }

    @Test
    fun hueWrapsAndInputsAreClamped() {
        assertEquals(ColorWheelMath.hsvToArgb(10f, 1f, 1f), ColorWheelMath.hsvToArgb(370f, 1f, 1f))
        assertEquals(ColorWheelMath.hsvToArgb(350f, 1f, 1f), ColorWheelMath.hsvToArgb(-10f, 1f, 1f))
        assertEquals(red, ColorWheelMath.hsvToArgb(0f, 5f, 5f))
    }

    @Test
    fun touchPositionMapsToHueClockwiseFromTheRight() {
        assertEquals(0f, ColorWheelMath.hueSaturationAt(10f, 0f, 100f).first, 0.01f)
        assertEquals(90f, ColorWheelMath.hueSaturationAt(0f, 10f, 100f).first, 0.01f)
        assertEquals(180f, ColorWheelMath.hueSaturationAt(-10f, 0f, 100f).first, 0.01f)
        assertEquals(270f, ColorWheelMath.hueSaturationAt(0f, -10f, 100f).first, 0.01f)
    }

    @Test
    fun saturationGrowsWithDistanceAndStopsAtTheRim() {
        assertEquals(0.1f, ColorWheelMath.hueSaturationAt(10f, 0f, 100f).second, 0.001f)
        assertEquals(1f, ColorWheelMath.hueSaturationAt(500f, 0f, 100f).second, 0.001f)
        assertEquals(0f, ColorWheelMath.hueSaturationAt(0f, 0f, 100f).second, 0.001f)
    }
}

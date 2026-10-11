package com.zdmgold.orlumen.platform.audio

import kotlin.math.PI
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FftProcessorTest {
    private fun sine(size: Int, bin: Int, amplitude: Float = 1f): FloatArray =
        FloatArray(size) { amplitude * sin(2.0 * PI * bin * it / size).toFloat() }

    private fun FloatArray.peakIndex(): Int = indices.maxByOrNull { this[it] } ?: -1

    @Test
    fun sineOnABinPeaksAtThatBinWithUnitMagnitude() {
        val fft = FftProcessor(512)
        val magnitudes = fft.magnitudes(sine(512, 16))
        assertEquals(256, magnitudes.size)
        assertEquals(16, magnitudes.peakIndex())
        assertEquals(1f, magnitudes[16], 0.02f)
        assertTrue(magnitudes[100] < 0.01f)
    }

    @Test
    fun amplitudeScalesLinearly() {
        val fft = FftProcessor(256)
        assertEquals(0.25f, fft.magnitudes(sine(256, 8, 0.25f))[8], 0.01f)
    }

    @Test
    fun silenceHasNoEnergy() {
        val magnitudes = FftProcessor(128).magnitudes(FloatArray(128))
        assertTrue(magnitudes.all { it == 0f })
    }

    @Test
    fun constantOffsetLandsInBinZero() {
        val magnitudes = FftProcessor(128).magnitudes(FloatArray(128) { 0.5f })
        assertEquals(0, magnitudes.peakIndex())
    }

    @Test
    fun reusingTheProcessorGivesIdenticalResults() {
        val fft = FftProcessor(256)
        val first = fft.magnitudes(sine(256, 12)).copyOf()
        fft.magnitudes(sine(256, 40))
        assertEquals(first.toList(), fft.magnitudes(sine(256, 12)).toList())
    }

    @Test
    fun rejectsInvalidSizesAndShortInput() {
        assertThrows(IllegalArgumentException::class.java) { FftProcessor(100) }
        assertThrows(IllegalArgumentException::class.java) { FftProcessor(2) }
        assertThrows(IllegalArgumentException::class.java) { FftProcessor(128).magnitudes(FloatArray(64)) }
    }
}

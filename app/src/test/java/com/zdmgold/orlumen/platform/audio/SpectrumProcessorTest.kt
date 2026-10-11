package com.zdmgold.orlumen.platform.audio

import com.zdmgold.orlumen.core.model.SpectrumFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpectrumProcessorTest {
    private val sampleRate = 44100

    private fun tone(bin: Int, value: Float = 1f): FloatArray = FloatArray(512).also { it[bin] = value }

    private fun SpectrumFrame.levels(): List<Float> = copyValues().toList()

    private fun SpectrumFrame.peakBand(): Int = levels().indices.maxByOrNull { levels()[it] } ?: -1

    private fun SpectrumProcessor.run(
        magnitudes: FloatArray,
        sensitivity: Float = 0.5f,
        smoothing: Float = 0f
    ): SpectrumFrame = process(magnitudes, sampleRate, sensitivity, smoothing, 0L)

    @Test
    fun producesTheRequestedNumberOfBands() {
        assertEquals(32, SpectrumProcessor().run(tone(10)).size)
        assertEquals(12, SpectrumProcessor(12).run(tone(10)).size)
    }

    @Test
    fun silenceYieldsZeroLevels() {
        val frame = SpectrumProcessor().run(FloatArray(512))
        assertTrue(frame.levels().all { it == 0f })
    }

    @Test
    fun higherFrequenciesLandInHigherBands() {
        val low = SpectrumProcessor().run(tone(4)).peakBand()
        val high = SpectrumProcessor().run(tone(92)).peakBand()
        assertTrue("low=$low high=$high", low < high)
        assertTrue(SpectrumProcessor().run(tone(4)).peak > 0f)
    }

    @Test
    fun levelsStayWithinZeroAndOne() {
        val frame = SpectrumProcessor().run(FloatArray(512) { 50f }, sensitivity = 1f)
        assertTrue(frame.levels().all { it in 0f..1f })
    }

    @Test
    fun sensitivityRaisesLevels() {
        val quiet = tone(20, 0.01f)
        val low = SpectrumProcessor().run(quiet, sensitivity = 0.1f).peak
        val high = SpectrumProcessor().run(quiet, sensitivity = 0.9f).peak
        assertTrue("low=$low high=$high", high > low)
    }

    @Test
    fun smoothingSlowsTheDecay() {
        val loud = tone(20)
        val instant = SpectrumProcessor()
        instant.run(loud, smoothing = 0f)
        assertEquals(0f, instant.run(FloatArray(512), smoothing = 0f).peak, 0f)

        val smooth = SpectrumProcessor()
        val peak = smooth.run(loud, smoothing = 0.9f).peak
        val after = smooth.run(FloatArray(512), smoothing = 0.9f).peak
        assertTrue("peak=$peak after=$after", after > 0f && after < peak)
    }

    @Test
    fun resetClearsState() {
        val processor = SpectrumProcessor()
        processor.run(tone(20), smoothing = 0.9f)
        processor.reset()
        assertEquals(0f, processor.run(FloatArray(512), smoothing = 0.9f).peak, 0f)
    }
}

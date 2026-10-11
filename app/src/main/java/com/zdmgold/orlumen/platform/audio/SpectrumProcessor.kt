package com.zdmgold.orlumen.platform.audio

import com.zdmgold.orlumen.core.model.SpectrumFrame
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

private const val MIN_FREQUENCY_HZ = 40f
private const val MAX_FREQUENCY_HZ = 16000f
private const val FLOOR_DB = -60f
private const val MAX_GAIN_DB = 24f
private const val DB_PER_DECADE = 20f
private const val EPSILON = 1e-6f
private const val ATTACK_SMOOTHING = 0.6f
private const val RELEASE_SMOOTHING = 0.92f
private const val NEUTRAL_SENSITIVITY = 0.5f

/**
 * Turns linear FFT magnitudes into logarithmically spaced, smoothed 0..1 band levels.
 * Keeps per-band state between frames, so use one instance per capture session on one thread.
 */
class SpectrumProcessor(val bandCount: Int = DEFAULT_BAND_COUNT) {
    private val levels = FloatArray(bandCount)

    init {
        require(bandCount > 0) { "bandCount must be positive" }
    }

    fun reset() {
        levels.fill(0f)
    }

    fun process(
        magnitudes: FloatArray,
        sampleRate: Int,
        sensitivity: Float,
        smoothing: Float,
        timestampNanos: Long
    ): SpectrumFrame {
        val nyquist = sampleRate / 2f
        val maxFrequency = min(MAX_FREQUENCY_HZ, nyquist)
        val binWidth = nyquist / magnitudes.size
        val gainDb = (sensitivity - NEUTRAL_SENSITIVITY) * 2f * MAX_GAIN_DB
        val attack = 1f - ATTACK_SMOOTHING * smoothing
        val release = 1f - RELEASE_SMOOTHING * smoothing
        for (band in 0 until bandCount) {
            val lowHz = bandEdge(band, maxFrequency)
            val highHz = bandEdge(band + 1, maxFrequency)
            val target = levelFor(peakMagnitude(magnitudes, lowHz, highHz, binWidth), gainDb)
            val previous = levels[band]
            val rate = if (target > previous) attack else release
            levels[band] = previous + (target - previous) * rate
        }
        return SpectrumFrame(levels.copyOf(), timestampNanos)
    }

    private fun bandEdge(index: Int, maxFrequency: Float): Float =
        MIN_FREQUENCY_HZ * (maxFrequency / MIN_FREQUENCY_HZ).pow(index.toFloat() / bandCount)

    private fun peakMagnitude(magnitudes: FloatArray, lowHz: Float, highHz: Float, binWidth: Float): Float {
        val lastBin = magnitudes.size - 1
        val first = floor(lowHz / binWidth).toInt().coerceIn(0, lastBin)
        val last = max(first, ceil(highHz / binWidth).toInt() - 1).coerceIn(0, lastBin)
        var peak = 0f
        for (bin in first..last) {
            peak = max(peak, magnitudes[bin])
        }
        return peak
    }

    private fun levelFor(magnitude: Float, gainDb: Float): Float {
        val decibels = DB_PER_DECADE * log10(max(magnitude, EPSILON)) + gainDb
        return ((decibels - FLOOR_DB) / -FLOOR_DB).coerceIn(0f, 1f)
    }

    companion object {
        const val DEFAULT_BAND_COUNT = 32
    }
}

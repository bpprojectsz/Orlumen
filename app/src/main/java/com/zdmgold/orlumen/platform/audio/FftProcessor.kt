package com.zdmgold.orlumen.platform.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val MIN_SIZE = 4
private const val TWO_PI = 2.0 * PI
private const val INT_BITS = 32
private const val AMPLITUDE_NORMALIZATION = 4f
private const val HANN_HALF = 0.5

/**
 * Radix-2 FFT with a periodic Hann window. Output magnitudes are normalized so that a
 * full-scale sine centred on a bin reads 1.0. Instances reuse work buffers and are not thread-safe.
 */
class FftProcessor(val size: Int) {
    private val window: FloatArray
    private val cosTable: FloatArray
    private val sinTable: FloatArray
    private val bitReversed: IntArray
    private val real = FloatArray(size)
    private val imag = FloatArray(size)

    init {
        require(size >= MIN_SIZE && size and (size - 1) == 0) { "FFT size must be a power of two >= $MIN_SIZE" }
        window = FloatArray(size) { (HANN_HALF - HANN_HALF * cos(TWO_PI * it / size)).toFloat() }
        cosTable = FloatArray(size / 2) { cos(TWO_PI * it / size).toFloat() }
        sinTable = FloatArray(size / 2) { (-sin(TWO_PI * it / size)).toFloat() }
        val bits = INT_BITS - Integer.numberOfLeadingZeros(size) - 1
        bitReversed = IntArray(size) { Integer.reverse(it) ushr (INT_BITS - bits) }
    }

    fun magnitudes(samples: FloatArray, output: FloatArray = FloatArray(size / 2)): FloatArray {
        require(samples.size >= size) { "Expected at least $size samples but got ${samples.size}" }
        require(output.size >= size / 2) { "Output needs room for ${size / 2} bins" }
        for (index in 0 until size) {
            real[bitReversed[index]] = samples[index] * window[index]
            imag[bitReversed[index]] = 0f
        }
        transform()
        val scale = AMPLITUDE_NORMALIZATION / size
        for (bin in 0 until size / 2) {
            output[bin] = sqrt(real[bin] * real[bin] + imag[bin] * imag[bin]) * scale
        }
        return output
    }

    private fun transform() {
        var length = 2
        while (length <= size) {
            val half = length / 2
            val step = size / length
            var start = 0
            while (start < size) {
                butterfly(start, half, step)
                start += length
            }
            length *= 2
        }
    }

    private fun butterfly(start: Int, half: Int, step: Int) {
        for (offset in 0 until half) {
            val twiddleReal = cosTable[offset * step]
            val twiddleImag = sinTable[offset * step]
            val upper = start + offset
            val lower = upper + half
            val tempReal = twiddleReal * real[lower] - twiddleImag * imag[lower]
            val tempImag = twiddleReal * imag[lower] + twiddleImag * real[lower]
            real[lower] = real[upper] - tempReal
            imag[lower] = imag[upper] - tempImag
            real[upper] += tempReal
            imag[upper] += tempImag
        }
    }
}

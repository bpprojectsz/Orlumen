package com.zdmgold.orlumen.core.model

class SpectrumFrame(private val values: FloatArray, val timestampNanos: Long) {
    val size: Int
        get() = values.size

    val peak: Float
        get() = values.maxOrNull() ?: 0f

    operator fun get(index: Int): Float = values[index]

    fun copyValues(): FloatArray = values.copyOf()

    companion object {
        fun silent(bandCount: Int, timestampNanos: Long = 0L): SpectrumFrame {
            require(bandCount > 0) { "bandCount must be positive" }
            return SpectrumFrame(FloatArray(bandCount), timestampNanos)
        }
    }
}

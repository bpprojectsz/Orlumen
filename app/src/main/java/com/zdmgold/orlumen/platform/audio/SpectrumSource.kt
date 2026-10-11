package com.zdmgold.orlumen.platform.audio

import kotlinx.coroutines.flow.Flow

enum class SpectrumSourceKind {
    GLOBAL_MIX,
    PLAYBACK_CAPTURE
}

class SampleBlock(val samples: FloatArray, val sampleRate: Int)

class SpectrumSourceException(message: String, cause: Throwable? = null) : Exception(message, cause)

interface SpectrumSource {
    val kind: SpectrumSourceKind

    fun isSupported(): Boolean

    fun blocks(): Flow<SampleBlock>
}

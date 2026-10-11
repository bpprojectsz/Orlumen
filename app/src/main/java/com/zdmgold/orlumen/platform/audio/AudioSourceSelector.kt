package com.zdmgold.orlumen.platform.audio

import com.zdmgold.orlumen.core.model.AudioSourceMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioSourceSelector internal constructor(private val sources: Map<SpectrumSourceKind, SpectrumSource>) {
    @Inject
    constructor(
        globalMix: GlobalMixSpectrumSource,
        playbackCapture: PlaybackCaptureSpectrumSource
    ) : this(
        mapOf(
            SpectrumSourceKind.GLOBAL_MIX to globalMix,
            SpectrumSourceKind.PLAYBACK_CAPTURE to playbackCapture
        )
    )

    fun candidates(mode: AudioSourceMode): List<SpectrumSource> {
        val order = when (mode) {
            AudioSourceMode.AUTO -> listOf(SpectrumSourceKind.GLOBAL_MIX, SpectrumSourceKind.PLAYBACK_CAPTURE)
            AudioSourceMode.GLOBAL_MIX -> listOf(SpectrumSourceKind.GLOBAL_MIX)
            AudioSourceMode.PLAYBACK_CAPTURE -> listOf(SpectrumSourceKind.PLAYBACK_CAPTURE)
        }
        return order.mapNotNull { sources[it] }.filter { it.isSupported() }
    }
}

package com.zdmgold.orlumen.platform.audio

import com.zdmgold.orlumen.core.model.AudioSourceMode
import com.zdmgold.orlumen.core.model.SpectrumFrame
import com.zdmgold.orlumen.core.model.VisualizerConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

private const val MIN_FFT_SIZE = 128

@Singleton
class AudioPipeline @Inject constructor(private val selector: AudioSourceSelector) {
    @Volatile
    private var sensitivity = VisualizerConfig.DEFAULT_SENSITIVITY

    @Volatile
    private var smoothing = VisualizerConfig.DEFAULT_SMOOTHING

    private val _activeKind = MutableStateFlow<SpectrumSourceKind?>(null)
    val activeKind: StateFlow<SpectrumSourceKind?> = _activeKind.asStateFlow()

    private val _latestPeak = MutableStateFlow(0f)
    val latestPeak: StateFlow<Float> = _latestPeak.asStateFlow()

    fun updateTuning(sensitivity: Float, smoothing: Float) {
        this.sensitivity = sensitivity
        this.smoothing = smoothing
    }

    fun frames(mode: AudioSourceMode): Flow<SpectrumFrame> = flow {
        val processor = SpectrumProcessor()
        val fftBySize = HashMap<Int, FftProcessor>()
        var lastFailure: SpectrumSourceException? = null
        for (source in selector.candidates(mode)) {
            try {
                _activeKind.value = source.kind
                source.blocks().collect { block ->
                    toFrame(block, processor, fftBySize)?.let { frame ->
                        _latestPeak.value = frame.peak
                        emit(frame)
                    }
                }
                return@flow
            } catch (failure: SpectrumSourceException) {
                lastFailure = failure
            }
        }
        throw SpectrumSourceException("No audio source could be started for $mode", lastFailure)
    }.onCompletion {
        _activeKind.value = null
        _latestPeak.value = 0f
    }

    private fun toFrame(
        block: SampleBlock,
        processor: SpectrumProcessor,
        fftBySize: MutableMap<Int, FftProcessor>
    ): SpectrumFrame? {
        val size = Integer.highestOneBit(block.samples.size)
        if (size < MIN_FFT_SIZE) return null
        val fft = fftBySize.getOrPut(size) { FftProcessor(size) }
        val magnitudes = fft.magnitudes(block.samples)
        return processor.process(magnitudes, block.sampleRate, sensitivity, smoothing, System.nanoTime())
    }
}

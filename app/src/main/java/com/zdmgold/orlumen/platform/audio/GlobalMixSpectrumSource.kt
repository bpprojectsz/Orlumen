package com.zdmgold.orlumen.platform.audio

import android.media.audiofx.Visualizer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val GLOBAL_MIX_SESSION = 0
private const val MAX_CAPTURE_SIZE = 1024
private const val MILLIHERTZ_PER_HERTZ = 1000
private const val BYTE_MASK = 0xFF
private const val BYTE_CENTER = 128
private const val BYTE_SCALE = 128f

@Singleton
class GlobalMixSpectrumSource @Inject constructor() : SpectrumSource {
    override val kind = SpectrumSourceKind.GLOBAL_MIX

    override fun isSupported(): Boolean = true

    override fun blocks(): Flow<SampleBlock> = callbackFlow {
        val visualizer = createVisualizer()
        val listener = object : Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                if (waveform != null) {
                    trySend(SampleBlock(waveform.toSamples(), samplingRate / MILLIHERTZ_PER_HERTZ))
                }
            }

            override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) = Unit
        }
        try {
            val captureRate = Visualizer.getMaxCaptureRate()
            checkStatus(visualizer.setDataCaptureListener(listener, captureRate, true, false), "capture listener")
            checkStatus(visualizer.setEnabled(true), "enable")
            awaitClose()
        } finally {
            runCatching { visualizer.setEnabled(false) }
            visualizer.release()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun createVisualizer(): Visualizer {
        val visualizer = try {
            Visualizer(GLOBAL_MIX_SESSION)
        } catch (failure: RuntimeException) {
            throw SpectrumSourceException("Global mix visualizer is unavailable", failure)
        }
        try {
            val captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(MAX_CAPTURE_SIZE)
            checkStatus(visualizer.setCaptureSize(captureSize), "capture size")
        } catch (failure: RuntimeException) {
            visualizer.release()
            throw SpectrumSourceException("Global mix visualizer could not be configured", failure)
        }
        return visualizer
    }

    private fun checkStatus(status: Int, step: String) {
        if (status != Visualizer.SUCCESS) {
            throw SpectrumSourceException("Global mix visualizer step '$step' failed with status $status")
        }
    }

    private fun ByteArray.toSamples(): FloatArray = FloatArray(size) { index ->
        ((this[index].toInt() and BYTE_MASK) - BYTE_CENTER) / BYTE_SCALE
    }
}

package com.zdmgold.orlumen.platform.audio

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.os.Build
import androidx.annotation.RequiresApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

private const val SAMPLE_RATE = 44100
private const val BLOCK_SAMPLES = 1024
private const val BYTES_PER_SAMPLE = 2
private const val BUFFER_BLOCKS = 4
private const val SHORT_SCALE = 32768f

@Singleton
class PlaybackCaptureSpectrumSource @Inject constructor() : SpectrumSource {
    @Volatile
    private var projection: MediaProjection? = null

    override val kind = SpectrumSourceKind.PLAYBACK_CAPTURE

    override fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && projection != null

    fun attachProjection(mediaProjection: MediaProjection) {
        projection = mediaProjection
    }

    fun detachProjection() {
        projection = null
    }

    override fun blocks(): Flow<SampleBlock> = flow {
        val activeProjection = projection
            ?: throw SpectrumSourceException("Screen capture permission has not been granted")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            throw SpectrumSourceException("Playback capture needs Android 10 or newer")
        }
        val record = createRecord(activeProjection)
        try {
            record.startRecording()
            val buffer = ShortArray(BLOCK_SAMPLES)
            while (currentCoroutineContext().isActive) {
                val read = record.read(buffer, 0, buffer.size)
                if (read < 0) throw SpectrumSourceException("Audio read failed with code $read")
                if (read > 0) emit(SampleBlock(FloatArray(read) { buffer[it] / SHORT_SCALE }, SAMPLE_RATE))
            }
        } finally {
            runCatching { record.stop() }
            record.release()
        }
    }.flowOn(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    @Suppress("TooGenericExceptionCaught")
    private fun createRecord(mediaProjection: MediaProjection): AudioRecord {
        val capture = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(AudioAttributes.USAGE_GAME)
            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
            .build()
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
            .build()
        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferBytes = maxOf(minBuffer, BLOCK_SAMPLES * BYTES_PER_SAMPLE * BUFFER_BLOCKS)
        val record = try {
            AudioRecord.Builder()
                .setAudioPlaybackCaptureConfig(capture)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferBytes)
                .build()
        } catch (failure: RuntimeException) {
            throw SpectrumSourceException("Playback capture could not be created", failure)
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            throw SpectrumSourceException("Playback capture did not initialize")
        }
        return record
    }
}

package com.zdmgold.orlumen.platform.audio

import com.zdmgold.orlumen.core.model.AudioSourceMode
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSource(
    override val kind: SpectrumSourceKind,
    private val supported: Boolean = true,
    private val blocks: List<SampleBlock> = emptyList(),
    private val failure: SpectrumSourceException? = null
) : SpectrumSource {
    override fun isSupported(): Boolean = supported

    override fun blocks(): Flow<SampleBlock> = flow {
        if (failure != null) throw failure
        blocks.forEach { emit(it) }
    }
}

class AudioPipelineTest {
    private fun sineBlock(): SampleBlock = SampleBlock(
        FloatArray(1024) {
            sin(2.0 * PI * 32 * it / 1024).toFloat()
        },
        44100
    )

    private fun pipeline(globalMix: FakeSource, capture: FakeSource): AudioPipeline {
        val selector = AudioSourceSelector(
            mapOf(
                SpectrumSourceKind.GLOBAL_MIX to globalMix,
                SpectrumSourceKind.PLAYBACK_CAPTURE to capture
            )
        )
        return AudioPipeline(selector)
    }

    @Test
    fun turnsBlocksIntoFramesAndClearsActiveKindWhenDone() = runTest {
        val pipeline = pipeline(
            FakeSource(SpectrumSourceKind.GLOBAL_MIX, blocks = List(3) { sineBlock() }),
            FakeSource(SpectrumSourceKind.PLAYBACK_CAPTURE)
        )
        val frames = pipeline.frames(AudioSourceMode.AUTO).toList()
        assertEquals(3, frames.size)
        assertTrue(frames.all { it.size == 32 && it.peak > 0f })
        assertNull(pipeline.activeKind.value)
    }

    @Test
    fun fallsBackWhenTheFirstSourceFails() = runTest {
        val pipeline = pipeline(
            FakeSource(SpectrumSourceKind.GLOBAL_MIX, failure = SpectrumSourceException("blocked")),
            FakeSource(SpectrumSourceKind.PLAYBACK_CAPTURE, blocks = List(2) { sineBlock() })
        )
        val kinds = pipeline.frames(AudioSourceMode.AUTO).map { pipeline.activeKind.value }.toList()
        assertEquals(listOf(SpectrumSourceKind.PLAYBACK_CAPTURE, SpectrumSourceKind.PLAYBACK_CAPTURE), kinds)
    }

    @Test
    fun skipsUnsupportedSources() {
        val selector = AudioSourceSelector(
            mapOf(
                SpectrumSourceKind.GLOBAL_MIX to FakeSource(SpectrumSourceKind.GLOBAL_MIX, supported = false),
                SpectrumSourceKind.PLAYBACK_CAPTURE to FakeSource(SpectrumSourceKind.PLAYBACK_CAPTURE)
            )
        )
        assertEquals(
            listOf(SpectrumSourceKind.PLAYBACK_CAPTURE),
            selector.candidates(AudioSourceMode.AUTO).map { it.kind }
        )
    }

    @Test
    fun explicitModeUsesOnlyThatSource() {
        val selector = AudioSourceSelector(
            mapOf(
                SpectrumSourceKind.GLOBAL_MIX to FakeSource(SpectrumSourceKind.GLOBAL_MIX),
                SpectrumSourceKind.PLAYBACK_CAPTURE to FakeSource(SpectrumSourceKind.PLAYBACK_CAPTURE)
            )
        )
        assertEquals(
            listOf(SpectrumSourceKind.GLOBAL_MIX),
            selector.candidates(AudioSourceMode.GLOBAL_MIX).map { it.kind }
        )
        assertEquals(
            listOf(SpectrumSourceKind.PLAYBACK_CAPTURE),
            selector.candidates(AudioSourceMode.PLAYBACK_CAPTURE).map { it.kind }
        )
    }

    @Test
    fun failsWhenEverySourceFails() = runTest {
        val pipeline = pipeline(
            FakeSource(SpectrumSourceKind.GLOBAL_MIX, failure = SpectrumSourceException("a")),
            FakeSource(SpectrumSourceKind.PLAYBACK_CAPTURE, failure = SpectrumSourceException("b"))
        )
        val result = runCatching { pipeline.frames(AudioSourceMode.AUTO).toList() }
        assertTrue(result.exceptionOrNull() is SpectrumSourceException)
        assertNull(pipeline.activeKind.value)
    }

    @Test
    fun failsWhenNoSourceIsSupported() = runTest {
        val pipeline = pipeline(
            FakeSource(SpectrumSourceKind.GLOBAL_MIX, supported = false),
            FakeSource(SpectrumSourceKind.PLAYBACK_CAPTURE, supported = false)
        )
        val result = runCatching { pipeline.frames(AudioSourceMode.AUTO).toList() }
        assertTrue(result.exceptionOrNull() is SpectrumSourceException)
    }
}

package com.zdmgold.orlumen.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class NowPlayingTest {
    private fun track(isPlaying: Boolean, position: Long = 10_000L, duration: Long = 200_000L): NowPlaying = NowPlaying(
        sourcePackage = "app",
        title = "Song",
        artist = "Artist",
        albumArt = null,
        isPlaying = isPlaying,
        positionMs = position,
        durationMs = duration,
        capturedAtElapsedMs = 1_000L
    )

    @Test
    fun playingTrackAdvancesWithElapsedTime() {
        assertEquals(15_000L, track(isPlaying = true).positionAt(6_000L))
    }

    @Test
    fun pausedTrackDoesNotAdvance() {
        assertEquals(10_000L, track(isPlaying = false).positionAt(60_000L))
    }

    @Test
    fun positionNeverPassesDurationOrGoesBackwards() {
        assertEquals(200_000L, track(isPlaying = true).positionAt(10_000_000L))
        assertEquals(10_000L, track(isPlaying = true).positionAt(0L))
    }

    @Test
    fun unknownDurationIsNotClamped() {
        assertEquals(20_000L, track(isPlaying = true, duration = 0L).positionAt(11_000L))
    }

    @Test
    fun progressIsAFractionOfDuration() {
        assertEquals(0.05f, track(isPlaying = false).progress, 0.0001f)
        assertEquals(0f, track(isPlaying = false, duration = 0L).progress, 0f)
    }
}

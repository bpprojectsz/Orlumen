package com.zdmgold.orlumen.core.repository

import com.zdmgold.orlumen.core.model.NowPlaying
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NowPlayingRepositoryTest {
    private fun session(packageName: String, playing: Boolean): NowPlaying =
        NowPlaying(packageName, "t", "a", null, playing, 0L, 0L)

    @Test
    fun prefersAPlayingSession() {
        val paused = session("one", false)
        val playing = session("two", true)
        assertSame(playing, selectActiveSession(listOf(paused, playing), emptySet()))
    }

    @Test
    fun fallsBackToFirstPausedSession() {
        val first = session("one", false)
        assertSame(first, selectActiveSession(listOf(first, session("two", false)), emptySet()))
    }

    @Test
    fun honoursTheAllowedSourceFilter() {
        val blocked = session("blocked", true)
        val allowed = session("allowed", false)
        assertSame(allowed, selectActiveSession(listOf(blocked, allowed), setOf("allowed")))
    }

    @Test
    fun emptyFilterMeansEveryApp() {
        val only = session("any", true)
        assertSame(only, selectActiveSession(listOf(only), emptySet()))
    }

    @Test
    fun returnsIdleWhenNothingMatches() {
        assertSame(NowPlaying.idle, selectActiveSession(emptyList(), emptySet()))
        assertEquals("", selectActiveSession(listOf(session("x", true)), setOf("y")).sourcePackage)
    }
}

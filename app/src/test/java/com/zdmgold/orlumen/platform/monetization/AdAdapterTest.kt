package com.zdmgold.orlumen.platform.monetization

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdAdapterTest {
    private val interval = 180_000L

    @Test
    fun neverShowsToProUsers() {
        assertFalse(shouldShowInterstitial(1_000_000L, 0L, interval, isPro = true))
    }

    @Test
    fun showsTheFirstInterstitial() {
        assertTrue(shouldShowInterstitial(1_000_000L, 0L, interval, isPro = false))
    }

    @Test
    fun respectsTheMinimumInterval() {
        assertFalse(shouldShowInterstitial(1_100_000L, 1_000_000L, interval, isPro = false))
        assertTrue(shouldShowInterstitial(1_180_000L, 1_000_000L, interval, isPro = false))
    }
}

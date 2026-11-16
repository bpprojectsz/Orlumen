package com.zdmgold.orlumen.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterstitialPolicyTest {
    private val minute = 60_000L
    private val now = 10_000_000_000L

    @Test
    fun neverShowsToPremiumOwners() {
        assertFalse(canShowInterstitial(now, emptyList(), isPro = true))
    }

    @Test
    fun showsTheFirstOne() {
        assertTrue(canShowInterstitial(now, emptyList(), isPro = false))
    }

    @Test
    fun needsFiveMinutesBetweenAds() {
        assertFalse(canShowInterstitial(now, listOf(now - 4 * minute), isPro = false))
        assertTrue(canShowInterstitial(now, listOf(now - 5 * minute), isPro = false))
    }

    @Test
    fun allowsEightAnHourAndNotANinth() {
        val seven = (1..7).map { now - 55 * minute + it * 6 * minute }
        assertTrue(canShowInterstitial(now, seven.dropLast(1) + (now - 6 * minute), isPro = false))
        val eight = (0 until 8).map { now - 59 * minute + it * 7 * minute }
        assertEquals(8, recentInterstitialTimes(now, eight).size)
        assertFalse(canShowInterstitial(now, eight.dropLast(1) + (now - 6 * minute), isPro = false))
    }

    @Test
    fun theHourlyCountForgetsOldAds() {
        val old = (0 until 8).map { now - 61 * minute - it * minute }
        assertTrue(canShowInterstitial(now, old, isPro = false))
    }

    @Test
    fun noDailyLimit() {
        val hoursAgo = (1..40).map { now - it * 60 * minute }
        assertTrue(canShowInterstitial(now, hoursAgo, isPro = false))
    }

    @Test
    fun timesInTheFutureAreIgnored() {
        assertTrue(canShowInterstitial(now, listOf(now + minute, now + 2 * minute), isPro = false))
    }

    @Test
    fun recordingKeepsOnlyRecentTimesPlusTheNewOne() {
        val history = listOf(now - 90 * minute, now - 10 * minute, now + minute)
        assertEquals(listOf(now - 10 * minute, now), withInterstitialShown(now, history))
    }
}

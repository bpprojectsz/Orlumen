package com.zdmgold.orlumen.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPolicyTest {
    private val day = 86_400_000L
    private val now = 100_000L * day
    private val installed = now - 30 * day
    private val ready = ReviewProgress(sessions = 10, sessionDays = setOf(1L, 2L, 3L))

    @Test
    fun allowsWhenEverythingIsMet() {
        assertTrue(shouldRequestReview(ready, installed, now))
    }

    @Test
    fun needsTwentyOneDaysSinceInstall() {
        assertFalse(shouldRequestReview(ready, now - 20 * day, now))
        assertTrue(shouldRequestReview(ready, now - 21 * day, now))
    }

    @Test
    fun unknownOrFutureInstallTimeNeverAllows() {
        assertFalse(shouldRequestReview(ready, 0L, now))
        assertFalse(shouldRequestReview(ready, now + day, now))
    }

    @Test
    fun needsTenSessionsOnThreeDays() {
        assertFalse(shouldRequestReview(ready.copy(sessions = 9), installed, now))
        assertFalse(shouldRequestReview(ready.copy(sessionDays = setOf(1L, 2L)), installed, now))
    }

    @Test
    fun retriesOnlyAfterSixtyDaysAndAtMostThreeTimes() {
        val once = ready.copy(attempts = 1, lastAttemptMillis = now - 59 * day)
        assertFalse(shouldRequestReview(once, installed, now))
        assertTrue(shouldRequestReview(once.copy(lastAttemptMillis = now - 60 * day), installed, now))
        val third = ready.copy(attempts = 3, lastAttemptMillis = now - 90 * day)
        assertFalse(shouldRequestReview(third, installed, now))
    }

    @Test
    fun aLastAttemptInTheFutureBlocksUntilTheClockCatchesUp() {
        val odd = ready.copy(attempts = 1, lastAttemptMillis = now + day)
        assertFalse(shouldRequestReview(odd, installed, now))
    }

    @Test
    fun sessionsStopGrowingAtTenAndKeepTheNewestThreeDays() {
        var progress = ReviewProgress()
        (1..12).forEach { progress = withQualifyingSession(progress, it.toLong()) }
        assertEquals(10, progress.sessions)
        assertEquals(setOf(10L, 11L, 12L), progress.sessionDays)
    }

    @Test
    fun theSameDayIsCountedOnce() {
        val progress = withQualifyingSession(withQualifyingSession(ReviewProgress(), 5L), 5L)
        assertEquals(2, progress.sessions)
        assertEquals(setOf(5L), progress.sessionDays)
    }

    @Test
    fun anAttemptIsRecorded() {
        val progress = withReviewAttempt(ReviewProgress(), now)
        assertEquals(1, progress.attempts)
        assertEquals(now, progress.lastAttemptMillis)
    }
}

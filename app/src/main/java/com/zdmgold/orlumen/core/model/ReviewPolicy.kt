package com.zdmgold.orlumen.core.model

private const val MILLIS_PER_DAY = 86_400_000L

/** The edge lights must run this long without a break to count as one session. */
const val REVIEW_MIN_SESSION_MS = 60_000L

const val REVIEW_MIN_SESSIONS = 10
const val REVIEW_MIN_DAYS = 3
const val REVIEW_MIN_INSTALL_DAYS = 21
const val REVIEW_RETRY_GAP_DAYS = 60
const val REVIEW_MAX_ATTEMPTS = 3

/**
 * What the review prompt rules need to remember. [sessionDays] holds the newest distinct days (as epoch days) on
 * which a qualifying session ended, never more than [REVIEW_MIN_DAYS] of them, and [sessions] stops growing at
 * [REVIEW_MIN_SESSIONS].
 */
data class ReviewProgress(
    val sessions: Int = 0,
    val sessionDays: Set<Long> = emptySet(),
    val attempts: Int = 0,
    val lastAttemptMillis: Long = 0L
)

/**
 * Whether Google's review popup may be requested now: installed for at least 21 days, at least 10 qualifying sessions
 * on at least 3 different days, at most 3 attempts, and at least 60 days since the last attempt. An unknown install
 * time or a clock that moved backwards means no.
 */
fun shouldRequestReview(progress: ReviewProgress, installedAtMillis: Long, nowMillis: Long): Boolean {
    val installedLongEnough = installedAtMillis in 1..nowMillis &&
        nowMillis - installedAtMillis >= REVIEW_MIN_INSTALL_DAYS * MILLIS_PER_DAY
    val usedEnough = progress.sessions >= REVIEW_MIN_SESSIONS && progress.sessionDays.size >= REVIEW_MIN_DAYS
    val attemptsLeft = progress.attempts < REVIEW_MAX_ATTEMPTS
    val sinceLastAttempt = nowMillis - progress.lastAttemptMillis
    val retryAllowed = progress.lastAttemptMillis in 1..nowMillis &&
        sinceLastAttempt >= REVIEW_RETRY_GAP_DAYS * MILLIS_PER_DAY
    val spacedOut = progress.attempts == 0 || retryAllowed
    return installedLongEnough && usedEnough && attemptsLeft && spacedOut
}

/** Progress after one more qualifying session that ended on [epochDay]. */
fun withQualifyingSession(progress: ReviewProgress, epochDay: Long): ReviewProgress = progress.copy(
    sessions = minOf(progress.sessions + 1, REVIEW_MIN_SESSIONS),
    sessionDays = (progress.sessionDays + epochDay).toSortedSet().toList().takeLast(REVIEW_MIN_DAYS).toSet()
)

/** Progress after the popup was requested at [nowMillis]. */
fun withReviewAttempt(progress: ReviewProgress, nowMillis: Long): ReviewProgress =
    progress.copy(attempts = progress.attempts + 1, lastAttemptMillis = nowMillis)

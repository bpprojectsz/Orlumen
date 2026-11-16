package com.zdmgold.orlumen.core.model

private const val MILLIS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60

/** Minimum time between two full-screen ads. */
const val INTERSTITIAL_MIN_GAP_MS = 5 * MILLIS_PER_MINUTE

/** Most full-screen ads allowed in any rolling hour. There is no daily limit. */
const val INTERSTITIAL_MAX_PER_HOUR = 8

private const val WINDOW_MS = MINUTES_PER_HOUR * MILLIS_PER_MINUTE

/** The recorded show times that still count: inside the last hour and not in the future (clock changes). */
fun recentInterstitialTimes(nowMillis: Long, shownMillis: List<Long>): List<Long> =
    shownMillis.filter { it <= nowMillis && nowMillis - it < WINDOW_MS }

/** Whether a full-screen ad may be shown now. Pro owners never see one. */
fun canShowInterstitial(nowMillis: Long, shownMillis: List<Long>, isPro: Boolean): Boolean {
    if (isPro) return false
    val recent = recentInterstitialTimes(nowMillis, shownMillis)
    val last = recent.maxOrNull()
    val spacedOut = last == null || nowMillis - last >= INTERSTITIAL_MIN_GAP_MS
    return spacedOut && recent.size < INTERSTITIAL_MAX_PER_HOUR
}

/** The history to store after an ad was shown at [nowMillis]: only recent entries plus the new one. */
fun withInterstitialShown(nowMillis: Long, shownMillis: List<Long>): List<Long> =
    recentInterstitialTimes(nowMillis, shownMillis) + nowMillis

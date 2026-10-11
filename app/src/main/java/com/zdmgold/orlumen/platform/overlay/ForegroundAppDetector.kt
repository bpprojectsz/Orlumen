package com.zdmgold.orlumen.platform.overlay

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import javax.inject.Inject
import javax.inject.Singleton

private const val INITIAL_LOOKBACK_MS = 10 * 60 * 1000L

@Singleton
class ForegroundAppDetector @Inject constructor(
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager
) {
    private var lastKnownPackage: String? = null
    private var lastQueryMillis = 0L
    private val categories = HashMap<String, AppCategoryHint>()

    fun reset() {
        lastKnownPackage = null
        lastQueryMillis = 0L
    }

    fun currentForegroundPackage(nowMillis: Long = System.currentTimeMillis()): String? {
        val from = if (lastQueryMillis == 0L) nowMillis - INITIAL_LOOKBACK_MS else lastQueryMillis
        val events = usageStatsManager.queryEvents(from, nowMillis)
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) lastKnownPackage = event.packageName
        }
        lastQueryMillis = nowMillis
        return lastKnownPackage
    }

    fun categoryOf(packageName: String): AppCategoryHint = categories.getOrPut(packageName) {
        resolveCategory(packageName)
    }

    @Suppress("DEPRECATION")
    private fun resolveCategory(packageName: String): AppCategoryHint {
        val info = runCatching { packageManager.getApplicationInfo(packageName, 0) }.getOrNull()
            ?: return AppCategoryHint.OTHER
        val isGame = info.category == ApplicationInfo.CATEGORY_GAME ||
            (info.flags and ApplicationInfo.FLAG_IS_GAME) != 0
        return when {
            isGame -> AppCategoryHint.GAME
            info.category == ApplicationInfo.CATEGORY_VIDEO -> AppCategoryHint.VIDEO
            else -> AppCategoryHint.OTHER
        }
    }
}

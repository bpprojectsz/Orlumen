package com.zdmgold.orlumen.platform.permissions

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class PermissionState(
    val recordAudio: Boolean,
    val overlay: Boolean,
    val notificationAccess: Boolean,
    val usageAccess: Boolean,
    val postNotifications: Boolean,
    val batteryUnrestricted: Boolean
)

@Singleton
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val powerManager: PowerManager
) {
    fun hasRecordAudio(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    fun hasNotificationAccess(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun canPostNotifications(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun isBatteryUnrestricted(): Boolean = powerManager.isIgnoringBatteryOptimizations(context.packageName)

    fun snapshot(): PermissionState = PermissionState(
        recordAudio = hasRecordAudio(),
        overlay = canDrawOverlays(),
        notificationAccess = hasNotificationAccess(),
        usageAccess = hasUsageAccess(),
        postNotifications = canPostNotifications(),
        batteryUnrestricted = isBatteryUnrestricted()
    )
}

object PermissionIntents {
    fun overlaySettings(context: Context): Intent =
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))

    fun usageAccessSettings(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun notificationListenerSettings(): Intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

    fun batterySettings(): Intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

    fun appDetails(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
}

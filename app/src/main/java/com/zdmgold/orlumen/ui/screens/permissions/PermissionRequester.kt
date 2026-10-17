package com.zdmgold.orlumen.ui.screens.permissions

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.PermissionKind
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.platform.permissions.PermissionIntents
import com.zdmgold.orlumen.ui.components.findActivity
import com.zdmgold.orlumen.ui.components.rationaleFor

private const val TAG = "PermissionRequester"
private const val PLAY_STORE_PACKAGE = "com.android.vending"

/**
 * Sends the user to the quickest place to grant each permission: a system dialog where Android
 * has one, otherwise Orlumen's own page in settings. One instance serves a whole screen.
 */
@Stable
class PermissionRequester(private val context: Context) {
    var onChanged: () -> Unit = {}
    var audioLauncher: ManagedActivityResultLauncher<String, Boolean>? = null
    var notificationLauncher: ManagedActivityResultLauncher<String, Boolean>? = null
    var helpKind by mutableStateOf<PermissionKind?>(null)
        private set
    val installedFromPlayStore: Boolean
        get() = installedFromPlay()

    private var afterNotifications: (() -> Unit)? = null

    /** [askFirst] shows the restricted-settings help before opening settings, for callers without inline help. */
    fun request(kind: PermissionKind, askFirst: Boolean = true) {
        when (kind) {
            PermissionKind.MICROPHONE -> audioLauncher?.launch(Manifest.permission.RECORD_AUDIO)
            PermissionKind.NOTIFICATIONS -> requestNotifications()
            PermissionKind.OVERLAY ->
                if (askFirst && needsRestrictedHelp(kind)) {
                    helpKind = kind
                } else {
                    openSettings(PermissionIntents.overlaySettings(context))
                }
            PermissionKind.NOTIFICATION_ACCESS ->
                if (askFirst && needsRestrictedHelp(kind)) helpKind = kind else openNotificationAccess()
            PermissionKind.USAGE_ACCESS -> openSettings(PermissionIntents.usageAccessSettings(context))
            PermissionKind.BATTERY -> openSettings(PermissionIntents.batterySettings())
            PermissionKind.SCREEN_CAPTURE -> Unit
        }
    }

    /** Asks for the notification permission once, just before something that shows a notification. */
    fun withNotifications(block: () -> Unit) {
        val needed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(context).areNotificationsEnabled()
        val launcher = notificationLauncher
        if (needed && launcher != null) {
            afterNotifications = block
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            block()
        }
    }

    internal fun onRuntimeResult(permission: String, granted: Boolean) {
        onChanged()
        if (permission == Manifest.permission.POST_NOTIFICATIONS) {
            afterNotifications?.invoke()
            afterNotifications = null
            return
        }
        val activity = context.findActivity()
        val blockedForGood =
            activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        if (!granted && blockedForGood) openSettings(PermissionIntents.appDetails(context))
    }

    fun confirmHelp() {
        val kind = helpKind
        helpKind = null
        if (kind != null) request(kind, askFirst = false)
    }

    fun openAppInfoFromHelp() {
        helpKind = null
        openAppInfo()
    }

    fun dismissHelp() {
        helpKind = null
    }

    fun openAppInfo() = openSettings(PermissionIntents.appDetails(context))

    private fun needsRestrictedHelp(kind: PermissionKind): Boolean =
        (kind == PermissionKind.OVERLAY || kind == PermissionKind.NOTIFICATION_ACCESS) && !installedFromPlay()

    private fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openSettings(PermissionIntents.appNotificationSettings(context))
        }
    }

    private fun openNotificationAccess() = openSettings(PermissionIntents.notificationListenerDetail(context))

    private fun openSettings(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (missing: ActivityNotFoundException) {
            Log.w(TAG, "Settings page is not available on this phone", missing)
            context.startActivity(PermissionIntents.appDetails(context))
        }
    }

    /** Android blocks some settings for apps installed outside the Play Store, which needs extra steps. */
    @Suppress("DEPRECATION")
    private fun installedFromPlay(): Boolean {
        val manager = context.packageManager
        val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { manager.getInstallSourceInfo(context.packageName).installingPackageName }.getOrNull()
        } else {
            manager.getInstallerPackageName(context.packageName)
        }
        return installer == PLAY_STORE_PACKAGE
    }
}

@Composable
fun rememberPermissionRequester(onChanged: () -> Unit): PermissionRequester {
    val context = LocalContext.current
    val requester = remember { PermissionRequester(context) }
    requester.onChanged = onChanged
    requester.audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        requester.onRuntimeResult(Manifest.permission.RECORD_AUDIO, it)
    }
    requester.notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        requester.onRuntimeResult(Manifest.permission.POST_NOTIFICATIONS, it)
    }
    return requester
}

/** Shows the extra how-to for notification access on sideloaded installs. Place once per screen. */
@Composable
fun PermissionRequesterDialogs(requester: PermissionRequester) {
    val kind = requester.helpKind ?: return
    val colors = OrlumenTheme.colors
    val rationale = rationaleFor(kind)
    AlertDialog(
        onDismissRequest = requester::dismissHelp,
        containerColor = colors.bgElevated,
        title = {
            Text(
                text = stringResource(rationale.first),
                style = OrlumenTheme.typography.headline,
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = stringResource(rationale.second) + "\n\n" + stringResource(R.string.perm_access_help),
                style = OrlumenTheme.typography.body,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = requester::confirmHelp) {
                Text(text = stringResource(R.string.perm_open_settings), color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = requester::openAppInfoFromHelp) {
                Text(text = stringResource(R.string.perm_open_app_info), color = colors.textSecondary)
            }
        }
    )
}

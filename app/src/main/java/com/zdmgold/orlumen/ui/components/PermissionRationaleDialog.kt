package com.zdmgold.orlumen.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme

enum class PermissionKind {
    OVERLAY,
    MICROPHONE,
    NOTIFICATION_ACCESS,
    USAGE_ACCESS,
    NOTIFICATIONS,
    BATTERY,
    SCREEN_CAPTURE
}

internal class Rationale(@StringRes val titleRes: Int, @StringRes val messageRes: Int)

internal fun rationaleFor(kind: PermissionKind): Rationale = when (kind) {
    PermissionKind.OVERLAY -> Rationale(R.string.perm_overlay_title, R.string.perm_overlay_message)
    PermissionKind.MICROPHONE -> Rationale(R.string.perm_microphone_title, R.string.perm_microphone_message)
    PermissionKind.NOTIFICATION_ACCESS ->
        Rationale(R.string.perm_notification_access_title, R.string.perm_notification_access_message)
    PermissionKind.USAGE_ACCESS -> Rationale(R.string.perm_usage_access_title, R.string.perm_usage_access_message)
    PermissionKind.NOTIFICATIONS ->
        Rationale(R.string.perm_notifications_title, R.string.perm_notifications_message)
    PermissionKind.BATTERY -> Rationale(R.string.perm_battery_title, R.string.perm_battery_message)
    PermissionKind.SCREEN_CAPTURE ->
        Rationale(R.string.perm_screen_capture_title, R.string.perm_screen_capture_message)
}

/** Explains why a permission is needed before the system prompt or settings page is opened. */
@Composable
fun PermissionRationaleDialog(kind: PermissionKind, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = OrlumenTheme.colors
    val rationale = rationaleFor(kind)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bgElevated,
        title = {
            Text(
                text = stringResource(rationale.titleRes),
                style = OrlumenTheme.typography.headline,
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = stringResource(rationale.messageRes),
                style = OrlumenTheme.typography.body,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.perm_continue), color = colors.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.perm_not_now), color = colors.textSecondary)
            }
        }
    )
}

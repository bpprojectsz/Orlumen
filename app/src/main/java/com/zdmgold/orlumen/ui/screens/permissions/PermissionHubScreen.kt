package com.zdmgold.orlumen.ui.screens.permissions

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.PermissionKind
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.PermissionsViewModel
import com.zdmgold.orlumen.core.viewmodel.isGranted
import com.zdmgold.orlumen.core.viewmodel.requiredPermissionsGranted
import com.zdmgold.orlumen.platform.permissions.PermissionIntents
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.AppRow
import com.zdmgold.orlumen.ui.components.IconWrapper
import com.zdmgold.orlumen.ui.components.PermissionRationaleDialog
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.findActivity
import com.zdmgold.orlumen.ui.components.iconBack
import com.zdmgold.orlumen.ui.components.iconCheck
import com.zdmgold.orlumen.ui.components.iconWarning
import com.zdmgold.orlumen.ui.components.rationaleFor

private const val TAG = "PermissionHubScreen"

private val requiredKinds = listOf(PermissionKind.MICROPHONE, PermissionKind.OVERLAY)
private val recommendedKinds = listOf(
    PermissionKind.NOTIFICATIONS,
    PermissionKind.NOTIFICATION_ACCESS,
    PermissionKind.USAGE_ACCESS,
    PermissionKind.BATTERY
)

private fun openSettings(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (missing: ActivityNotFoundException) {
        Log.w(TAG, "Settings page is not available on this phone", missing)
        context.startActivity(PermissionIntents.appDetails(context))
    }
}

@Composable
fun PermissionHubScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    viewModel: PermissionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pending by remember { mutableStateOf<PermissionKind?>(null) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    fun afterRuntimeRequest(granted: Boolean, permission: String) {
        viewModel.refresh()
        val activity = context.findActivity()
        if (!granted && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        ) {
            openSettings(context, PermissionIntents.appDetails(context))
        }
    }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        afterRuntimeRequest(it, Manifest.permission.RECORD_AUDIO)
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        afterRuntimeRequest(it, Manifest.permission.POST_NOTIFICATIONS)
    }

    fun request(kind: PermissionKind) {
        when (kind) {
            PermissionKind.MICROPHONE -> audioLauncher.launch(Manifest.permission.RECORD_AUDIO)
            PermissionKind.NOTIFICATIONS ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    openSettings(context, PermissionIntents.appNotificationSettings(context))
                }
            PermissionKind.OVERLAY -> openSettings(context, PermissionIntents.overlaySettings(context))
            PermissionKind.NOTIFICATION_ACCESS ->
                openSettings(context, PermissionIntents.notificationListenerSettings())
            PermissionKind.USAGE_ACCESS -> openSettings(context, PermissionIntents.usageAccessSettings())
            PermissionKind.BATTERY -> openSettings(context, PermissionIntents.batterySettings())
            PermissionKind.SCREEN_CAPTURE -> Unit
        }
    }

    pending?.let { kind ->
        PermissionRationaleDialog(
            kind = kind,
            onConfirm = {
                pending = null
                request(kind)
            },
            onDismiss = { pending = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    IconWrapper(imageVector = iconBack, contentDescription = stringResource(R.string.action_back))
                }
            }
            Text(
                text = stringResource(R.string.perm_hub_title),
                style = OrlumenTheme.typography.headline,
                color = OrlumenTheme.colors.textPrimary,
                modifier = Modifier.padding(vertical = Spacing.lg)
            )
        }
        Text(
            text = stringResource(R.string.perm_hub_intro),
            style = OrlumenTheme.typography.body,
            color = OrlumenTheme.colors.textSecondary
        )
        SectionHeader(text = stringResource(R.string.perm_section_required))
        PermissionGroup(requiredKinds, state.isGrantedMap(requiredKinds)) { pending = it }
        SectionHeader(text = stringResource(R.string.perm_section_recommended))
        PermissionGroup(recommendedKinds, state.isGrantedMap(recommendedKinds)) { pending = it }
        Spacer(modifier = Modifier.height(Spacing.xl))
        AppPrimaryButton(
            text = stringResource(R.string.perm_hub_continue),
            onClick = onDone,
            enabled = requiredPermissionsGranted(state)
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

private fun com.zdmgold.orlumen.platform.permissions.PermissionState.isGrantedMap(
    kinds: List<PermissionKind>
): Map<PermissionKind, Boolean> = kinds.associateWith { isGranted(it) }

@Composable
private fun PermissionGroup(
    kinds: List<PermissionKind>,
    granted: Map<PermissionKind, Boolean>,
    onRequest: (PermissionKind) -> Unit
) {
    AppCard {
        kinds.forEach { kind ->
            val isGranted = granted[kind] == true
            AppRow(
                title = stringResource(rationaleFor(kind).first),
                subtitle = stringResource(if (isGranted) R.string.perm_status_allowed else R.string.perm_status_needed),
                leadingIcon = if (isGranted) iconCheck else iconWarning,
                showChevron = !isGranted,
                onClick = if (isGranted) null else ({ onRequest(kind) })
            )
        }
    }
}

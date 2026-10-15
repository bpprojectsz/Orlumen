package com.zdmgold.orlumen.ui.screens.diagnostics

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.PermissionKind
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Radius
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.DiagnosticHint
import com.zdmgold.orlumen.core.viewmodel.DiagnosticsUiState
import com.zdmgold.orlumen.core.viewmodel.DiagnosticsViewModel
import com.zdmgold.orlumen.core.viewmodel.MainViewModel
import com.zdmgold.orlumen.core.viewmodel.isGranted
import com.zdmgold.orlumen.platform.aod.AodActivity
import com.zdmgold.orlumen.platform.audio.SpectrumSourceKind
import com.zdmgold.orlumen.platform.overlay.RuntimeFailure
import com.zdmgold.orlumen.platform.overlay.RuntimeState
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.AppRow
import com.zdmgold.orlumen.ui.components.AppSwitchRow
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.findActivity
import com.zdmgold.orlumen.ui.components.iconCheck
import com.zdmgold.orlumen.ui.components.iconInfo
import com.zdmgold.orlumen.ui.components.iconWarning
import com.zdmgold.orlumen.ui.components.rationaleFor
import com.zdmgold.orlumen.ui.screens.permissions.PermissionRequesterDialogs
import com.zdmgold.orlumen.ui.screens.permissions.rememberPermissionRequester
import java.util.Date

private const val PERCENT = 100

@Composable
private fun runtimeText(runtime: RuntimeState): String = when (runtime) {
    RuntimeState.Idle -> stringResource(R.string.diag_state_idle)
    RuntimeState.Starting -> stringResource(R.string.diag_state_starting)
    is RuntimeState.Running -> when (runtime.source) {
        null -> stringResource(R.string.diag_state_running_aod)
        SpectrumSourceKind.GLOBAL_MIX ->
            stringResource(R.string.diag_state_running_source, stringResource(R.string.diag_source_global_mix))
        SpectrumSourceKind.PLAYBACK_CAPTURE ->
            stringResource(R.string.diag_state_running_source, stringResource(R.string.diag_source_capture))
    }
    is RuntimeState.Failed -> stringResource(R.string.diag_state_failed, failureText(runtime.reason))
}

@Composable
private fun failureText(reason: RuntimeFailure): String = stringResource(
    when (reason) {
        RuntimeFailure.OVERLAY_PERMISSION_MISSING -> R.string.diag_failure_overlay
        RuntimeFailure.AUDIO_PERMISSION_MISSING -> R.string.diag_failure_audio
        RuntimeFailure.START_NOT_ALLOWED -> R.string.diag_failure_not_allowed
        RuntimeFailure.SOURCE_UNAVAILABLE -> R.string.diag_failure_source
    }
)

@Composable
private fun hintText(hint: DiagnosticHint): String? = when (hint) {
    DiagnosticHint.NONE -> null
    DiagnosticHint.PERMISSIONS_MISSING -> stringResource(R.string.diag_hint_permissions)
    DiagnosticHint.SOURCE_FAILED -> stringResource(R.string.diag_hint_source_failed)
    DiagnosticHint.NO_SIGNAL_WHILE_PLAYING -> stringResource(R.string.diag_hint_no_signal)
}

@Composable
private fun LevelBar(level: Float, modifier: Modifier = Modifier) {
    val colors = OrlumenTheme.colors
    val shape = RoundedCornerShape(Radius.pill)
    Box(modifier = modifier.fillMaxWidth().height(Spacing.sm).clip(shape).background(colors.border)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = level.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(shape)
                .background(colors.accent)
        )
    }
}

@Composable
fun DiagnosticsScreen(
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity() as ComponentActivity
    val main: MainViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val projectionManager = remember { context.getSystemService(MediaProjectionManager::class.java) }
    val captureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                requester.withNotifications { main.startVisualizer(result.resultCode, data) }
            }
        }
    val requester = rememberPermissionRequester { viewModel.refreshPermissions() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermissions() }

    val current = state
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.diag_title),
            style = OrlumenTheme.typography.headline,
            color = OrlumenTheme.colors.textPrimary,
            modifier = Modifier.padding(vertical = Spacing.lg)
        )
        if (current != null) {
            DiagnosticsContent(
                state = current,
                onStart = { requester.withNotifications { main.startVisualizer() } },
                onStop = main::stopVisualizer,
                onStartWithCapture = { captureLauncher.launch(projectionManager.createScreenCaptureIntent()) },
                onAodToggle = main::setAodEnabled,
                onOpenAodNow = { context.startActivity(Intent(context, AodActivity::class.java)) },
                onRequestPermission = requester::request,
                onOpenAbout = onOpenAbout
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
    PermissionRequesterDialogs(requester)
}

@Composable
private fun DiagnosticsContent(
    state: DiagnosticsUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onStartWithCapture: () -> Unit,
    onAodToggle: (Boolean) -> Unit,
    onOpenAodNow: () -> Unit,
    onRequestPermission: (PermissionKind) -> Unit,
    onOpenAbout: () -> Unit
) {
    val colors = OrlumenTheme.colors
    val context = LocalContext.current
    val running = state.runtime is RuntimeState.Running || state.runtime == RuntimeState.Starting
    SectionHeader(text = stringResource(R.string.diag_section_status))
    AppCard {
        Text(text = runtimeText(state.runtime), style = OrlumenTheme.typography.body, color = colors.textPrimary)
        hintText(state.hint)?.let {
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(text = it, style = OrlumenTheme.typography.caption, color = colors.accentSecondary)
        }
    }
    Spacer(modifier = Modifier.height(Spacing.md))
    if (running) {
        AppPrimaryButton(text = stringResource(R.string.diag_action_stop), onClick = onStop)
    } else {
        AppPrimaryButton(text = stringResource(R.string.diag_action_start), onClick = onStart)
        Spacer(modifier = Modifier.height(Spacing.sm))
        AppPrimaryButton(text = stringResource(R.string.diag_action_capture), onClick = onStartWithCapture)
    }
    SectionHeader(text = stringResource(R.string.diag_section_signal))
    AppCard {
        Text(
            text = stringResource(R.string.diag_signal_level, (state.peak * PERCENT).toInt()),
            style = OrlumenTheme.typography.monospace,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        LevelBar(state.peak)
    }
    SectionHeader(text = stringResource(R.string.diag_section_music))
    AppCard {
        val title = state.nowPlaying.title
        if (title != null) {
            Text(text = title, style = OrlumenTheme.typography.body, color = colors.textPrimary)
            state.nowPlaying.artist?.let {
                Text(text = it, style = OrlumenTheme.typography.caption, color = colors.textSecondary)
            }
            Text(
                text = state.nowPlaying.sourcePackage,
                style = OrlumenTheme.typography.caption,
                color = colors.textTertiary
            )
        } else {
            Text(
                text = stringResource(R.string.diag_nothing_playing),
                style = OrlumenTheme.typography.body,
                color = colors.textSecondary
            )
            if (!state.permissions.notificationAccess) {
                Text(
                    text = stringResource(R.string.diag_notification_access_hint),
                    style = OrlumenTheme.typography.caption,
                    color = colors.accentSecondary
                )
            }
        }
    }
    SectionHeader(text = stringResource(R.string.diag_section_aod))
    AppCard {
        AppSwitchRow(
            title = stringResource(R.string.diag_aod_toggle),
            checked = state.aodEnabled,
            onCheckedChange = onAodToggle,
            subtitle = stringResource(R.string.diag_aod_instructions)
        )
        Text(
            text = stringResource(if (state.aodScreenShowing) R.string.diag_aod_showing else R.string.diag_aod_hidden),
            style = OrlumenTheme.typography.caption,
            color = colors.textSecondary
        )
        Text(
            text = lastLaunchText(context, state.lastAodLaunchMillis),
            style = OrlumenTheme.typography.caption,
            color = colors.textSecondary
        )
        AppRow(
            title = stringResource(R.string.diag_aod_test),
            leadingIcon = iconInfo,
            onClick = onOpenAodNow
        )
    }
    SectionHeader(text = stringResource(R.string.diag_section_permissions))
    AppCard {
        PermissionKind.entries.filterNot { it == PermissionKind.SCREEN_CAPTURE }.forEach { kind ->
            val granted = state.permissions.isGranted(kind)
            AppRow(
                title = stringResource(rationaleFor(kind).first),
                subtitle = stringResource(if (granted) R.string.perm_status_allowed else R.string.perm_status_needed),
                leadingIcon = if (granted) iconCheck else iconWarning,
                showChevron = !granted,
                onClick = if (granted) null else ({ onRequestPermission(kind) })
            )
        }
        AppRow(title = stringResource(R.string.diag_open_about), leadingIcon = iconInfo, onClick = onOpenAbout)
    }
}

@Composable
private fun lastLaunchText(context: Context, millis: Long): String = if (millis == 0L) {
    stringResource(R.string.diag_aod_never)
} else {
    stringResource(R.string.diag_aod_last_launch, DateFormat.getTimeFormat(context).format(Date(millis)))
}

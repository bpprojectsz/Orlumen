package com.zdmgold.orlumen.ui.screens.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.DiagnosticsViewModel
import com.zdmgold.orlumen.core.viewmodel.MainViewModel
import com.zdmgold.orlumen.core.viewmodel.requiredMissing
import com.zdmgold.orlumen.platform.overlay.RuntimeState
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.AppRow
import com.zdmgold.orlumen.ui.components.AppSwitchRow
import com.zdmgold.orlumen.ui.components.ScreenHeader
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.findActivity
import com.zdmgold.orlumen.ui.components.iconEdit
import com.zdmgold.orlumen.ui.components.iconInfo
import com.zdmgold.orlumen.ui.components.iconLock
import com.zdmgold.orlumen.ui.components.iconStar
import com.zdmgold.orlumen.ui.screens.permissions.PermissionRequesterDialogs
import com.zdmgold.orlumen.ui.screens.permissions.rememberPermissionRequester

class HomeActions(
    val onNeedPermissions: () -> Unit,
    val onOpenVisualizers: () -> Unit,
    val onOpenAod: () -> Unit,
    val onOpenPalettes: () -> Unit,
    val onOpenDiagnostics: () -> Unit,
    val onOpenAbout: () -> Unit
)

@Composable
fun HomeScreen(
    autoStart: Boolean,
    onAutoStartHandled: () -> Unit,
    actions: HomeActions,
    modifier: Modifier = Modifier,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val activity = LocalContext.current.findActivity() as ComponentActivity
    val main: MainViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val requester = rememberPermissionRequester { viewModel.refreshPermissions() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermissions() }

    val current = state
    fun startOrAskForSetup() {
        val granted = current?.permissions
        if (granted != null && requiredMissing(granted).isNotEmpty()) {
            actions.onNeedPermissions()
        } else {
            requester.withNotifications { main.startVisualizer() }
        }
    }
    LaunchedEffect(autoStart, current != null) {
        if (autoStart && current != null) {
            onAutoStartHandled()
            requester.withNotifications { main.startVisualizer() }
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = stringResource(R.string.app_name))
        if (current != null) {
            val running = current.runtime is RuntimeState.Running || current.runtime == RuntimeState.Starting
            AppCard {
                Text(
                    text = stringResource(
                        when {
                            current.runtime is RuntimeState.Running -> R.string.home_status_on
                            current.runtime == RuntimeState.Starting -> R.string.home_status_starting
                            else -> R.string.home_status_off
                        }
                    ),
                    style = OrlumenTheme.typography.headline,
                    color = OrlumenTheme.colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            AppPrimaryButton(
                text = stringResource(if (running) R.string.home_stop else R.string.home_start),
                onClick = { if (running) main.stopVisualizer() else startOrAskForSetup() }
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard {
                AppSwitchRow(
                    title = stringResource(R.string.home_aod_title),
                    subtitle = stringResource(R.string.home_aod_subtitle),
                    checked = current.aodEnabled,
                    onCheckedChange = main::setAodEnabled
                )
            }
            SectionHeader(text = stringResource(R.string.home_section_customize))
            AppCard {
                AppRow(
                    title = stringResource(R.string.home_edge_lights),
                    subtitle = stringResource(R.string.home_edge_lights_sub),
                    leadingIcon = iconStar,
                    onClick = actions.onOpenVisualizers
                )
                AppRow(
                    title = stringResource(R.string.home_aod_clock),
                    subtitle = stringResource(R.string.home_aod_clock_sub),
                    leadingIcon = iconLock,
                    onClick = actions.onOpenAod
                )
                AppRow(
                    title = stringResource(R.string.home_colors),
                    subtitle = stringResource(R.string.home_colors_sub),
                    leadingIcon = iconEdit,
                    onClick = actions.onOpenPalettes
                )
            }
            SectionHeader(text = stringResource(R.string.home_section_tools))
            AppCard {
                AppRow(
                    title = stringResource(R.string.home_diagnostics),
                    subtitle = stringResource(R.string.home_diagnostics_sub),
                    leadingIcon = iconInfo,
                    onClick = actions.onOpenDiagnostics
                )
                AppRow(
                    title = stringResource(R.string.home_about),
                    leadingIcon = iconInfo,
                    onClick = actions.onOpenAbout
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
    PermissionRequesterDialogs(requester)
}

package com.zdmgold.orlumen.ui.screens.home

import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.NowPlaying
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.AodViewModel
import com.zdmgold.orlumen.core.viewmodel.ConsentViewModel
import com.zdmgold.orlumen.core.viewmodel.DiagnosticsViewModel
import com.zdmgold.orlumen.core.viewmodel.MainViewModel
import com.zdmgold.orlumen.core.viewmodel.VisualizerViewModel
import com.zdmgold.orlumen.core.viewmodel.requiredMissing
import com.zdmgold.orlumen.designs.visualizer.VisualizerRegistry
import com.zdmgold.orlumen.platform.overlay.RuntimeState
import com.zdmgold.orlumen.ui.components.AodPreviewCanvas
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.AppRow
import com.zdmgold.orlumen.ui.components.AppSwitchRow
import com.zdmgold.orlumen.ui.components.IconWrapper
import com.zdmgold.orlumen.ui.components.PaletteSwatch
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.VisualizerPreviewCanvas
import com.zdmgold.orlumen.ui.components.findActivity
import com.zdmgold.orlumen.ui.components.iconInfo
import com.zdmgold.orlumen.ui.components.iconSettings
import com.zdmgold.orlumen.ui.components.iconStar
import com.zdmgold.orlumen.ui.components.paletteColors
import com.zdmgold.orlumen.ui.components.paletteColorsArgb
import com.zdmgold.orlumen.ui.screens.permissions.PermissionRequesterDialogs
import com.zdmgold.orlumen.ui.screens.permissions.rememberPermissionRequester

private const val HERO_PREVIEW_DP = 96
private const val TILE_PREVIEW_DP = 150
private const val TILE_PREVIEW_FRACTION = 0.62f

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
    val consent: ConsentViewModel = hiltViewModel(viewModelStoreOwner = activity)
    LaunchedEffect(Unit) { consent.gatherConsent(activity) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val requester = rememberPermissionRequester { viewModel.refreshPermissions() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermissions() }
    val visualizer: VisualizerViewModel = hiltViewModel()
    val aod: AodViewModel = hiltViewModel()
    val look by visualizer.uiState.collectAsStateWithLifecycle()
    val clock by aod.uiState.collectAsStateWithLifecycle()
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)

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
        HomeHeader(onOpenSettings = actions.onOpenSettings)
        if (current != null) {
            val running = current.runtime is RuntimeState.Running || current.runtime == RuntimeState.Starting
            val statusRes = when {
                current.runtime is RuntimeState.Running -> R.string.home_status_on
                current.runtime == RuntimeState.Starting -> R.string.home_status_starting
                else -> R.string.home_status_off
            }
            val lights = look
            val clocks = clock
            HeroCard(
                statusRes = statusRes,
                running = running,
                designNameRes = lights?.let { VisualizerRegistry.byId(it.active.designId)?.descriptor?.nameRes },
                onToggle = { if (running) main.stopVisualizer() else startOrAskForSetup() },
                preview = {
                    if (lights != null) {
                        VisualizerPreviewCanvas(
                            config = lights.active,
                            colors = paletteColors(lights.palettes, lights.active.paletteId),
                            modifier = Modifier.width(HERO_PREVIEW_DP.dp)
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            NowPlayingCard(current.nowPlaying)
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
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                HomeTile(
                    title = stringResource(R.string.home_edge_lights),
                    onClick = actions.onOpenVisualizers,
                    modifier = Modifier.weight(1f)
                ) {
                    if (lights != null) {
                        VisualizerPreviewCanvas(
                            config = lights.active,
                            colors = paletteColors(lights.palettes, lights.active.paletteId),
                            modifier = Modifier.fillMaxWidth(TILE_PREVIEW_FRACTION)
                        )
                    }
                }
                HomeTile(
                    title = stringResource(R.string.home_aod_clock),
                    onClick = actions.onOpenAod,
                    modifier = Modifier.weight(1f)
                ) {
                    if (clocks != null) {
                        AodPreviewCanvas(
                            config = clocks.active,
                            colors = paletteColorsArgb(clocks.palettes, clocks.active.paletteId),
                            use24Hour = use24Hour,
                            modifier = Modifier.fillMaxWidth(TILE_PREVIEW_FRACTION)
                        )
                    }
                }
                HomeTile(
                    title = stringResource(R.string.home_colors),
                    onClick = actions.onOpenPalettes,
                    modifier = Modifier.weight(1f)
                ) {
                    if (lights != null) {
                        PaletteSwatch(colors = paletteColorsArgb(lights.palettes, lights.active.paletteId))
                    }
                }
            }
            SectionHeader(text = stringResource(R.string.home_section_tools))
            AppCard {
                AppRow(
                    title = stringResource(R.string.home_remove_ads),
                    subtitle = stringResource(R.string.home_remove_ads_sub),
                    leadingIcon = iconStar,
                    onClick = actions.onOpenPaywall
                )
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

@Composable
private fun HomeHeader(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = OrlumenTheme.typography.headline,
            color = OrlumenTheme.colors.textPrimary,
            modifier = Modifier.padding(vertical = Spacing.lg)
        )
        IconButton(onClick = onOpenSettings) {
            IconWrapper(imageVector = iconSettings, contentDescription = stringResource(R.string.home_settings))
        }
    }
}

@Composable
private fun HeroCard(
    statusRes: Int,
    running: Boolean,
    designNameRes: Int?,
    onToggle: () -> Unit,
    preview: @Composable () -> Unit
) {
    AppCard {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            preview()
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = stringResource(statusRes),
                    style = OrlumenTheme.typography.headline,
                    color = OrlumenTheme.colors.textPrimary
                )
                if (designNameRes != null) {
                    Text(
                        text = stringResource(designNameRes),
                        style = OrlumenTheme.typography.caption,
                        color = OrlumenTheme.colors.textSecondary
                    )
                }
                AppPrimaryButton(
                    text = stringResource(if (running) R.string.home_stop else R.string.home_start),
                    onClick = onToggle
                )
            }
        }
    }
}

@Composable
private fun NowPlayingCard(nowPlaying: NowPlaying) {
    AppCard {
        Text(
            text = nowPlaying.title ?: stringResource(R.string.diag_nothing_playing),
            style = OrlumenTheme.typography.body,
            color = OrlumenTheme.colors.textPrimary
        )
        nowPlaying.artist?.let {
            Text(text = it, style = OrlumenTheme.typography.caption, color = OrlumenTheme.colors.textSecondary)
        }
    }
}

@Composable
private fun HomeTile(title: String, onClick: () -> Unit, modifier: Modifier, preview: @Composable () -> Unit) {
    AppCard(modifier = modifier, onClick = onClick) {
        Box(
            modifier = Modifier.fillMaxWidth().height(TILE_PREVIEW_DP.dp),
            contentAlignment = Alignment.Center
        ) { preview() }
        Text(
            text = title,
            style = OrlumenTheme.typography.caption,
            color = OrlumenTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)
        )
    }
}

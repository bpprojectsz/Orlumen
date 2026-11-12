package com.zdmgold.orlumen.ui.screens.home

import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.NowPlaying
import com.zdmgold.orlumen.core.model.ThemeMode
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.AodViewModel
import com.zdmgold.orlumen.core.viewmodel.ConsentViewModel
import com.zdmgold.orlumen.core.viewmodel.DiagnosticsViewModel
import com.zdmgold.orlumen.core.viewmodel.MainViewModel
import com.zdmgold.orlumen.core.viewmodel.SettingsViewModel
import com.zdmgold.orlumen.core.viewmodel.VisualizerViewModel
import com.zdmgold.orlumen.core.viewmodel.requiredMissing
import com.zdmgold.orlumen.designs.visualizer.VisualizerRegistry
import com.zdmgold.orlumen.platform.overlay.RuntimeState
import com.zdmgold.orlumen.ui.components.AodPreviewCanvas
import com.zdmgold.orlumen.ui.components.BrandFontFamily
import com.zdmgold.orlumen.ui.components.ChevronBadge
import com.zdmgold.orlumen.ui.components.Glyph
import com.zdmgold.orlumen.ui.components.GlyphIcon
import com.zdmgold.orlumen.ui.components.GradientButton
import com.zdmgold.orlumen.ui.components.IconChip
import com.zdmgold.orlumen.ui.components.IconWrapper
import com.zdmgold.orlumen.ui.components.PaletteSwatch
import com.zdmgold.orlumen.ui.components.PremiumCard
import com.zdmgold.orlumen.ui.components.RingChip
import com.zdmgold.orlumen.ui.components.ShrinkText
import com.zdmgold.orlumen.ui.components.VisualizerPreviewCanvas
import com.zdmgold.orlumen.ui.components.Wordmark
import com.zdmgold.orlumen.ui.components.chipIconTint
import com.zdmgold.orlumen.ui.components.findActivity
import com.zdmgold.orlumen.ui.components.iconCheck
import com.zdmgold.orlumen.ui.components.iconInfo
import com.zdmgold.orlumen.ui.components.iconSettings
import com.zdmgold.orlumen.ui.components.iconStar
import com.zdmgold.orlumen.ui.components.openLanguageSettings
import com.zdmgold.orlumen.ui.components.paletteColors
import com.zdmgold.orlumen.ui.components.paletteColorsArgb
import com.zdmgold.orlumen.ui.screens.permissions.PermissionRequesterDialogs
import com.zdmgold.orlumen.ui.screens.permissions.rememberPermissionRequester

private const val HERO_PREVIEW_DP = 72
private const val TILE_PREVIEW_DP = 130
private const val TILE_PREVIEW_FRACTION = 0.62f

@Composable
fun HomeScreen(
    autoStart: Boolean,
    onAutoStartHandled: () -> Unit,
    actions: HomeActions,
    modifier: Modifier = Modifier,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity() as ComponentActivity
    val main: MainViewModel = hiltViewModel(viewModelStoreOwner = activity)
    val consent: ConsentViewModel = hiltViewModel(viewModelStoreOwner = activity)
    LaunchedEffect(Unit) { consent.gatherConsent(activity) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mainState by main.uiState.collectAsStateWithLifecycle()
    val settings: SettingsViewModel = hiltViewModel()
    val prefs by settings.preferences.collectAsStateWithLifecycle()
    val requester = rememberPermissionRequester { viewModel.refreshPermissions() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermissions() }
    val visualizer: VisualizerViewModel = hiltViewModel()
    val aod: AodViewModel = hiltViewModel()
    val look by visualizer.uiState.collectAsStateWithLifecycle()
    val clock by aod.uiState.collectAsStateWithLifecycle()
    val use24Hour = DateFormat.is24HourFormat(context)

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
        HomeHeader(
            themeMode = prefs.themeMode,
            onThemeChange = settings::setThemeMode,
            onOpenLanguage = { openLanguageSettings(context) },
            onOpenSettings = actions.onOpenSettings
        )
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
            AodCard(checked = current.aodEnabled, onCheckedChange = main::setAodEnabled)
            SectionLabel(text = stringResource(R.string.home_section_customize))
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
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                HalfCard(
                    title = stringResource(R.string.home_settings),
                    subtitle = null,
                    onClick = actions.onOpenSettings,
                    strong = false,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) { IconWrapper(imageVector = iconSettings, contentDescription = null) }
                HalfCard(
                    title = stringResource(R.string.home_premium),
                    subtitle = stringResource(
                        if (mainState?.isPro == true) R.string.home_premium_owned else R.string.home_premium_sub
                    ),
                    onClick = actions.onOpenPaywall,
                    strong = true,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) { IconWrapper(imageVector = iconStar, contentDescription = null) }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            WideCard(
                title = stringResource(R.string.home_diagnostics),
                subtitle = stringResource(R.string.home_diagnostics_sub),
                onClick = actions.onOpenDiagnostics
            ) { GlyphIcon(Glyph.PULSE, chipIconTint()) }
            Spacer(modifier = Modifier.height(Spacing.md))
            WideCard(
                title = stringResource(R.string.home_about),
                subtitle = null,
                onClick = actions.onOpenAbout
            ) { IconWrapper(imageVector = iconInfo, contentDescription = null) }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
    PermissionRequesterDialogs(requester)
}

private fun themeLabel(mode: ThemeMode) = when (mode) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.DARK -> R.string.settings_theme_dark
    ThemeMode.LIGHT -> R.string.settings_theme_light
}

@Composable
private fun HomeHeader(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val tint = OrlumenTheme.colors.textPrimary
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Wordmark()
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Box {
                RingChip(onClick = { menuOpen = true }, contentDescription = stringResource(R.string.home_theme)) {
                    val glyph = when (themeMode) {
                        ThemeMode.SYSTEM -> Glyph.AUTO
                        ThemeMode.DARK -> Glyph.MOON
                        ThemeMode.LIGHT -> Glyph.SUN
                    }
                    GlyphIcon(glyph = glyph, tint = tint)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    ThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(text = stringResource(themeLabel(mode))) },
                            onClick = {
                                onThemeChange(mode)
                                menuOpen = false
                            },
                            trailingIcon = {
                                if (mode == themeMode) IconWrapper(imageVector = iconCheck, contentDescription = null)
                            }
                        )
                    }
                }
            }
            RingChip(onClick = onOpenLanguage, contentDescription = stringResource(R.string.settings_language)) {
                GlyphIcon(glyph = Glyph.GLOBE, tint = tint)
            }
            RingChip(onClick = onOpenSettings, contentDescription = stringResource(R.string.home_settings)) {
                IconWrapper(imageVector = iconSettings, contentDescription = null)
            }
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
    PremiumCard(strong = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            preview()
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = stringResource(statusRes),
                    color = OrlumenTheme.colors.textPrimary,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (designNameRes != null) DesignPill(name = stringResource(designNameRes))
                GradientButton(
                    text = stringResource(if (running) R.string.home_stop else R.string.home_start),
                    onClick = onToggle
                )
            }
        }
    }
}

@Composable
private fun DesignPill(name: String) {
    val colors = OrlumenTheme.colors
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.accent.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = name.uppercase(),
            color = colors.accent,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun NowPlayingCard(nowPlaying: NowPlaying) {
    val colors = OrlumenTheme.colors
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            IconChip { GlyphIcon(Glyph.NOTE, chipIconTint()) }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nowPlaying.title ?: stringResource(R.string.diag_nothing_playing),
                    color = colors.textPrimary,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                nowPlaying.artist?.let {
                    Text(
                        text = it,
                        style = OrlumenTheme.typography.caption,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                listOf(8, 15, 11, 6).forEach { bar ->
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(bar.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.45f))
                    )
                }
            }
        }
    }
}

@Composable
private fun AodCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = OrlumenTheme.colors
    PremiumCard {
        Row(
            modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            IconChip { GlyphIcon(Glyph.CLOCK, chipIconTint()) }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_aod_title),
                    color = colors.textPrimary,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                Text(
                    text = stringResource(R.string.home_aod_subtitle),
                    style = OrlumenTheme.typography.caption,
                    color = colors.textSecondary
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.accent,
                    uncheckedThumbColor = colors.textTertiary,
                    uncheckedTrackColor = colors.bgSurface,
                    uncheckedBorderColor = colors.border
                )
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = OrlumenTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            text = text.uppercase(),
            color = colors.textSecondary,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.5.sp,
            letterSpacing = 1.6.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(colors.accent.copy(alpha = 0.5f), Color.Transparent)))
        )
    }
}

@Composable
private fun HomeTile(title: String, onClick: () -> Unit, modifier: Modifier, preview: @Composable () -> Unit) {
    PremiumCard(modifier = modifier, onClick = onClick) {
        Box(modifier = Modifier.fillMaxWidth().height(TILE_PREVIEW_DP.dp), contentAlignment = Alignment.Center) {
            preview()
        }
        Text(
            text = title,
            color = OrlumenTheme.colors.textPrimary,
            fontFamily = BrandFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            minLines = 2,
            maxLines = 2,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)
        )
    }
}

@Composable
private fun HalfCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    strong: Boolean,
    modifier: Modifier,
    icon: @Composable () -> Unit
) {
    PremiumCard(modifier = modifier, onClick = onClick, strong = strong) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                IconChip(size = 32.dp, warm = strong) { icon() }
                Column(modifier = Modifier.weight(1f)) {
                    ShrinkText(text = title, color = OrlumenTheme.colors.textPrimary)
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = OrlumenTheme.typography.caption,
                            color = OrlumenTheme.colors.textSecondary,
                            fontSize = 10.5.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WideCard(title: String, subtitle: String?, onClick: () -> Unit, icon: @Composable () -> Unit) {
    PremiumCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            IconChip { icon() }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = OrlumenTheme.colors.textPrimary,
                    fontFamily = BrandFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = OrlumenTheme.typography.caption,
                        color = OrlumenTheme.colors.textSecondary,
                        maxLines = 2
                    )
                }
            }
            ChevronBadge()
        }
    }
}

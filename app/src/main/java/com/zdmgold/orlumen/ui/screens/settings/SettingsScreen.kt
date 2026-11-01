package com.zdmgold.orlumen.ui.screens.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.AodHostMode
import com.zdmgold.orlumen.core.model.AudioSourceMode
import com.zdmgold.orlumen.core.model.ThemeMode
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.SettingsViewModel
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppChip
import com.zdmgold.orlumen.ui.components.AppRow
import com.zdmgold.orlumen.ui.components.AppSwitchRow
import com.zdmgold.orlumen.ui.components.ScreenHeader
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.iconInfo
import com.zdmgold.orlumen.ui.components.iconLock
import com.zdmgold.orlumen.ui.components.iconNotifications
import com.zdmgold.orlumen.ui.components.iconStar

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = stringResource(R.string.settings_title), onBack = onBack)

        SectionHeader(text = stringResource(R.string.settings_section_appearance))
        OptionChips(
            options = listOf(
                ThemeMode.SYSTEM to R.string.settings_theme_system,
                ThemeMode.DARK to R.string.settings_theme_dark,
                ThemeMode.LIGHT to R.string.settings_theme_light
            ),
            selected = prefs.themeMode,
            onSelect = viewModel::setThemeMode
        )

        SectionHeader(text = stringResource(R.string.home_edge_lights))
        AppCard {
            AppSwitchRow(
                title = stringResource(R.string.settings_hide_fullscreen),
                subtitle = stringResource(R.string.settings_hide_fullscreen_sub),
                checked = prefs.hideOverFullscreen,
                onCheckedChange = viewModel::setHideOverFullscreen
            )
            AppSwitchRow(
                title = stringResource(R.string.settings_dim_background),
                subtitle = stringResource(R.string.settings_dim_background_sub),
                checked = prefs.dimBackground,
                onCheckedChange = viewModel::setDimBackground
            )
            AppSwitchRow(
                title = stringResource(R.string.settings_keep_screen_on),
                checked = prefs.keepScreenOn,
                onCheckedChange = viewModel::setKeepScreenOn
            )
            AppRow(
                title = stringResource(R.string.settings_visualizer_apps),
                subtitle = stringResource(R.string.settings_visualizer_apps_sub),
                leadingIcon = iconStar,
                onClick = actions.onOpenVisualizerApps
            )
        }

        SectionHeader(text = stringResource(R.string.settings_section_audio))
        OptionChips(
            options = listOf(
                AudioSourceMode.AUTO to R.string.settings_audio_auto,
                AudioSourceMode.GLOBAL_MIX to R.string.settings_audio_global,
                AudioSourceMode.PLAYBACK_CAPTURE to R.string.settings_audio_capture
            ),
            selected = prefs.audioSourceMode,
            onSelect = viewModel::setAudioSourceMode
        )
        Hint(text = stringResource(R.string.settings_audio_hint))

        SectionHeader(text = stringResource(R.string.home_aod_title))
        OptionChips(
            options = listOf(
                AodHostMode.ACTIVITY to R.string.settings_aod_activity,
                AodHostMode.SCREENSAVER to R.string.settings_aod_screensaver
            ),
            selected = prefs.aodHostMode,
            onSelect = viewModel::setAodHostMode
        )
        Hint(text = stringResource(R.string.settings_aod_hint))

        SectionHeader(text = stringResource(R.string.settings_section_music))
        AppCard {
            AppSwitchRow(
                title = stringResource(R.string.palette_auto_album),
                subtitle = stringResource(R.string.palette_auto_album_sub),
                checked = prefs.autoApplyAlbumColors,
                onCheckedChange = viewModel::setAutoApplyAlbumColors
            )
            AppRow(
                title = stringResource(R.string.settings_music_sources),
                subtitle = stringResource(R.string.settings_music_sources_sub),
                leadingIcon = iconNotifications,
                onClick = actions.onOpenMusicSources
            )
        }

        SectionHeader(text = stringResource(R.string.settings_section_privacy))
        AppCard {
            AppRow(
                title = stringResource(R.string.settings_ad_choices),
                leadingIcon = iconLock,
                onClick = actions.onOpenConsent
            )
            AppRow(
                title = stringResource(R.string.settings_audio_privacy),
                subtitle = stringResource(R.string.settings_audio_privacy_sub),
                leadingIcon = iconInfo,
                showChevron = false
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

@Composable
private fun <T> OptionChips(options: List<Pair<T, Int>>, selected: T, onSelect: (T) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        options.forEach { (value, label) ->
            AppChip(label = stringResource(label), selected = value == selected, onClick = { onSelect(value) })
        }
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text = text,
        style = OrlumenTheme.typography.caption,
        color = OrlumenTheme.colors.textTertiary,
        modifier = Modifier.padding(top = Spacing.sm)
    )
}

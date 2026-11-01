package com.zdmgold.orlumen.ui.screens.palette

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.theme.Stroke
import com.zdmgold.orlumen.core.viewmodel.PaletteUiState
import com.zdmgold.orlumen.core.viewmodel.PaletteViewModel
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppChip
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.AppSwitchRow
import com.zdmgold.orlumen.ui.components.AppTabRow
import com.zdmgold.orlumen.ui.components.ColorPickerWheel
import com.zdmgold.orlumen.ui.components.EmptyState
import com.zdmgold.orlumen.ui.components.IconWrapper
import com.zdmgold.orlumen.ui.components.PaletteSwatch
import com.zdmgold.orlumen.ui.components.ScreenHeader
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.iconDelete
import com.zdmgold.orlumen.ui.components.iconEdit

private const val WHEEL_SIZE_DP = 220
private const val DOT_SIZE_DP = 36
private const val NEW_COLOR_ARGB = 0xFF9B6BFF.toInt()

private enum class PaletteTab(@StringRes val label: Int) {
    STOCK(R.string.palette_tab_stock),
    CUSTOM(R.string.palette_tab_custom),
    ALBUM(R.string.palette_album_art),
    WALLPAPER(R.string.palette_wallpaper)
}

@Composable
fun ColorPaletteScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaletteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = PaletteTab.entries
    LaunchedEffect(tabIndex) { if (tabs[tabIndex] == PaletteTab.WALLPAPER) viewModel.refreshWallpaper() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(title = stringResource(R.string.palette_title), onBack = onBack)
        val current = state
        if (current != null) {
            AppTabRow(
                tabs = tabs.map { stringResource(it.label) },
                selectedIndex = tabIndex,
                onTabSelected = { tabIndex = it }
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            when (tabs[tabIndex]) {
                PaletteTab.STOCK -> PaletteList(current.stock, current, viewModel)
                PaletteTab.CUSTOM -> CustomTab(current, viewModel)
                PaletteTab.ALBUM -> AlbumTab(current, viewModel)
                PaletteTab.WALLPAPER -> WallpaperTab(current, viewModel)
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun PaletteList(
    palettes: List<ColorPalette>,
    state: PaletteUiState,
    viewModel: PaletteViewModel,
    onDelete: ((ColorPalette) -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        palettes.forEach { palette ->
            PaletteCard(
                palette = palette,
                lightsSelected = palette.id == state.visualizerPaletteId,
                aodSelected = palette.id == state.aodPaletteId,
                onLights = { viewModel.applyToVisualizer(palette) },
                onAod = { viewModel.applyToAod(palette) },
                onDelete = onDelete?.let { delete -> { delete(palette) } }
            )
        }
    }
}

@Composable
private fun PaletteCard(
    palette: ColorPalette,
    lightsSelected: Boolean,
    aodSelected: Boolean,
    onLights: () -> Unit,
    onAod: () -> Unit,
    onDelete: (() -> Unit)?
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                PaletteSwatch(colors = palette.colorsArgb)
                Text(
                    text = palette.nameRes?.let { stringResource(it) } ?: palette.name,
                    style = OrlumenTheme.typography.body,
                    color = OrlumenTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        IconWrapper(
                            imageVector = iconDelete,
                            contentDescription = stringResource(R.string.palette_delete)
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AppChip(
                    label = stringResource(R.string.palette_use_lights),
                    selected = lightsSelected,
                    onClick = onLights
                )
                AppChip(
                    label = stringResource(R.string.palette_use_aod),
                    selected = aodSelected,
                    onClick = onAod
                )
            }
        }
    }
}

@Composable
private fun AlbumTab(state: PaletteUiState, viewModel: PaletteViewModel) {
    AppCard {
        AppSwitchRow(
            title = stringResource(R.string.palette_auto_album),
            subtitle = stringResource(R.string.palette_auto_album_sub),
            checked = state.autoApplyAlbumColors,
            onCheckedChange = viewModel::setAutoApplyAlbumColors
        )
    }
    Spacer(modifier = Modifier.height(Spacing.md))
    val album = state.album
    if (album == null) {
        EmptyState(
            icon = iconEdit,
            title = stringResource(R.string.palette_album_empty),
            subtitle = stringResource(R.string.palette_album_empty_sub)
        )
    } else {
        PaletteList(listOf(album), state, viewModel)
    }
}

@Composable
private fun WallpaperTab(state: PaletteUiState, viewModel: PaletteViewModel) {
    val wallpaper = state.wallpaper
    if (wallpaper == null) {
        EmptyState(
            icon = iconEdit,
            title = stringResource(R.string.palette_wallpaper_empty),
            subtitle = stringResource(R.string.palette_wallpaper_empty_sub)
        )
    } else {
        PaletteList(listOf(wallpaper), state, viewModel)
    }
}

@Composable
private fun CustomTab(state: PaletteUiState, viewModel: PaletteViewModel) {
    if (state.custom.isNotEmpty()) {
        PaletteList(
            palettes = state.custom,
            state = state,
            viewModel = viewModel,
            onDelete = { viewModel.deleteCustomPalette(it.id) }
        )
    }
    SectionHeader(text = stringResource(R.string.palette_new_title))
    NewPaletteEditor(onSave = viewModel::saveCustomPalette)
}

@Composable
private fun NewPaletteEditor(onSave: (String, List<Int>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableIntStateOf(0) }
    val colors = remember { mutableStateListOf(NEW_COLOR_ARGB, NEW_COLOR_ARGB) }
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(R.string.palette_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                colors.forEachIndexed { index, argb ->
                    Box(
                        modifier = Modifier
                            .size(DOT_SIZE_DP.dp)
                            .swatchTap(argb, index == selected) { selected = index }
                    )
                }
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ColorPickerWheel(
                    colorArgb = colors[selected],
                    onColorChange = { colors[selected] = it },
                    modifier = Modifier.size(WHEEL_SIZE_DP.dp)
                )
            }
            if (colors.size < ColorPalette.MAX_COLORS) {
                AppChip(
                    label = stringResource(R.string.palette_add_color),
                    onClick = {
                        colors.add(NEW_COLOR_ARGB)
                        selected = colors.lastIndex
                    }
                )
            }
            AppPrimaryButton(
                text = stringResource(R.string.palette_save),
                enabled = name.isNotBlank() && colors.size >= ColorPalette.MIN_COLORS,
                onClick = {
                    onSave(name.trim(), colors.toList())
                    name = ""
                }
            )
        }
    }
}

private fun Modifier.swatchTap(argb: Int, isSelected: Boolean, onClick: () -> Unit): Modifier = this
    .clip(CircleShape)
    .background(Color(argb))
    .border(
        width = if (isSelected) Stroke.emphasis else Stroke.hairline,
        color = if (isSelected) Color.White else Color.Gray,
        shape = CircleShape
    )
    .clickable(onClick = onClick)

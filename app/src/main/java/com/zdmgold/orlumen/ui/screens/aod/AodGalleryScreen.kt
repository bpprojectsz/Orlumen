package com.zdmgold.orlumen.ui.screens.aod

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.AodViewModel
import com.zdmgold.orlumen.designs.aod.AodRegistry
import com.zdmgold.orlumen.ui.components.AodPreviewCanvas
import com.zdmgold.orlumen.ui.components.DesignCard
import com.zdmgold.orlumen.ui.components.ScreenHeader
import com.zdmgold.orlumen.ui.components.paletteColorsArgb

private const val GRID_COLUMNS = 2

@Composable
fun AodGalleryScreen(
    onBack: () -> Unit,
    onOpenEditor: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AodViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)
    Column(modifier = modifier.fillMaxSize().padding(horizontal = Spacing.lg)) {
        ScreenHeader(title = stringResource(R.string.aod_gallery_title), onBack = onBack)
        val current = state
        if (current != null) {
            val colors = paletteColorsArgb(current.palettes, current.active.paletteId)
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                modifier = Modifier.fillMaxSize()
            ) {
                items(AodRegistry.descriptors, key = { it.id }) { descriptor ->
                    DesignCard(
                        title = stringResource(descriptor.nameRes),
                        selected = descriptor.id == current.active.designId,
                        onClick = { onOpenEditor(descriptor.id) }
                    ) {
                        AodPreviewCanvas(
                            config = AodConfig(designId = descriptor.id, paletteId = current.active.paletteId),
                            colors = colors,
                            use24Hour = use24Hour,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

package com.zdmgold.orlumen.ui.screens.visualizer

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.model.VisualizerDirection
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.viewmodel.VisualizerViewModel
import com.zdmgold.orlumen.designs.visualizer.VisualizerRegistry
import com.zdmgold.orlumen.ui.components.AppChip
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.AppSlider
import com.zdmgold.orlumen.ui.components.PaletteChipRow
import com.zdmgold.orlumen.ui.components.ParamEditorPanel
import com.zdmgold.orlumen.ui.components.ScreenHeader
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.VisualizerPreviewCanvas
import com.zdmgold.orlumen.ui.components.formatSliderValue
import com.zdmgold.orlumen.ui.components.paletteColors

private const val PREVIEW_FRACTION = 0.55f
private val UNIT_RANGE = 0f..1f

@Composable
fun VisualizerEditorScreen(
    designId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisualizerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val current = state
    LaunchedEffect(designId, current != null) {
        if (current != null && current.draft.designId != designId) viewModel.selectDesign(designId)
    }
    val descriptor = VisualizerRegistry.byId(designId)?.descriptor
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = descriptor?.let { stringResource(it.nameRes) } ?: stringResource(R.string.vis_gallery_title),
            onBack = onBack
        )
        if (current != null) {
            val draft = current.draft
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                VisualizerPreviewCanvas(
                    config = draft,
                    colors = paletteColors(current.palettes, draft.paletteId),
                    modifier = Modifier.fillMaxWidth(PREVIEW_FRACTION)
                )
            }
            SectionHeader(text = stringResource(R.string.vis_section_look))
            AppSlider(
                label = stringResource(R.string.vis_param_thickness),
                value = draft.thickness,
                onValueChange = viewModel::setThickness,
                valueRange = VisualizerConfig.MIN_THICKNESS..VisualizerConfig.MAX_THICKNESS,
                valueLabel = formatSliderValue(draft.thickness, VisualizerConfig.MAX_THICKNESS)
            )
            AppSlider(
                label = stringResource(R.string.vis_param_height),
                value = draft.height,
                onValueChange = viewModel::setHeight,
                valueRange = VisualizerConfig.MIN_HEIGHT..VisualizerConfig.MAX_HEIGHT,
                valueLabel = formatSliderValue(draft.height, VisualizerConfig.MAX_HEIGHT)
            )
            AppSlider(
                label = stringResource(R.string.vis_param_sensitivity),
                value = draft.sensitivity,
                onValueChange = viewModel::setSensitivity,
                valueRange = UNIT_RANGE,
                valueLabel = formatSliderValue(draft.sensitivity, 1f)
            )
            AppSlider(
                label = stringResource(R.string.vis_param_smoothing),
                value = draft.smoothing,
                onValueChange = viewModel::setSmoothing,
                valueRange = UNIT_RANGE,
                valueLabel = formatSliderValue(draft.smoothing, 1f)
            )
            SectionHeader(text = stringResource(R.string.vis_direction))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                VisualizerDirection.entries.forEach { direction ->
                    AppChip(
                        label = stringResource(
                            if (direction == VisualizerDirection.CLOCKWISE) {
                                R.string.vis_direction_clockwise
                            } else {
                                R.string.vis_direction_anticlockwise
                            }
                        ),
                        selected = draft.direction == direction,
                        onClick = { viewModel.setDirection(direction) }
                    )
                }
            }
            if (descriptor != null && descriptor.params.isNotEmpty()) {
                SectionHeader(text = stringResource(R.string.vis_section_design))
                ParamEditorPanel(
                    specs = descriptor.params,
                    numbers = draft.extras,
                    choices = emptyMap(),
                    onNumberChange = viewModel::setExtra,
                    onChoiceChange = { _, _ -> }
                )
            }
            SectionHeader(text = stringResource(R.string.section_colors))
            PaletteChipRow(
                palettes = current.palettes,
                selectedId = draft.paletteId,
                onSelect = { viewModel.selectPalette(it.id) }
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            AppPrimaryButton(
                text = stringResource(R.string.action_apply),
                onClick = viewModel::apply,
                enabled = current.isDirty
            )
            TextButton(onClick = viewModel::resetDraft, enabled = current.isDirty) {
                Text(text = stringResource(R.string.action_reset))
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

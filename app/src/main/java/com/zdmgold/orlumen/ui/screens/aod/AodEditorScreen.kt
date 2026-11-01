package com.zdmgold.orlumen.ui.screens.aod

import android.text.format.DateFormat
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
import androidx.compose.ui.Alignment
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
import com.zdmgold.orlumen.designs.aod.AodSymbolSet
import com.zdmgold.orlumen.ui.components.AodPreviewCanvas
import com.zdmgold.orlumen.ui.components.AppCard
import com.zdmgold.orlumen.ui.components.AppChip
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.AppSlider
import com.zdmgold.orlumen.ui.components.AppSwitchRow
import com.zdmgold.orlumen.ui.components.PaletteChipRow
import com.zdmgold.orlumen.ui.components.ParamEditorPanel
import com.zdmgold.orlumen.ui.components.ScreenHeader
import com.zdmgold.orlumen.ui.components.SectionHeader
import com.zdmgold.orlumen.ui.components.formatSliderValue
import com.zdmgold.orlumen.ui.components.paletteColorsArgb
import kotlin.math.roundToInt

private const val PREVIEW_FRACTION = 0.55f

@Composable
fun AodEditorScreen(
    designId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AodViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val use24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val current = state
    LaunchedEffect(designId, current != null) {
        if (current != null && current.draft.designId != designId) viewModel.selectDesign(designId)
    }
    val descriptor = AodRegistry.byId(designId)?.descriptor
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenHeader(
            title = descriptor?.let { stringResource(it.nameRes) } ?: stringResource(R.string.aod_gallery_title),
            onBack = onBack
        )
        if (current != null) {
            val draft = current.draft
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                AodPreviewCanvas(
                    config = draft,
                    colors = paletteColorsArgb(current.palettes, draft.paletteId),
                    use24Hour = use24Hour,
                    modifier = Modifier.fillMaxWidth(PREVIEW_FRACTION)
                )
            }
            SectionHeader(text = stringResource(R.string.vis_section_look))
            AppSlider(
                label = stringResource(R.string.aod_param_weight),
                value = draft.clockWeight.toFloat(),
                onValueChange = { viewModel.setClockWeight(it.roundToInt()) },
                valueRange = AodConfig.MIN_CLOCK_WEIGHT.toFloat()..AodConfig.MAX_CLOCK_WEIGHT.toFloat(),
                valueLabel = draft.clockWeight.toString()
            )
            if (descriptor != null && (descriptor.params.isNotEmpty())) {
                SectionHeader(text = stringResource(R.string.vis_section_design))
                ParamEditorPanel(
                    specs = descriptor.params,
                    numbers = draft.extras,
                    choices = draft.choices,
                    onNumberChange = viewModel::setExtra,
                    onChoiceChange = viewModel::setChoice
                )
            }
            SectionHeader(text = stringResource(R.string.aod_symbol_title))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                AppChip(
                    label = stringResource(R.string.aod_symbol_none),
                    selected = draft.symbolId == null,
                    onClick = { viewModel.setSymbol(null) }
                )
                AodSymbolSet.symbols.forEach { symbol ->
                    AppChip(
                        label = "${symbol.glyph} ${stringResource(symbol.labelRes)}",
                        selected = draft.symbolId == symbol.id,
                        onClick = { viewModel.setSymbol(symbol.id) }
                    )
                }
            }
            if (draft.symbolId != null) {
                AppSlider(
                    label = stringResource(R.string.aod_symbol_size),
                    value = draft.symbolScale,
                    onValueChange = viewModel::setSymbolScale,
                    valueRange = AodConfig.MIN_SYMBOL_SCALE..AodConfig.MAX_SYMBOL_SCALE,
                    valueLabel = formatSliderValue(draft.symbolScale, AodConfig.MAX_SYMBOL_SCALE)
                )
            }
            SectionHeader(text = stringResource(R.string.aod_section_show))
            AppCard {
                AppSwitchRow(
                    title = stringResource(R.string.aod_show_music),
                    checked = draft.showMusicCard,
                    onCheckedChange = viewModel::setShowMusicCard
                )
                AppSwitchRow(
                    title = stringResource(R.string.aod_show_status),
                    checked = draft.showStatusRow,
                    onCheckedChange = viewModel::setShowStatusRow
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

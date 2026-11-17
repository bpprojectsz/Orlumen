package com.zdmgold.orlumen.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.model.StockPalettes
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.designs.aod.AodRegistry
import com.zdmgold.orlumen.designs.visualizer.VisualizerRegistry
import com.zdmgold.orlumen.ui.components.AodPreviewCanvas
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.IconWrapper
import com.zdmgold.orlumen.ui.components.PaletteSwatch
import com.zdmgold.orlumen.ui.components.VisualizerPreviewCanvas
import com.zdmgold.orlumen.ui.components.iconLock
import kotlinx.coroutines.delay

private const val CYCLE_MS = 2200L
private const val COLORS_PREVIEW_SCALE = 0.7f
private val previewWidth = 180.dp
private val swatchDot = 22.dp
private val dotSize = 8.dp

internal class OnboardingPage(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    val visual: @Composable () -> Unit
)

internal val onboardingPages = listOf(
    OnboardingPage(R.string.onboarding_title_1, R.string.onboarding_body_1) { EdgeLightsVisual() },
    OnboardingPage(R.string.onboarding_title_clock, R.string.onboarding_body_clock) { ClocksVisual() },
    OnboardingPage(R.string.onboarding_title_2, R.string.onboarding_body_2) { ColorsVisual() },
    OnboardingPage(R.string.onboarding_title_3, R.string.onboarding_body_3) { PrivacyVisual() }
)

@Composable
private fun <T> rememberCycling(items: List<T>): T {
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(items.size) {
        while (true) {
            delay(CYCLE_MS)
            index = (index + 1) % items.size
        }
    }
    return items[index]
}

@Composable
private fun CaptionedPreview(label: String, preview: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        preview()
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(text = label.uppercase(), style = OrlumenTheme.typography.caption, color = OrlumenTheme.colors.accent)
    }
}

@Composable
private fun EdgeLightsVisual() {
    val design = rememberCycling(VisualizerRegistry.descriptors)
    val palette = StockPalettes.all.first()
    CaptionedPreview(label = stringResource(design.nameRes)) {
        VisualizerPreviewCanvas(
            config = VisualizerConfig(designId = design.id, paletteId = palette.id),
            colors = palette.colorsArgb.map { Color(it) },
            modifier = Modifier.width(previewWidth)
        )
    }
}

@Composable
private fun ClocksVisual() {
    val design = rememberCycling(AodRegistry.descriptors)
    val palette = StockPalettes.all.first()
    CaptionedPreview(label = stringResource(design.nameRes)) {
        AodPreviewCanvas(
            config = AodConfig(designId = design.id, paletteId = palette.id),
            colors = palette.colorsArgb,
            use24Hour = true,
            modifier = Modifier.width(previewWidth)
        )
    }
}

@Composable
private fun paletteLabel(palette: ColorPalette): String = palette.nameRes?.let { stringResource(it) } ?: palette.name

@Composable
private fun ColorsVisual() {
    val palette = rememberCycling(StockPalettes.all)
    CaptionedPreview(label = paletteLabel(palette)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            VisualizerPreviewCanvas(
                config = VisualizerConfig(designId = VisualizerConfig.defaults().designId, paletteId = palette.id),
                colors = palette.colorsArgb.map { Color(it) },
                modifier = Modifier.width(previewWidth * COLORS_PREVIEW_SCALE)
            )
            PaletteSwatch(colors = palette.colorsArgb, dotSize = swatchDot)
        }
    }
}

@Composable
private fun PrivacyVisual() {
    IconWrapper(
        imageVector = iconLock,
        contentDescription = null,
        size = previewWidth / 2,
        tint = StockPalettes.all.first().colorsArgb.first().let { Color(it) }
    )
}

@Composable
fun OnboardingScreen(onFinished: () -> Unit, modifier: Modifier = Modifier, replay: Boolean = false) {
    val colors = OrlumenTheme.colors
    var index by remember { mutableIntStateOf(0) }
    val page = onboardingPages[index]
    val isLast = index == onboardingPages.lastIndex
    val lastLabel = if (replay) R.string.onboarding_done else R.string.onboarding_setup
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (!isLast) {
                TextButton(onClick = onFinished) {
                    Text(text = stringResource(R.string.onboarding_skip), color = colors.textSecondary)
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            page.visual()
            Spacer(modifier = Modifier.height(Spacing.xxl))
            Text(
                text = stringResource(page.titleRes),
                style = OrlumenTheme.typography.headline,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = stringResource(page.bodyRes),
                style = OrlumenTheme.typography.body,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.padding(Spacing.lg)) {
            onboardingPages.indices.forEach { position ->
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(if (position == index) colors.accent else colors.border)
                )
            }
        }
        AppPrimaryButton(
            text = stringResource(if (isLast) lastLabel else R.string.onboarding_next),
            onClick = { if (isLast) onFinished() else index++ }
        )
    }
}

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
import com.zdmgold.orlumen.core.model.StockPalettes
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.ui.components.AodPreviewCanvas
import com.zdmgold.orlumen.ui.components.AppPrimaryButton
import com.zdmgold.orlumen.ui.components.IconWrapper
import com.zdmgold.orlumen.ui.components.VisualizerPreviewCanvas
import com.zdmgold.orlumen.ui.components.iconLock

private val previewWidth = 180.dp
private val dotSize = 8.dp

internal class OnboardingPage(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    val visual: @Composable () -> Unit
)

internal val onboardingPages = listOf(
    OnboardingPage(R.string.onboarding_title_1, R.string.onboarding_body_1) {
        val palette = StockPalettes.all.first()
        VisualizerPreviewCanvas(
            config = VisualizerConfig.defaults(),
            colors = palette.colorsArgb.map { Color(it) },
            modifier = Modifier.width(previewWidth)
        )
    },
    OnboardingPage(R.string.onboarding_title_2, R.string.onboarding_body_2) {
        AodPreviewCanvas(
            config = AodConfig.defaults(),
            colors = StockPalettes.all.first().colorsArgb,
            use24Hour = true,
            modifier = Modifier.width(previewWidth)
        )
    },
    OnboardingPage(R.string.onboarding_title_3, R.string.onboarding_body_3) {
        IconWrapper(
            imageVector = iconLock,
            contentDescription = null,
            size = previewWidth / 2,
            tint = StockPalettes.all.first().colorsArgb.first().let { Color(it) }
        )
    }
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val colors = OrlumenTheme.colors
    var index by remember { mutableIntStateOf(0) }
    val page = onboardingPages[index]
    val isLast = index == onboardingPages.lastIndex
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
            text = stringResource(if (isLast) R.string.onboarding_setup else R.string.onboarding_next),
            onClick = { if (isLast) onFinished() else index++ }
        )
    }
}

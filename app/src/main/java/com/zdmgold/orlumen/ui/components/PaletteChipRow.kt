package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Radius
import com.zdmgold.orlumen.core.theme.Spacing
import com.zdmgold.orlumen.core.theme.Stroke

private val defaultDotSize = 18.dp

@Composable
fun PaletteSwatch(colors: List<Int>, modifier: Modifier = Modifier, dotSize: Dp = defaultDotSize) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        colors.forEach { argb ->
            Box(modifier = Modifier.size(dotSize).clip(CircleShape).background(Color(argb)))
        }
    }
}

@Composable
private fun PaletteChip(palette: ColorPalette, selected: Boolean, onClick: () -> Unit) {
    val colors = OrlumenTheme.colors
    val shape = RoundedCornerShape(Radius.pill)
    val label = palette.nameRes?.let { stringResource(it) } ?: palette.name
    Row(
        modifier = Modifier
            .clip(shape)
            .background(colors.bgSurface)
            .border(
                if (selected) Stroke.emphasis else Stroke.hairline,
                if (selected) colors.accent else colors.border,
                shape
            )
            .pressFeedback(onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaletteSwatch(colors = palette.colorsArgb)
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = label,
            style = OrlumenTheme.typography.caption,
            color = if (selected) colors.textPrimary else colors.textSecondary
        )
    }
}

@Composable
fun PaletteChipRow(
    palettes: List<ColorPalette>,
    selectedId: String,
    onSelect: (ColorPalette) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(horizontal = Spacing.lg)
    ) {
        items(palettes, key = { it.id }) { palette ->
            PaletteChip(palette = palette, selected = palette.id == selectedId, onClick = { onSelect(palette) })
        }
    }
}

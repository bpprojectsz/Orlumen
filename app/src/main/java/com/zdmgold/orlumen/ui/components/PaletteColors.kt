package com.zdmgold.orlumen.ui.components

import androidx.compose.ui.graphics.Color
import com.zdmgold.orlumen.core.model.ColorPalette

private const val WHITE_ARGB = 0xFFFFFFFF.toInt()

/** Colors of the palette with [id], falling back to the first palette, then to white. */
fun paletteColorsArgb(palettes: List<ColorPalette>, id: String): List<Int> =
    (palettes.firstOrNull { it.id == id } ?: palettes.firstOrNull())?.colorsArgb ?: listOf(WHITE_ARGB, WHITE_ARGB)

fun paletteColors(palettes: List<ColorPalette>, id: String): List<Color> =
    paletteColorsArgb(palettes, id).map { Color(it) }

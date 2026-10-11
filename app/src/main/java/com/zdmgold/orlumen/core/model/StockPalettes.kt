package com.zdmgold.orlumen.core.model

import com.zdmgold.orlumen.R

object StockPalettes {
    val all: List<ColorPalette> = listOf(
        stock(
            "obsidian_violet",
            R.string.palette_obsidian_violet,
            "Obsidian violet",
            0xFF9B6BFF,
            0xFFFF5FD2,
            0xFFF1EEFA
        ),
        stock("deep_space", R.string.palette_deep_space, "Deep space", 0xFF2DE2C9, 0xFF4C7DFF, 0xFFE8EEF8),
        stock("ember_dusk", R.string.palette_ember_dusk, "Ember dusk", 0xFFFF6B4A, 0xFFFFC857, 0xFFF6EEE8),
        stock("glacier", R.string.palette_glacier, "Glacier", 0xFF7FD8FF, 0xFF4C9BFF, 0xFFE6F6FF),
        stock("jade", R.string.palette_jade, "Jade", 0xFF3DDC97, 0xFF1FA97A, 0xFFD8FFEF),
        stock("rose_gold", R.string.palette_rose_gold, "Rose gold", 0xFFF2A4B7, 0xFFE26A8D, 0xFFFFE9EE),
        stock("solar", R.string.palette_solar, "Solar", 0xFFFFD23F, 0xFFFF8A1F, 0xFFFFF3D6),
        stock("neon_grid", R.string.palette_neon_grid, "Neon grid", 0xFF00F0FF, 0xFFFF2E93, 0xFFFFFFFF),
        stock("sunset", R.string.palette_sunset, "Sunset", 0xFFFF7E5F, 0xFFFEB47B, 0xFFFFE6D5),
        stock("monochrome", R.string.palette_monochrome, "Monochrome", 0xFFFFFFFF, 0xFF9AA0A6, 0xFF4A4F55)
    )

    fun byId(id: String): ColorPalette? = all.firstOrNull { it.id == id }

    private fun stock(id: String, nameRes: Int, name: String, vararg colors: Long): ColorPalette = ColorPalette(
        id = id,
        name = name,
        colorsArgb = colors.map { it.toInt() },
        isBuiltIn = true,
        nameRes = nameRes
    )
}

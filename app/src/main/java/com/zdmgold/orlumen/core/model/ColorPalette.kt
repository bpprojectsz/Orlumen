package com.zdmgold.orlumen.core.model

data class ColorPalette(val id: String, val name: String, val colorsArgb: List<Int>, val isBuiltIn: Boolean = true) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(colorsArgb.size in MIN_COLORS..MAX_COLORS) { "palette needs $MIN_COLORS to $MAX_COLORS colors" }
    }

    companion object {
        const val MIN_COLORS = 2
        const val MAX_COLORS = 6
    }
}

package com.zdmgold.orlumen.core.model

data class AodConfig(
    val designId: String,
    val paletteId: String,
    val clockWeight: Int = DEFAULT_CLOCK_WEIGHT,
    val symbolId: String? = null,
    val symbolScale: Float = DEFAULT_SYMBOL_SCALE,
    val showMusicCard: Boolean = true,
    val showStatusRow: Boolean = true,
    val backgroundUri: String? = null
) {
    init {
        require(designId.isNotBlank()) { "designId must not be blank" }
        require(paletteId.isNotBlank()) { "paletteId must not be blank" }
        require(clockWeight in MIN_CLOCK_WEIGHT..MAX_CLOCK_WEIGHT) { "clockWeight out of range: $clockWeight" }
        require(symbolScale in MIN_SYMBOL_SCALE..MAX_SYMBOL_SCALE) { "symbolScale out of range: $symbolScale" }
    }

    companion object {
        const val DEFAULT_DESIGN_ID = "orbit"
        const val MIN_CLOCK_WEIGHT = 100
        const val MAX_CLOCK_WEIGHT = 900
        const val DEFAULT_CLOCK_WEIGHT = 300
        const val MIN_SYMBOL_SCALE = 0.5f
        const val MAX_SYMBOL_SCALE = 2f
        const val DEFAULT_SYMBOL_SCALE = 1f

        fun defaults(): AodConfig = AodConfig(DEFAULT_DESIGN_ID, VisualizerConfig.DEFAULT_PALETTE_ID)
    }
}

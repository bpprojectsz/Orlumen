package com.zdmgold.orlumen.core.model

enum class VisualizerDirection {
    CLOCKWISE,
    ANTI_CLOCKWISE
}

data class VisualizerConfig(
    val designId: String,
    val paletteId: String,
    val thickness: Float = DEFAULT_THICKNESS,
    val height: Float = DEFAULT_HEIGHT,
    val sensitivity: Float = DEFAULT_SENSITIVITY,
    val smoothing: Float = DEFAULT_SMOOTHING,
    val direction: VisualizerDirection = VisualizerDirection.CLOCKWISE,
    val extras: Map<String, Float> = emptyMap()
) {
    init {
        require(designId.isNotBlank()) { "designId must not be blank" }
        require(paletteId.isNotBlank()) { "paletteId must not be blank" }
        require(thickness in MIN_THICKNESS..MAX_THICKNESS) { "thickness out of range: $thickness" }
        require(height in MIN_HEIGHT..MAX_HEIGHT) { "height out of range: $height" }
        require(sensitivity in UNIT_MIN..UNIT_MAX) { "sensitivity out of range: $sensitivity" }
        require(smoothing in UNIT_MIN..UNIT_MAX) { "smoothing out of range: $smoothing" }
    }

    fun withExtra(key: String, value: Float): VisualizerConfig = copy(extras = extras + (key to value))

    companion object {
        const val DEFAULT_DESIGN_ID = "filament"
        const val DEFAULT_PALETTE_ID = "obsidian_violet"
        const val MIN_THICKNESS = 1f
        const val MAX_THICKNESS = 16f
        const val DEFAULT_THICKNESS = 4f
        const val MIN_HEIGHT = 8f
        const val MAX_HEIGHT = 96f
        const val DEFAULT_HEIGHT = 32f
        const val DEFAULT_SENSITIVITY = 0.5f
        const val DEFAULT_SMOOTHING = 0.5f
        private const val UNIT_MIN = 0f
        private const val UNIT_MAX = 1f

        fun defaults(): VisualizerConfig = VisualizerConfig(DEFAULT_DESIGN_ID, DEFAULT_PALETTE_ID)
    }
}

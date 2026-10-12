package com.zdmgold.orlumen.designs.visualizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.SpectrumFrame
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.model.VisualizerDirection
import com.zdmgold.orlumen.platform.overlay.CornerRadii
import com.zdmgold.orlumen.platform.overlay.EdgeGeometry
import kotlin.math.min

/**
 * Everything a design needs for one frame. Numeric design-specific settings live in
 * [VisualizerConfig.extras]; toggles are stored there as 1f (on) or 0f (off).
 */
class DesignInput(
    val frame: SpectrumFrame,
    val geometry: EdgeGeometry,
    val config: VisualizerConfig,
    val colors: List<Color>,
    val path: EdgePath
) {
    val clockwise: Boolean
        get() = config.direction == VisualizerDirection.CLOCKWISE

    fun extra(key: String, default: Float): Float = config.extras[key] ?: default
}

interface VisualizerDesign {
    val descriptor: DesignDescriptor

    fun draw(scope: DrawScope, input: DesignInput)
}

fun gradientColor(colors: List<Color>, fraction: Float): Color {
    if (colors.size < 2) return colors.firstOrNull() ?: Color.White
    val scaled = fraction.coerceIn(0f, 1f) * (colors.size - 1)
    val lower = scaled.toInt().coerceAtMost(colors.size - 2)
    return lerp(colors[lower], colors[lower + 1], scaled - lower)
}

fun List<Color>.asGradientStops(): List<Color> =
    if (size >= 2) this else listOf(firstOrNull() ?: Color.White, firstOrNull() ?: Color.White)

@Composable
fun VisualizerCanvas(
    frame: SpectrumFrame?,
    config: VisualizerConfig,
    colors: List<Color>,
    corners: CornerRadii,
    modifier: Modifier = Modifier
) {
    val design = remember(config.designId) { VisualizerRegistry.resolve(config.designId) }
    Canvas(modifier = modifier.fillMaxSize()) {
        val current = frame ?: return@Canvas
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val geometry = EdgeGeometry(size.width, size.height, corners.coercedTo(min(size.width, size.height) / 2f))
        design.draw(this, DesignInput(current, geometry, config, colors.asGradientStops(), EdgePath(geometry)))
    }
}

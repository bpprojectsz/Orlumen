package com.zdmgold.orlumen.designs.visualizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec

private const val KEY_SPACING = "spacing"
private const val MIN_SPACING = 6f
private const val MAX_SPACING = 24f
private const val DEFAULT_SPACING = 12f
private const val BASE_INSET = 0.75f
private const val PUSH_FACTOR = 0.3f
private const val MIN_SCALE = 0.5f
private const val LEVEL_SCALE = 1.5f
private const val GLOW_SCALE = 2.2f
private const val GLOW_ALPHA = 0.3f
private const val GLOW_BASE = 0.4f

/** A string of glowing dots around the screen edge that swell and drift inward with the music. */
object FilamentDesign : VisualizerDesign {
    override val descriptor = DesignDescriptor(
        id = "filament",
        kind = DesignKind.VISUALIZER,
        nameRes = R.string.design_filament,
        isPro = false,
        params = listOf(
            ParamSpec.Slider(KEY_SPACING, R.string.param_spacing, MIN_SPACING, MAX_SPACING, DEFAULT_SPACING)
        )
    )

    override fun draw(scope: DrawScope, input: DesignInput) {
        val thickness = with(scope) { input.config.thickness.dp.toPx() }
        val reach = with(scope) { input.config.height.dp.toPx() }
        val spacing = with(scope) { input.extra(KEY_SPACING, DEFAULT_SPACING).dp.toPx() }
        val count = (input.path.length / spacing).toInt().coerceAtLeast(1)
        for (index in 0 until count) {
            val fraction = index / count.toFloat()
            val level = perimeterLevel(input.frame, fraction, input.path.bottomCenterFraction)
            val point = input.path.pointAtFraction(fraction)
            val push = thickness * BASE_INSET + level * reach * PUSH_FACTOR
            val center = Offset(point.x + point.inwardX * push, point.y + point.inwardY * push)
            val radius = thickness * (MIN_SCALE + level * LEVEL_SCALE) / 2f
            val color = gradientColor(input.colors, fraction)
            scope.drawCircle(
                color,
                radius * GLOW_SCALE,
                center,
                alpha = (GLOW_ALPHA * (GLOW_BASE + level)).coerceIn(0f, 1f)
            )
            scope.drawCircle(color, radius, center)
        }
    }
}

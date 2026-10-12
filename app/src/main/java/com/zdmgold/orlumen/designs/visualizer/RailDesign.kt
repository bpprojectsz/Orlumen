package com.zdmgold.orlumen.designs.visualizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec

private const val KEY_GAP = "gap"
private const val KEY_ROUNDED = "rounded"
private const val MIN_GAP = 0f
private const val MAX_GAP = 12f
private const val DEFAULT_GAP = 4f
private const val TOGGLE_ON_THRESHOLD = 0.5f

/** Short bars on both sides that grow like a level meter. */
object RailDesign : VisualizerDesign {
    override val descriptor = DesignDescriptor(
        id = "rail",
        kind = DesignKind.VISUALIZER,
        nameRes = R.string.design_rail,
        isPro = false,
        params = listOf(
            ParamSpec.Slider(KEY_GAP, R.string.param_gap, MIN_GAP, MAX_GAP, DEFAULT_GAP),
            ParamSpec.Toggle(KEY_ROUNDED, R.string.param_rounded, true)
        )
    )

    override fun draw(scope: DrawScope, input: DesignInput) {
        val thickness = with(scope) { input.config.thickness.dp.toPx() }
        val reach = with(scope) { input.config.height.dp.toPx() }
        val gap = with(scope) { input.extra(KEY_GAP, DEFAULT_GAP).dp.toPx() }
        val rounded = input.extra(KEY_ROUNDED, 1f) >= TOGGLE_ON_THRESHOLD
        val cap = if (rounded) StrokeCap.Round else StrokeCap.Butt
        val width = input.geometry.widthPx
        val height = input.geometry.heightPx
        val count = (height / (thickness + gap)).toInt().coerceAtLeast(1)
        for (index in 0 until count) {
            val position = (index + 0.5f) / count
            val length = thickness + sideLevel(input.frame, position, input.clockwise) * reach
            val y = height * position
            val color = gradientColor(input.colors, 1f - position)
            scope.drawLine(color, Offset(0f, y), Offset(length, y), strokeWidth = thickness, cap = cap)
            scope.drawLine(color, Offset(width, y), Offset(width - length, y), strokeWidth = thickness, cap = cap)
        }
    }
}

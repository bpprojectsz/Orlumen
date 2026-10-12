package com.zdmgold.orlumen.designs.visualizer

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec

private const val KEY_OPACITY = "opacity"
private const val MIN_OPACITY = 0.2f
private const val MAX_OPACITY = 1f
private const val DEFAULT_OPACITY = 0.8f
private const val STEPS = 96

/** Soft filled waves that rise and fall along both sides of the screen. */
object TideDesign : VisualizerDesign {
    override val descriptor = DesignDescriptor(
        id = "tide",
        kind = DesignKind.VISUALIZER,
        nameRes = R.string.design_tide,
        isPro = false,
        params = listOf(
            ParamSpec.Slider(KEY_OPACITY, R.string.param_opacity, MIN_OPACITY, MAX_OPACITY, DEFAULT_OPACITY)
        )
    )

    override fun draw(scope: DrawScope, input: DesignInput) {
        val reach = with(scope) { input.config.height.dp.toPx() }
        val minWidth = with(scope) { input.config.thickness.dp.toPx() }
        val opacity = input.extra(KEY_OPACITY, DEFAULT_OPACITY).coerceIn(MIN_OPACITY, MAX_OPACITY)
        val brush = Brush.verticalGradient(input.colors, startY = 0f, endY = input.geometry.heightPx)
        scope.drawPath(sidePath(input, reach, minWidth, fromLeft = true), brush, alpha = opacity)
        scope.drawPath(sidePath(input, reach, minWidth, fromLeft = false), brush, alpha = opacity)
    }

    private fun sidePath(input: DesignInput, reach: Float, minWidth: Float, fromLeft: Boolean): Path {
        val width = input.geometry.widthPx
        val height = input.geometry.heightPx
        val edgeX = if (fromLeft) 0f else width
        val path = Path()
        path.moveTo(edgeX, 0f)
        for (step in 0..STEPS) {
            val position = step / STEPS.toFloat()
            val offset = minWidth + sideLevel(input.frame, position, input.clockwise) * reach
            path.lineTo(if (fromLeft) offset else width - offset, height * position)
        }
        path.lineTo(edgeX, height)
        path.close()
        return path
    }
}

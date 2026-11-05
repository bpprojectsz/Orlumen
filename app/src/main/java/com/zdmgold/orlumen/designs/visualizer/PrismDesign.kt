package com.zdmgold.orlumen.designs.visualizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import kotlin.math.roundToInt

private const val KEY_SEGMENTS = "segments"
private const val KEY_GAP = "prismGap"
private const val MIN_SEGMENTS = 12f
private const val MAX_SEGMENTS = 72f
private const val DEFAULT_SEGMENTS = 36f
private const val MIN_GAP = 0.05f
private const val MAX_GAP = 0.6f
private const val DEFAULT_GAP = 0.25f
private const val BASE_WIDTH = 0.6f
private const val LEVEL_WIDTH = 1.4f
private const val INSET = 0.75f
private const val PUSH = 0.25f
private const val BASE_ALPHA = 0.35f
private const val ALPHA_RANGE = 0.65f

/** Colored segments around the whole edge that thicken and brighten with the music. */
object PrismDesign : VisualizerDesign {
    override val descriptor = DesignDescriptor(
        id = "prism",
        kind = DesignKind.VISUALIZER,
        nameRes = R.string.design_prism,
        isPro = false,
        params = listOf(
            ParamSpec.Slider(KEY_SEGMENTS, R.string.param_segments, MIN_SEGMENTS, MAX_SEGMENTS, DEFAULT_SEGMENTS),
            ParamSpec.Slider(KEY_GAP, R.string.param_gap, MIN_GAP, MAX_GAP, DEFAULT_GAP)
        )
    )

    override fun draw(scope: DrawScope, input: DesignInput) {
        val thickness = with(scope) { input.config.thickness.dp.toPx() }
        val reach = with(scope) { input.config.height.dp.toPx() }
        val count = input.extra(KEY_SEGMENTS, DEFAULT_SEGMENTS).roundToInt().coerceAtLeast(1)
        val gap = input.extra(KEY_GAP, DEFAULT_GAP).coerceIn(MIN_GAP, MAX_GAP)
        val segment = input.path.length / count
        for (index in 0 until count) {
            val fraction = index / count.toFloat()
            val level = perimeterLevel(input.frame, fraction, input.path.bottomCenterFraction)
            val start = input.path.pointAt(index * segment + segment * gap / 2f)
            val end = input.path.pointAt((index + 1) * segment - segment * gap / 2f)
            val push = thickness * INSET + level * reach * PUSH
            scope.drawLine(
                color = gradientColor(input.colors, fraction),
                start = Offset(start.x + start.inwardX * push, start.y + start.inwardY * push),
                end = Offset(end.x + end.inwardX * push, end.y + end.inwardY * push),
                strokeWidth = thickness * (BASE_WIDTH + level * LEVEL_WIDTH),
                cap = StrokeCap.Round,
                alpha = (BASE_ALPHA + ALPHA_RANGE * level).coerceIn(0f, 1f)
            )
        }
    }
}

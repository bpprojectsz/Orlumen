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
import kotlin.math.sin

private const val KEY_DENSITY = "density"
private const val MIN_DENSITY = 20f
private const val MAX_DENSITY = 120f
private const val DEFAULT_DENSITY = 60f
private const val GOLDEN = 0.618034f
private const val NANOS_PER_SECOND = 1_000_000_000f
private const val FLICKER_RATE = 9f
private const val FLICKER_SPREAD = 1.7f
private const val FLICKER_BASE = 0.7f
private const val FLICKER_RANGE = 0.3f
private const val BASE_LENGTH = 0.5f
private const val WIDTH_SCALE = 0.4f
private const val BASE_ALPHA = 0.25f

/** Thin sparks that shoot inward from the edge, taller and brighter where the music is loud. */
object SparkDesign : VisualizerDesign {
    override val descriptor = DesignDescriptor(
        id = "spark",
        kind = DesignKind.VISUALIZER,
        nameRes = R.string.design_spark,
        isPro = false,
        params = listOf(
            ParamSpec.Slider(KEY_DENSITY, R.string.param_density, MIN_DENSITY, MAX_DENSITY, DEFAULT_DENSITY)
        )
    )

    override fun draw(scope: DrawScope, input: DesignInput) {
        val thickness = with(scope) { input.config.thickness.dp.toPx() }
        val reach = with(scope) { input.config.height.dp.toPx() }
        val count = input.extra(KEY_DENSITY, DEFAULT_DENSITY).roundToInt().coerceAtLeast(1)
        val seconds = input.frame.timestampNanos / NANOS_PER_SECOND
        for (index in 0 until count) {
            val fraction = wrapUnit(index * GOLDEN)
            val level = perimeterLevel(input.frame, fraction, input.path.bottomCenterFraction)
            val flicker = FLICKER_BASE + FLICKER_RANGE * sin(seconds * FLICKER_RATE + index * FLICKER_SPREAD)
            val length = thickness * BASE_LENGTH + level * reach * flicker
            val point = input.path.pointAtFraction(fraction)
            scope.drawLine(
                color = gradientColor(input.colors, fraction),
                start = Offset(point.x, point.y),
                end = Offset(point.x + point.inwardX * length, point.y + point.inwardY * length),
                strokeWidth = thickness * WIDTH_SCALE,
                cap = StrokeCap.Round,
                alpha = (BASE_ALPHA + level).coerceIn(0f, 1f)
            )
        }
    }
}

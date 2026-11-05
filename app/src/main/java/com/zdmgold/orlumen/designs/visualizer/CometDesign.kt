package com.zdmgold.orlumen.designs.visualizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import kotlin.math.roundToInt

private const val KEY_COMETS = "comets"
private const val KEY_TRAIL = "trail"
private const val KEY_SPEED = "speed"
private const val MIN_COMETS = 1f
private const val MAX_COMETS = 5f
private const val DEFAULT_COMETS = 2f
private const val MIN_TRAIL = 60f
private const val MAX_TRAIL = 400f
private const val DEFAULT_TRAIL = 180f
private const val MIN_SPEED = 0.05f
private const val MAX_SPEED = 0.6f
private const val DEFAULT_SPEED = 0.2f
private const val NANOS_PER_SECOND = 1_000_000_000f
private const val TRAIL_STEPS = 24
private const val BASE_SCALE = 0.5f
private const val HEAD_SCALE = 1.2f
private const val LEVEL_BOOST = 1.5f
private const val INSET = 0.75f
private const val MIN_ALPHA = 0.08f

/** Bright heads that circle the screen edge, each dragging a fading trail and swelling with the music. */
object CometDesign : VisualizerDesign {
    override val descriptor = DesignDescriptor(
        id = "comet",
        kind = DesignKind.VISUALIZER,
        nameRes = R.string.design_comet,
        isPro = false,
        params = listOf(
            ParamSpec.Slider(KEY_COMETS, R.string.param_comets, MIN_COMETS, MAX_COMETS, DEFAULT_COMETS),
            ParamSpec.Slider(KEY_TRAIL, R.string.param_trail, MIN_TRAIL, MAX_TRAIL, DEFAULT_TRAIL),
            ParamSpec.Slider(KEY_SPEED, R.string.param_speed, MIN_SPEED, MAX_SPEED, DEFAULT_SPEED)
        )
    )

    override fun draw(scope: DrawScope, input: DesignInput) {
        val thickness = with(scope) { input.config.thickness.dp.toPx() }
        val trail = with(scope) { input.extra(KEY_TRAIL, DEFAULT_TRAIL).dp.toPx() }
        val requested = input.extra(KEY_COMETS, DEFAULT_COMETS).roundToInt()
        val count = requested.coerceIn(MIN_COMETS.toInt(), MAX_COMETS.toInt())
        val laps = input.frame.timestampNanos / NANOS_PER_SECOND * input.extra(KEY_SPEED, DEFAULT_SPEED)
        val direction = if (input.clockwise) 1f else -1f
        for (comet in 0 until count) {
            val headFraction = wrapUnit(direction * laps + comet / count.toFloat())
            val level = perimeterLevel(input.frame, headFraction, input.path.bottomCenterFraction)
            for (step in 0 until TRAIL_STEPS) {
                val along = step / (TRAIL_STEPS - 1f)
                val point = input.path.pointAt(headFraction * input.path.length - direction * along * trail)
                val fade = 1f - along
                val radius = thickness * (BASE_SCALE + HEAD_SCALE * fade) * (1f + level * LEVEL_BOOST) / 2f
                val inset = thickness * INSET
                val center = Offset(point.x + point.inwardX * inset, point.y + point.inwardY * inset)
                scope.drawCircle(
                    color = gradientColor(input.colors, headFraction),
                    radius = radius,
                    center = center,
                    alpha = (MIN_ALPHA + fade * (1f - MIN_ALPHA)).coerceIn(0f, 1f)
                )
            }
        }
    }
}

package com.zdmgold.orlumen.designs.visualizer

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import kotlin.math.PI
import kotlin.math.sin

private const val KEY_TWIST = "twist"
private const val MIN_TWIST = 1f
private const val MAX_TWIST = 8f
private const val DEFAULT_TWIST = 3f
private const val STEPS = 120
private const val SPEED = 1.6f
private const val AMPLITUDE_SHARE = 0.6f
private const val PHASE_WRAP_MILLIS = 3_600_000L
private const val NANOS_PER_MILLI = 1_000_000L
private const val MILLIS_PER_SECOND = 1000f
private const val TWO_PI = (2 * PI).toFloat()

/** Two ribbons per side that braid around each other, swelling with the music. */
object StrandDesign : VisualizerDesign {
    override val descriptor = DesignDescriptor(
        id = "strand",
        kind = DesignKind.VISUALIZER,
        nameRes = R.string.design_strand,
        isPro = false,
        params = listOf(ParamSpec.Slider(KEY_TWIST, R.string.param_twist, MIN_TWIST, MAX_TWIST, DEFAULT_TWIST))
    )

    override fun draw(scope: DrawScope, input: DesignInput) {
        val thickness = with(scope) { input.config.thickness.dp.toPx() }
        val reach = with(scope) { input.config.height.dp.toPx() }
        val twist = input.extra(KEY_TWIST, DEFAULT_TWIST)
        val seconds = (input.frame.timestampNanos / NANOS_PER_MILLI % PHASE_WRAP_MILLIS) / MILLIS_PER_SECOND
        val phase = seconds * SPEED * if (input.clockwise) 1f else -1f
        val brush = Brush.verticalGradient(input.colors, startY = 0f, endY = input.geometry.heightPx)
        val style = Stroke(width = thickness, cap = StrokeCap.Round, join = StrokeJoin.Round)
        for (fromLeft in listOf(true, false)) {
            for (offset in listOf(0f, PI.toFloat())) {
                val ribbon = ribbon(input, thickness, reach, twist, phase + offset, fromLeft)
                scope.drawPath(ribbon, brush, style = style)
            }
        }
    }

    private fun ribbon(
        input: DesignInput,
        thickness: Float,
        reach: Float,
        twist: Float,
        phase: Float,
        fromLeft: Boolean
    ): Path {
        val width = input.geometry.widthPx
        val height = input.geometry.heightPx
        val path = Path()
        for (step in 0..STEPS) {
            val position = step / STEPS.toFloat()
            val amplitude = thickness + sideLevel(input.frame, position, input.clockwise) * reach * AMPLITUDE_SHARE
            val wave = 0.5f + 0.5f * sin(TWO_PI * twist * position + phase)
            val offset = thickness / 2f + amplitude * wave
            val x = if (fromLeft) offset else width - offset
            if (step == 0) path.moveTo(x, height * position) else path.lineTo(x, height * position)
        }
        return path
    }
}

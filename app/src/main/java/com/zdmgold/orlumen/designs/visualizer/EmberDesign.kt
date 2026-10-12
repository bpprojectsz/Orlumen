package com.zdmgold.orlumen.designs.visualizer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec

private const val KEY_PULSE = "pulse"
private const val MIN_PULSE = 0f
private const val MAX_PULSE = 1f
private const val DEFAULT_PULSE = 0.7f
private const val SEGMENTS = 120
private const val ENERGY_BLEND = 0.6f
private const val WIDTH_GAIN = 3f
private const val BASE_ALPHA = 0.3f
private const val PHASE_WRAP_MILLIS = 20_000L
private const val NANOS_PER_MILLI = 1_000_000L

private class GlowPass(val widthFactor: Float, val alpha: Float)

private val glowPasses = listOf(GlowPass(4f, 0.12f), GlowPass(2f, 0.25f), GlowPass(1f, 0.9f))

/** A glowing outline around the whole screen that pulses with the overall energy of the music. */
object EmberDesign : VisualizerDesign {
    override val descriptor = DesignDescriptor(
        id = "ember",
        kind = DesignKind.VISUALIZER,
        nameRes = R.string.design_ember,
        isPro = false,
        params = listOf(ParamSpec.Slider(KEY_PULSE, R.string.param_pulse, MIN_PULSE, MAX_PULSE, DEFAULT_PULSE))
    )

    override fun draw(scope: DrawScope, input: DesignInput) {
        val pulse = input.extra(KEY_PULSE, DEFAULT_PULSE).coerceIn(MIN_PULSE, MAX_PULSE)
        val base = with(scope) { input.config.thickness.dp.toPx() }
        val energy = averageLevel(input.frame)
        val length = input.path.length
        val drift = (input.frame.timestampNanos / NANOS_PER_MILLI % PHASE_WRAP_MILLIS) / PHASE_WRAP_MILLIS.toFloat()
        val motion = if (input.clockwise) drift else -drift
        for (index in 0 until SEGMENTS) {
            val start = index / SEGMENTS.toFloat()
            val end = (index + 1) / SEGMENTS.toFloat()
            val local = perimeterLevel(input.frame, (start + end) / 2f, input.path.bottomCenterFraction)
            val intensity = (ENERGY_BLEND * energy + (1f - ENERGY_BLEND) * local).coerceIn(0f, 1f)
            val width = base * (1f + pulse * WIDTH_GAIN * intensity)
            val color = gradientColor(input.colors, wrapUnit(start + motion))
            val first = input.path.pointAt(start * length)
            val second = input.path.pointAt(end * length)
            for (pass in glowPasses) {
                val passWidth = width * pass.widthFactor
                val inset = passWidth / 2f
                scope.drawLine(
                    color,
                    Offset(first.x + first.inwardX * inset, first.y + first.inwardY * inset),
                    Offset(second.x + second.inwardX * inset, second.y + second.inwardY * inset),
                    strokeWidth = passWidth,
                    cap = StrokeCap.Round,
                    alpha = (pass.alpha * (BASE_ALPHA + (1f - BASE_ALPHA) * intensity)).coerceIn(0f, 1f)
                )
            }
        }
    }
}

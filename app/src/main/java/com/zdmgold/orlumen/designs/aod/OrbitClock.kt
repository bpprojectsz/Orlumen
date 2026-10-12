package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import kotlin.math.roundToInt

private const val KEY_RINGS = "rings"
private const val MIN_RINGS = 2f
private const val MAX_RINGS = 3f
private const val DEFAULT_RINGS = 3f
private const val SIZE_DP = 280
private const val RING_STROKE_DP = 6f
private const val RING_GAP_DP = 10f
private const val TRACK_ALPHA = 0.15f
private const val FULL_CIRCLE = 360f
private const val START_ANGLE = -90f
private const val HOURS_ON_FACE = 12f
private const val MINUTES_PER_HOUR = 60f
private const val MINUTES_PER_DAY = 1440f
private const val TIME_SIZE_SP = 44
private const val MARKER_SIZE_SP = 16

/** Concentric progress rings for the hour, the minute and the day, with the time in the centre. */
object OrbitClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "orbit",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_orbit,
        isPro = false,
        params = listOf(ParamSpec.Slider(KEY_RINGS, R.string.param_rings, MIN_RINGS, MAX_RINGS, DEFAULT_RINGS))
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val rings = input.extra(KEY_RINGS, DEFAULT_RINGS).roundToInt().coerceIn(MIN_RINGS.toInt(), MAX_RINGS.toInt())
        val hour = input.dateTime.hour
        val minute = input.dateTime.minute
        val fractions = listOf(
            ((hour % HOURS_ON_FACE.toInt()) + minute / MINUTES_PER_HOUR) / HOURS_ON_FACE,
            minute / MINUTES_PER_HOUR,
            (hour * MINUTES_PER_HOUR + minute) / MINUTES_PER_DAY
        ).take(rings)
        Box(modifier = modifier.size(SIZE_DP.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = RING_STROKE_DP.dp.toPx()
                val gap = RING_GAP_DP.dp.toPx()
                fractions.forEachIndexed { index, fraction ->
                    val inset = stroke / 2f + index * (stroke + gap)
                    val topLeft = Offset(inset, inset)
                    val arcSize = Size(size.width - 2f * inset, size.height - 2f * inset)
                    drawArc(
                        color = Color.White.copy(alpha = TRACK_ALPHA),
                        startAngle = START_ANGLE,
                        sweepAngle = FULL_CIRCLE,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )
                    drawArc(
                        color = input.accent,
                        startAngle = START_ANGLE,
                        sweepAngle = FULL_CIRCLE * fraction,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${input.clock.hours}${input.separator}${input.clock.minutes}",
                    color = input.accent,
                    fontSize = TIME_SIZE_SP.sp,
                    fontWeight = FontWeight(input.config.clockWeight)
                )
                input.clock.marker?.let { Text(text = it, color = input.secondary, fontSize = MARKER_SIZE_SP.sp) }
            }
        }
    }
}

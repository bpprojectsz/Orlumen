package com.zdmgold.orlumen.designs.aod

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val KEY_NUMERALS = "numerals"
private const val TOGGLE_ON_THRESHOLD = 0.5f
private const val SIZE_DP = 280
private const val MARGIN_DP = 12f
private const val TICKS = 60
private const val TICKS_PER_HOUR = 5
private const val MINOR_ALPHA = 0.3f
private const val MINOR_LENGTH_DP = 6f
private const val MAJOR_LENGTH_DP = 14f
private const val MINOR_WIDTH_DP = 1.5f
private const val MAJOR_WIDTH_DP = 3f
private const val NUMERAL_SIZE_DP = 22f
private const val NUMERAL_RADIUS_SHARE = 0.72f
private const val BASELINE_SHARE = 0.33f
private const val HOUR_HAND_SHARE = 0.5f
private const val MINUTE_HAND_SHARE = 0.78f
private const val HOUR_HAND_WIDTH_DP = 7f
private const val MINUTE_HAND_WIDTH_DP = 4f
private const val HUB_RADIUS_DP = 6f
private const val DEGREES_PER_HOUR = 30f
private const val DEGREES_PER_MINUTE = 6f
private const val MINUTES_PER_HOUR = 60f
private const val HOURS_ON_FACE = 12
private const val HALF_TURN_DEGREES = 180f
private const val QUARTER_TURN_DEGREES = 90f

/** A classic analogue face with minute ticks, optional numerals and two hands. */
object MeridianClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "meridian",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_meridian,
        isPro = false,
        params = listOf(ParamSpec.Toggle(KEY_NUMERALS, R.string.param_numerals, true))
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val showNumerals = input.extra(KEY_NUMERALS, 1f) >= TOGGLE_ON_THRESHOLD
        val hour = input.dateTime.hour % HOURS_ON_FACE
        val minute = input.dateTime.minute
        val hourAngle = (hour + minute / MINUTES_PER_HOUR) * DEGREES_PER_HOUR
        val minuteAngle = minute * DEGREES_PER_MINUTE
        Canvas(modifier = modifier.size(SIZE_DP.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - MARGIN_DP.dp.toPx()
            drawTicks(center, radius, input)
            if (showNumerals) drawNumerals(center, radius, input.secondary)
            drawHand(center, hourAngle, radius * HOUR_HAND_SHARE, HOUR_HAND_WIDTH_DP.dp.toPx(), Color.White)
            drawHand(center, minuteAngle, radius * MINUTE_HAND_SHARE, MINUTE_HAND_WIDTH_DP.dp.toPx(), input.accent)
            drawCircle(input.accent, HUB_RADIUS_DP.dp.toPx(), center)
        }
    }

    private fun DrawScope.drawTicks(center: Offset, radius: Float, input: AodClockInput) {
        for (tick in 0 until TICKS) {
            val major = tick % TICKS_PER_HOUR == 0
            val length = (if (major) MAJOR_LENGTH_DP else MINOR_LENGTH_DP).dp.toPx()
            val width = (if (major) MAJOR_WIDTH_DP else MINOR_WIDTH_DP).dp.toPx()
            val radians = radiansFor(tick * DEGREES_PER_MINUTE)
            val outer = Offset(center.x + radius * cos(radians), center.y + radius * sin(radians))
            val inner = Offset(center.x + (radius - length) * cos(radians), center.y + (radius - length) * sin(radians))
            val color = if (major) input.accent else Color.White.copy(alpha = MINOR_ALPHA)
            drawLine(color, inner, outer, strokeWidth = width, cap = StrokeCap.Round)
        }
    }

    private fun DrawScope.drawNumerals(center: Offset, radius: Float, color: Color) {
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = color.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = NUMERAL_SIZE_DP.dp.toPx()
        }
        drawIntoCanvas { canvas ->
            for (numeral in 1..HOURS_ON_FACE) {
                val radians = radiansFor(numeral * DEGREES_PER_HOUR)
                val x = center.x + radius * NUMERAL_RADIUS_SHARE * cos(radians)
                val y = center.y + radius * NUMERAL_RADIUS_SHARE * sin(radians) + paint.textSize * BASELINE_SHARE
                canvas.nativeCanvas.drawText(numeral.toString(), x, y, paint)
            }
        }
    }

    private fun DrawScope.drawHand(center: Offset, degrees: Float, length: Float, width: Float, color: Color) {
        val radians = radiansFor(degrees)
        val tip = Offset(center.x + length * cos(radians), center.y + length * sin(radians))
        drawLine(color, center, tip, strokeWidth = width, cap = StrokeCap.Round)
    }

    private fun radiansFor(clockDegrees: Float): Float =
        ((clockDegrees - QUARTER_TURN_DEGREES) * PI / HALF_TURN_DEGREES).toFloat()
}

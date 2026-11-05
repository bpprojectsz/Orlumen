package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec
import kotlin.math.PI
import kotlin.math.sin

private const val KEY_ARCH = "arch"
private const val MIN_ARCH = 60f
private const val MAX_ARCH = 200f
private const val DEFAULT_ARCH = 120f
private const val MINUTES_PER_DAY = 1440f
private const val SIDE_MARGIN_DP = 28
private const val STEPS = 48
private const val ARCH_ALPHA = 0.35f
private const val LINE_DP = 3
private const val MARKER_DP = 11
private const val TIME_SIZE_SP = 72
private const val TIME_PADDING_DP = 28
private const val EXTRA_HEIGHT_DP = 24

/** A daylight arc: the marker rises from the left at midnight, peaks at noon and sets on the right. */
object HorizonClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "horizon",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_horizon,
        isPro = false,
        params = listOf(ParamSpec.Slider(KEY_ARCH, R.string.param_arch, MIN_ARCH, MAX_ARCH, DEFAULT_ARCH))
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val arch = input.extra(KEY_ARCH, DEFAULT_ARCH).coerceIn(MIN_ARCH, MAX_ARCH)
        val dayFraction = (input.dateTime.hour * 60 + input.dateTime.minute) / MINUTES_PER_DAY
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            val canvasModifier = Modifier
                .fillMaxWidth()
                .height((arch + EXTRA_HEIGHT_DP).dp)
                .padding(horizontal = SIDE_MARGIN_DP.dp)
            Canvas(modifier = canvasModifier) {
                val left = 0f
                val right = size.width
                val base = size.height - EXTRA_HEIGHT_DP.dp.toPx() / 2f
                val rise = arch.dp.toPx()
                val stroke = LINE_DP.dp.toPx()
                var previous = Offset(left, base)
                for (step in 1..STEPS) {
                    val t = step / STEPS.toFloat()
                    val point = Offset(left + (right - left) * t, base - rise * sin(PI.toFloat() * t))
                    drawLine(
                        input.secondary,
                        previous,
                        point,
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                        alpha = ARCH_ALPHA
                    )
                    previous = point
                }
                val marker = Offset(left + (right - left) * dayFraction, base - rise * sin(PI.toFloat() * dayFraction))
                drawCircle(input.accent, radius = MARKER_DP.dp.toPx(), center = marker)
            }
            Text(
                text = input.clock.hours + input.separator + input.clock.minutes,
                color = input.accent,
                fontSize = TIME_SIZE_SP.sp,
                fontWeight = FontWeight(input.config.clockWeight),
                modifier = Modifier.padding(top = TIME_PADDING_DP.dp)
            )
            input.clock.marker?.let { Text(text = it, color = input.secondary, fontSize = (TIME_SIZE_SP / 4).sp) }
        }
    }
}

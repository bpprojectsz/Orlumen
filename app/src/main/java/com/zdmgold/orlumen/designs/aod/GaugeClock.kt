package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec

private const val KEY_WIDTH = "barWidth"
private const val MIN_WIDTH = 14f
private const val MAX_WIDTH = 60f
private const val DEFAULT_WIDTH = 28f
private const val BAR_HEIGHT_DP = 240
private const val BAR_GAP_DP = 48
private const val TRACK_ALPHA = 0.2f
private const val DIGIT_SIZE_SP = 56
private const val DIGIT_PADDING_DP = 16
private const val HOURS_IN_DAY = 24f
private const val MINUTES_IN_HOUR = 60f

/** Two vertical gauges: the left fills through the day, the right fills through the hour. */
object GaugeClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "gauge",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_gauge,
        isPro = false,
        params = listOf(ParamSpec.Slider(KEY_WIDTH, R.string.param_bar_width, MIN_WIDTH, MAX_WIDTH, DEFAULT_WIDTH))
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val barWidth = input.extra(KEY_WIDTH, DEFAULT_WIDTH).coerceIn(MIN_WIDTH, MAX_WIDTH)
        val hourFill = (input.dateTime.hour + input.dateTime.minute / MINUTES_IN_HOUR) / HOURS_IN_DAY
        val minuteFill = input.dateTime.minute / MINUTES_IN_HOUR
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(BAR_GAP_DP.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Gauge(barWidth, hourFill, input.accent, input.secondary, input.clock.hours, input.config.clockWeight)
            Gauge(barWidth, minuteFill, input.accent, input.secondary, input.clock.minutes, input.config.clockWeight)
        }
    }
}

@Composable
private fun Gauge(
    barWidth: Float,
    fill: Float,
    accent: Color,
    secondary: Color,
    digits: String,
    weight: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.width(barWidth.dp).height(BAR_HEIGHT_DP.dp)) {
            val radius = CornerRadius(size.width / 2f, size.width / 2f)
            drawRoundRect(secondary, Offset.Zero, size, radius, alpha = TRACK_ALPHA)
            val filled = size.height * fill.coerceIn(0f, 1f)
            drawRoundRect(accent, Offset(0f, size.height - filled), Size(size.width, filled), radius)
        }
        Text(
            text = digits,
            color = accent,
            fontSize = DIGIT_SIZE_SP.sp,
            fontWeight = FontWeight(weight),
            modifier = Modifier.padding(top = DIGIT_PADDING_DP.dp)
        )
    }
}

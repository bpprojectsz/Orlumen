package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec

private const val KEY_DOT = "dotSize"
private const val MIN_DOT = 10f
private const val MAX_DOT = 30f
private const val DEFAULT_DOT = 18f
private const val COLUMN_GAP = 0.9f
private const val ROW_GAP = 0.6f
private const val UNLIT_ALPHA = 0.22f
private const val TIME_SIZE_SP = 22
private const val TIME_PADDING_DP = 24
private const val TEN = 10
private val BIT_COUNTS = listOf(2, 4, 3, 4)
private const val BITS = 4

/** Hours and minutes as lit dots: each column is one digit written in binary. */
object BinaryClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "binary",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_binary,
        isPro = false,
        params = listOf(ParamSpec.Slider(KEY_DOT, R.string.param_dot_size, MIN_DOT, MAX_DOT, DEFAULT_DOT))
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val dot = input.extra(KEY_DOT, DEFAULT_DOT).coerceIn(MIN_DOT, MAX_DOT)
        val hour = if (input.use24Hour) input.dateTime.hour else ((input.dateTime.hour + 11) % 12) + 1
        val minute = input.dateTime.minute
        val digits = listOf(hour / TEN, hour % TEN, minute / TEN, minute % TEN)
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy((dot * COLUMN_GAP).dp)) {
                digits.forEachIndexed { column, value ->
                    Column(verticalArrangement = Arrangement.spacedBy((dot * ROW_GAP).dp)) {
                        for (bit in BITS - 1 downTo 0) {
                            val exists = bit < BIT_COUNTS[column]
                            val lit = exists && (value shr bit) and 1 == 1
                            val color = when {
                                !exists -> Color.Transparent
                                lit -> input.accent
                                else -> input.secondary.copy(alpha = UNLIT_ALPHA)
                            }
                            Box(modifier = Modifier.size(dot.dp).clip(CircleShape).background(color))
                        }
                    }
                }
            }
            Text(
                text = input.clock.hours + input.separator + input.clock.minutes,
                color = input.secondary,
                fontSize = TIME_SIZE_SP.sp,
                fontWeight = FontWeight(input.config.clockWeight),
                modifier = Modifier.padding(top = TIME_PADDING_DP.dp)
            )
        }
    }
}

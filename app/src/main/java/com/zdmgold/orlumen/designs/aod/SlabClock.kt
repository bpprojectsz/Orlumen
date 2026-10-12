package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import com.zdmgold.orlumen.core.model.ParamSpec

private const val KEY_SPACING = "lineSpacing"
private const val MIN_SPACING = 0.7f
private const val MAX_SPACING = 1.2f
private const val DEFAULT_SPACING = 0.85f
private const val DIGIT_SIZE_SP = 128f
private const val SYMBOL_SIZE_SP = 28f
private const val MARKER_SIZE_SP = 18
private const val MINUTES_ALPHA = 0.92f

/** Huge stacked digits: hours over minutes, with an optional symbol between them. */
object SlabClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "slab",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_slab,
        isPro = false,
        params = listOf(
            ParamSpec.Slider(KEY_SPACING, R.string.param_line_spacing, MIN_SPACING, MAX_SPACING, DEFAULT_SPACING)
        )
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val spacing = input.extra(KEY_SPACING, DEFAULT_SPACING).coerceIn(MIN_SPACING, MAX_SPACING)
        val weight = FontWeight(input.config.clockWeight)
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = input.clock.hours,
                color = input.accent,
                fontSize = DIGIT_SIZE_SP.sp,
                lineHeight = (DIGIT_SIZE_SP * spacing).sp,
                fontWeight = weight
            )
            input.symbol?.let {
                Text(
                    text = it.glyph,
                    color = input.secondary,
                    fontSize = (SYMBOL_SIZE_SP * input.config.symbolScale).sp
                )
            }
            Text(
                text = input.clock.minutes,
                color = Color.White.copy(alpha = MINUTES_ALPHA),
                fontSize = DIGIT_SIZE_SP.sp,
                lineHeight = (DIGIT_SIZE_SP * spacing).sp,
                fontWeight = weight
            )
            input.clock.marker?.let { Text(text = it, color = input.secondary, fontSize = MARKER_SIZE_SP.sp) }
        }
    }
}

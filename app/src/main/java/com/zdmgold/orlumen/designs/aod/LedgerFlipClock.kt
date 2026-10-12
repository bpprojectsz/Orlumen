package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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

private const val KEY_DIVIDER = "divider"
private const val TOGGLE_ON_THRESHOLD = 0.5f
private const val TILE_WIDTH_DP = 112
private const val TILE_HEIGHT_DP = 144
private const val TILE_RADIUS_DP = 14
private const val DIGIT_SIZE_SP = 84
private const val SYMBOL_SIZE_SP = 28f
private const val MARKER_SIZE_SP = 18
private const val TILE_COLOR = 0xFF16161A

/** Two flip-card tiles, one for the hours and one for the minutes, with an optional centre seam. */
object LedgerFlipClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "ledger",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_ledger,
        isPro = false,
        params = listOf(ParamSpec.Toggle(KEY_DIVIDER, R.string.param_divider, true))
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val showDivider = input.extra(KEY_DIVIDER, 1f) >= TOGGLE_ON_THRESHOLD
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlipTile(input.clock.hours, input, showDivider)
                input.symbol?.let {
                    Text(
                        text = it.glyph,
                        color = input.secondary,
                        fontSize = (SYMBOL_SIZE_SP * input.config.symbolScale).sp
                    )
                }
                FlipTile(input.clock.minutes, input, showDivider)
            }
            input.clock.marker?.let { Text(text = it, color = input.secondary, fontSize = MARKER_SIZE_SP.sp) }
        }
    }

    @Composable
    private fun FlipTile(text: String, input: AodClockInput, showDivider: Boolean) {
        Box(
            modifier = Modifier
                .size(width = TILE_WIDTH_DP.dp, height = TILE_HEIGHT_DP.dp)
                .clip(RoundedCornerShape(TILE_RADIUS_DP.dp))
                .background(Color(TILE_COLOR)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = input.accent,
                fontSize = DIGIT_SIZE_SP.sp,
                fontWeight = FontWeight(input.config.clockWeight)
            )
            if (showDivider) {
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.Black))
            }
        }
    }
}

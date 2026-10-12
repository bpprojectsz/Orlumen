package com.zdmgold.orlumen.designs.aod

import android.icu.text.RuleBasedNumberFormat
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.model.DesignKind
import java.util.Locale

private const val WORD_SIZE_SP = 52
private const val NUMERIC_SIZE_SP = 28
private const val SYMBOL_SIZE_SP = 28f
private const val MARKER_SIZE_SP = 18
private const val MINUTES_ALPHA = 0.92f

/** The time spelled out in words in the phone's language, for example "Seven" over "Twenty-nine". */
object LexiconTextClock : AodDesign {
    override val descriptor = DesignDescriptor(
        id = "lexicon",
        kind = DesignKind.AOD,
        nameRes = R.string.aod_lexicon,
        isPro = false,
        params = emptyList()
    )

    @Composable
    override fun Clock(input: AodClockInput, modifier: Modifier) {
        val locale = Locale.getDefault()
        val speller = remember(locale) { RuleBasedNumberFormat(locale, RuleBasedNumberFormat.SPELLOUT) }
        val weight = FontWeight(input.config.clockWeight)
        val hourWord = speller.format(input.clock.hours.toLong()).replaceFirstChar { it.titlecase(locale) }
        val minuteValue = input.clock.minutes.toLong()
        Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = hourWord,
                color = input.accent,
                fontSize = WORD_SIZE_SP.sp,
                fontWeight = weight,
                textAlign = TextAlign.Center
            )
            input.symbol?.let {
                Text(
                    text = it.glyph,
                    color = input.secondary,
                    fontSize = (SYMBOL_SIZE_SP * input.config.symbolScale).sp
                )
            }
            if (minuteValue == 0L) {
                Text(text = input.clock.minutes, color = input.secondary, fontSize = NUMERIC_SIZE_SP.sp)
            } else {
                Text(
                    text = speller.format(minuteValue).replaceFirstChar { it.titlecase(locale) },
                    color = Color.White.copy(alpha = MINUTES_ALPHA),
                    fontSize = WORD_SIZE_SP.sp,
                    fontWeight = weight,
                    textAlign = TextAlign.Center
                )
            }
            input.clock.marker?.let { Text(text = it, color = input.secondary, fontSize = MARKER_SIZE_SP.sp) }
        }
    }
}

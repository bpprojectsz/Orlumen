package com.zdmgold.orlumen.designs.aod

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.core.model.DesignDescriptor
import com.zdmgold.orlumen.core.utils.ClockParts
import com.zdmgold.orlumen.core.utils.TimeFormatter
import java.time.Instant
import java.time.LocalDateTime

/**
 * Everything a clock design needs for one minute. Numeric settings live in [AodConfig.extras]
 * (toggles as 1f or 0f) and choice settings in [AodConfig.choices].
 */
class AodClockInput(
    val instant: Instant,
    val dateTime: LocalDateTime,
    val use24Hour: Boolean,
    val config: AodConfig,
    val accent: Color,
    val secondary: Color,
    val symbol: AodSymbol?
) {
    val clock: ClockParts = TimeFormatter.parts(dateTime.hour, dateTime.minute, use24Hour)

    val separator: String
        get() = symbol?.glyph ?: ":"

    fun extra(key: String, default: Float): Float = config.extras[key] ?: default

    fun choice(key: String, default: String): String = config.choices[key] ?: default
}

interface AodDesign {
    val descriptor: DesignDescriptor

    @Composable
    fun Clock(input: AodClockInput, modifier: Modifier)
}

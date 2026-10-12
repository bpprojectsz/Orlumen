package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.zdmgold.orlumen.core.model.AodConfig
import com.zdmgold.orlumen.designs.aod.AodClockInput
import com.zdmgold.orlumen.designs.aod.AodRegistry
import com.zdmgold.orlumen.designs.aod.AodSymbolSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private const val SECONDARY_ALPHA = 0.55f
private const val WHITE_ARGB = 0xFFFFFFFF.toInt()

@Composable
fun AodPreviewCanvas(config: AodConfig, colors: List<Int>, use24Hour: Boolean, modifier: Modifier = Modifier) {
    val accent = Color(colors.firstOrNull() ?: WHITE_ARGB)
    val instant = remember { Instant.now() }
    val input = remember(config, accent, use24Hour, instant) {
        AodClockInput(
            instant = instant,
            dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()),
            use24Hour = use24Hour,
            config = config,
            accent = accent,
            secondary = Color.White.copy(alpha = SECONDARY_ALPHA),
            symbol = AodSymbolSet.byId(config.symbolId)
        )
    }
    val design = remember(config.designId) { AodRegistry.resolve(config.designId) }
    PhoneFrame(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            design.Clock(input, Modifier)
        }
    }
}

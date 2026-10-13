package com.zdmgold.orlumen.platform.aod

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.designs.aod.AodClockInput
import com.zdmgold.orlumen.designs.aod.AodMusicCard
import com.zdmgold.orlumen.designs.aod.AodRegistry
import com.zdmgold.orlumen.designs.aod.AodStatusRow
import com.zdmgold.orlumen.designs.aod.AodSymbolSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val SECONDARY_ALPHA = 0.55f
private const val WHITE_ARGB = 0xFFFFFFFF.toInt()

@Composable
fun AodHostScreen(controller: AodController, onTap: () -> Unit, modifier: Modifier = Modifier) {
    val flow = remember(controller) { controller.uiState() }
    val state by flow.collectAsState(initial = null)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) { detectTapGestures { onTap() } }
    ) {
        state?.let { AodHostContent(it) }
    }
}

@Composable
fun AodHostContent(state: AodUiState, modifier: Modifier = Modifier) {
    val accent = Color(state.colors.firstOrNull() ?: WHITE_ARGB)
    val secondary = Color.White.copy(alpha = SECONDARY_ALPHA)
    val instant = Instant.ofEpochMilli(state.timeMillis)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val input = AodClockInput(
        instant = instant,
        dateTime = dateTime,
        use24Hour = state.use24Hour,
        config = state.config,
        accent = accent,
        secondary = secondary,
        symbol = AodSymbolSet.byId(state.config.symbolId)
    )
    val design = remember(state.config.designId) { AodRegistry.resolve(state.config.designId) }
    val dateText = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(dateTime)
    Column(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(state.shift.x, state.shift.y) }
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        design.Clock(input, Modifier)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = dateText, color = secondary, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(40.dp))
        if (state.config.showMusicCard && state.nowPlaying.title != null) {
            AodMusicCard(state.nowPlaying, state.elapsedRealtimeMs, accent, secondary)
        }
        if (state.config.showStatusRow) {
            Spacer(modifier = Modifier.height(24.dp))
            AodStatusRow(state.battery, secondary)
        }
    }
}

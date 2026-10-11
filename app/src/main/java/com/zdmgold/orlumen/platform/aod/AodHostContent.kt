package com.zdmgold.orlumen.platform.aod

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.utils.TimeFormatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val SECONDARY_ALPHA = 0.55f
private const val TRACK_ALPHA = 0.2f
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
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(state.timeMillis), ZoneId.systemDefault())
    val clock = TimeFormatter.parts(dateTime.hour, dateTime.minute, state.use24Hour)
    val dateText = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(dateTime)
    Column(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(state.shift.x, state.shift.y) }
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${clock.hours}:${clock.minutes}",
                color = accent,
                fontSize = 88.sp,
                fontWeight = FontWeight(state.config.clockWeight)
            )
            clock.marker?.let {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = it, color = secondary, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
            }
        }
        Text(text = dateText, color = secondary, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(48.dp))
        if (state.config.showMusicCard && state.nowPlaying.title != null) {
            MusicLine(state = state, accent = accent, secondary = secondary)
        }
        if (state.config.showStatusRow) {
            Spacer(modifier = Modifier.height(24.dp))
            val charging = if (state.battery.isCharging) " · ${stringResource(R.string.aod_charging)}" else ""
            Text(text = "${state.battery.percent}%$charging", color = secondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun MusicLine(state: AodUiState, accent: Color, secondary: Color) {
    val track = state.nowPlaying
    val position = track.positionAt(state.elapsedRealtimeMs)
    val fraction = if (track.durationMs > 0) (position.toFloat() / track.durationMs).coerceIn(0f, 1f) else 0f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = track.title.orEmpty(), color = Color.White, fontSize = 16.sp, maxLines = 1)
        track.artist?.let { Text(text = it, color = secondary, fontSize = 14.sp, maxLines = 1) }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.6f)
                .height(2.dp)
                .background(Color.White.copy(alpha = TRACK_ALPHA))
        ) {
            Box(modifier = Modifier.fillMaxWidth(fraction).height(2.dp).background(accent))
        }
    }
}

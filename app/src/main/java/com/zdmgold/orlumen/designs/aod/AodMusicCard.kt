package com.zdmgold.orlumen.designs.aod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zdmgold.orlumen.core.model.NowPlaying

private const val TRACK_ALPHA = 0.2f
private const val TRACK_WIDTH = 0.6f

@Composable
fun AodMusicCard(
    nowPlaying: NowPlaying,
    elapsedRealtimeMs: Long,
    accent: Color,
    secondary: Color,
    modifier: Modifier = Modifier
) {
    val position = nowPlaying.positionAt(elapsedRealtimeMs)
    val fraction = if (nowPlaying.durationMs > 0) {
        (position.toFloat() / nowPlaying.durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = nowPlaying.title.orEmpty(), color = Color.White, fontSize = 16.sp, maxLines = 1)
        nowPlaying.artist?.let { Text(text = it, color = secondary, fontSize = 14.sp, maxLines = 1) }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = TRACK_WIDTH)
                .height(2.dp)
                .background(Color.White.copy(alpha = TRACK_ALPHA))
        ) {
            Box(modifier = Modifier.fillMaxWidth(fraction).height(2.dp).background(accent))
        }
    }
}

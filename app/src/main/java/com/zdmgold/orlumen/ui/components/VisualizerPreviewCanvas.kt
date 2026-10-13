package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.core.model.SpectrumFrame
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.theme.OrlumenTheme
import com.zdmgold.orlumen.core.theme.Stroke
import com.zdmgold.orlumen.designs.visualizer.VisualizerCanvas
import com.zdmgold.orlumen.platform.overlay.CornerRadii
import kotlin.math.sin

private const val REFERENCE_WIDTH_DP = 360f
private const val REFERENCE_HEIGHT_DP = 780f
private const val PREVIEW_CORNER_DP = 36f
private const val DEMO_BANDS = 32
private const val DEMO_BASE = 0.3f
private const val DEMO_RANGE = 0.6f
private const val DEMO_FALLOFF = 0.55f
private const val DEMO_SPEED = 2.2f
private const val DEMO_SPREAD = 0.45f
private const val NANOS_PER_MILLI = 1_000_000L
private const val WRAP_MILLIS = 600_000L
private const val MILLIS_PER_SECOND = 1000f

/**
 * A black phone-shaped frame that lays its content out at a full-size reference screen and scales
 * it down, so dp values in a design look the same in a small preview as on the real screen.
 */
@Composable
internal fun PhoneFrame(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = OrlumenTheme.colors
    BoxWithConstraints(modifier = modifier) {
        val scale = maxWidth.value / REFERENCE_WIDTH_DP
        val shape = RoundedCornerShape((PREVIEW_CORNER_DP * scale).dp)
        Box(
            modifier = Modifier
                .width(maxWidth)
                .height((REFERENCE_HEIGHT_DP * scale).dp)
                .clip(shape)
                .background(Color.Black)
                .border(Stroke.hairline, colors.border, shape)
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .size(REFERENCE_WIDTH_DP.dp, REFERENCE_HEIGHT_DP.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                content()
            }
        }
    }
}

/** A synthetic, gently moving spectrum used to preview a design without any audio playing. */
internal fun demoFrame(timeNanos: Long, bands: Int = DEMO_BANDS): SpectrumFrame {
    val seconds = (timeNanos / NANOS_PER_MILLI % WRAP_MILLIS) / MILLIS_PER_SECOND
    val levels = FloatArray(bands) { index ->
        val falloff = 1f - DEMO_FALLOFF * index / bands
        val wave = 0.5f + 0.5f * sin(seconds * DEMO_SPEED + index * DEMO_SPREAD)
        ((DEMO_BASE + DEMO_RANGE * wave) * falloff).coerceIn(0f, 1f)
    }
    return SpectrumFrame(levels, timeNanos)
}

@Composable
private fun rememberDemoFrame(enabled: Boolean): SpectrumFrame {
    var nanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(enabled) {
        if (enabled) {
            while (true) withFrameNanos { nanos = it }
        }
    }
    return remember(nanos) { demoFrame(nanos) }
}

@Composable
fun VisualizerPreviewCanvas(
    config: VisualizerConfig,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    liveFrame: SpectrumFrame? = null
) {
    val demo = rememberDemoFrame(enabled = liveFrame == null)
    val corners = with(LocalDensity.current) { CornerRadii.uniform(PREVIEW_CORNER_DP.dp.toPx()) }
    PhoneFrame(modifier = modifier) {
        VisualizerCanvas(frame = liveFrame ?: demo, config = config, colors = colors, corners = corners)
    }
}

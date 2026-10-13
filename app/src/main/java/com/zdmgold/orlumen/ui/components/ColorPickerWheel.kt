package com.zdmgold.orlumen.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.zdmgold.orlumen.R
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

private const val OPAQUE = 0xFF000000.toInt()
private const val HUE_STEP = 60f
private const val HUE_STOPS = 6
private const val FULL_TURN = 360f
private const val CHANNEL_MAX = 255f
private const val CHANNEL_MASK = 0xFF
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val MARKER_RADIUS_DP = 10f
private const val MARKER_STROKE_DP = 3f

internal object ColorWheelMath {
    fun hsvToArgb(hue: Float, saturation: Float, value: Float): Int {
        val h = ((hue % FULL_TURN) + FULL_TURN) % FULL_TURN
        val s = saturation.coerceIn(0f, 1f)
        val v = value.coerceIn(0f, 1f)
        val chroma = v * s
        val x = chroma * (1f - abs((h / HUE_STEP) % 2f - 1f))
        val m = v - chroma
        val (red, green, blue) = when ((h / HUE_STEP).toInt()) {
            0 -> Triple(chroma, x, 0f)
            1 -> Triple(x, chroma, 0f)
            2 -> Triple(0f, chroma, x)
            3 -> Triple(0f, x, chroma)
            4 -> Triple(x, 0f, chroma)
            else -> Triple(chroma, 0f, x)
        }
        return OPAQUE or (channel(red + m) shl RED_SHIFT) or (channel(green + m) shl GREEN_SHIFT) or channel(blue + m)
    }

    /** Returns hue in degrees, saturation and value, each saturation and value in 0..1. */
    fun argbToHsv(argb: Int): FloatArray {
        val red = (argb shr RED_SHIFT and CHANNEL_MASK) / CHANNEL_MAX
        val green = (argb shr GREEN_SHIFT and CHANNEL_MASK) / CHANNEL_MAX
        val blue = (argb and CHANNEL_MASK) / CHANNEL_MAX
        val maximum = maxOf(red, green, blue)
        val delta = maximum - minOf(red, green, blue)
        val hue = when {
            delta == 0f -> 0f
            maximum == red -> HUE_STEP * ((green - blue) / delta).mod(HUE_STOPS.toFloat())
            maximum == green -> HUE_STEP * ((blue - red) / delta + 2f)
            else -> HUE_STEP * ((red - green) / delta + 4f)
        }
        val saturation = if (maximum == 0f) 0f else delta / maximum
        return floatArrayOf(hue, saturation, maximum)
    }

    /** Hue is the angle clockwise from the right-hand side; saturation grows with distance. */
    fun hueSaturationAt(dx: Float, dy: Float, radius: Float): Pair<Float, Float> {
        val degrees = (atan2(dy, dx) * 180.0 / PI).toFloat()
        val hue = if (degrees < 0f) degrees + FULL_TURN else degrees
        return hue to (hypot(dx, dy) / radius).coerceIn(0f, 1f)
    }

    private fun channel(value: Float): Int = (value * CHANNEL_MAX + 0.5f).toInt().coerceIn(0, CHANNEL_MASK)
}

@Composable
fun ColorPickerWheel(colorArgb: Int, onColorChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val hsv = remember(colorArgb) { ColorWheelMath.argbToHsv(colorArgb) }
    val currentHsv by rememberUpdatedState(hsv)
    val currentCallback by rememberUpdatedState(onColorChange)
    val hueColors = remember {
        List(HUE_STOPS + 1) { Color(ColorWheelMath.hsvToArgb(it * HUE_STEP, 1f, 1f)) }
    }

    fun pick(position: Offset, size: IntSize) {
        val radius = min(size.width, size.height) / 2f
        val (hue, saturation) = ColorWheelMath.hueSaturationAt(
            position.x - size.width / 2f,
            position.y - size.height / 2f,
            radius
        )
        currentCallback(ColorWheelMath.hsvToArgb(hue, saturation, currentHsv[2]))
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .pointerInput(Unit) { detectTapGestures { pick(it, size) } }
                .pointerInput(Unit) {
                    detectDragGestures(onDragStart = { pick(it, size) }) { change, _ -> pick(change.position, size) }
                }
        ) {
            val radius = min(size.width, size.height) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Brush.sweepGradient(hueColors, center), radius, center)
            drawCircle(Brush.radialGradient(listOf(Color.White, Color.Transparent), center, radius), radius, center)
            drawCircle(Color.Black.copy(alpha = 1f - hsv[2]), radius, center)
            val angle = (hsv[0] * PI / 180.0).toFloat()
            val marker = Offset(center.x + hsv[1] * radius * cos(angle), center.y + hsv[1] * radius * sin(angle))
            drawCircle(Color(colorArgb), MARKER_RADIUS_DP.dp.toPx(), marker)
            drawCircle(Color.White, MARKER_RADIUS_DP.dp.toPx(), marker, style = Stroke(MARKER_STROKE_DP.dp.toPx()))
        }
        AppSlider(
            label = stringResource(R.string.picker_brightness),
            value = hsv[2],
            onValueChange = { currentCallback(ColorWheelMath.hsvToArgb(hsv[0], hsv[1], it)) },
            valueRange = 0f..1f
        )
    }
}

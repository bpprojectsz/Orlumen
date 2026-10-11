package com.zdmgold.orlumen.core.utils

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

private const val MAX_SWATCHES = 16
private const val MIN_VISIBLE_BRIGHTNESS = 0.45f
private const val CHANNEL_MASK = 0xFF
private const val MAX_CHANNEL = 255f
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val RED_WEIGHT = 0.299f
private const val GREEN_WEIGHT = 0.587f
private const val BLUE_WEIGHT = 0.114f
private const val OPAQUE_ALPHA = 0xFF000000.toInt()

fun Int.perceivedBrightness(): Float {
    val red = (this shr RED_SHIFT and CHANNEL_MASK) / MAX_CHANNEL
    val green = (this shr GREEN_SHIFT and CHANNEL_MASK) / MAX_CHANNEL
    val blue = (this and CHANNEL_MASK) / MAX_CHANNEL
    return RED_WEIGHT * red + GREEN_WEIGHT * green + BLUE_WEIGHT * blue
}

fun Int.redChannel(): Int = this shr RED_SHIFT and CHANNEL_MASK

fun Int.greenChannel(): Int = this shr GREEN_SHIFT and CHANNEL_MASK

fun Int.blueChannel(): Int = this and CHANNEL_MASK

fun Int.blendedTowardWhite(amount: Float): Int = (this and OPAQUE_ALPHA) or
    blendChannel(redChannel(), amount, RED_SHIFT) or
    blendChannel(greenChannel(), amount, GREEN_SHIFT) or
    blendChannel(blueChannel(), amount, 0)

fun Int.liftedForDarkBackground(minBrightness: Float = MIN_VISIBLE_BRIGHTNESS): Int {
    val brightness = perceivedBrightness()
    if (brightness >= minBrightness) return this
    return blendedTowardWhite((minBrightness - brightness) / (1f - brightness))
}

fun List<Int>.liftedForDarkBackground(): List<Int> = map { it.liftedForDarkBackground() }

fun Bitmap.dominantColors(count: Int): List<Int> {
    val palette = Palette.from(this).maximumColorCount(MAX_SWATCHES).generate()
    return palette.swatches
        .sortedByDescending { it.population }
        .take(count)
        .map { it.rgb or OPAQUE_ALPHA }
}

private fun blendChannel(channel: Int, amount: Float, shift: Int): Int {
    val blended = (channel + (MAX_CHANNEL - channel) * amount).toInt().coerceIn(0, CHANNEL_MASK)
    return blended shl shift
}

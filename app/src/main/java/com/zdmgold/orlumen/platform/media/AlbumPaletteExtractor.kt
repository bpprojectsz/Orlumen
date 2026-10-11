package com.zdmgold.orlumen.platform.media

import android.graphics.Bitmap
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.utils.blendedTowardWhite
import com.zdmgold.orlumen.core.utils.blueChannel
import com.zdmgold.orlumen.core.utils.dominantColors
import com.zdmgold.orlumen.core.utils.greenChannel
import com.zdmgold.orlumen.core.utils.liftedForDarkBackground
import com.zdmgold.orlumen.core.utils.redChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val ALBUM_PALETTE_ID = "album_art"

private const val CANDIDATE_COLORS = 6
private const val MAX_PALETTE_COLORS = 3
private const val DUPLICATE_DISTANCE = 96
private const val COMPANION_BLEND = 0.35f

fun buildAlbumPalette(swatches: List<Int>): ColorPalette? {
    val distinct = mutableListOf<Int>()
    for (color in swatches.map { it.liftedForDarkBackground() }) {
        if (distinct.none { colorDistance(it, color) < DUPLICATE_DISTANCE }) distinct += color
        if (distinct.size == MAX_PALETTE_COLORS) break
    }
    val colors = when (distinct.size) {
        0 -> return null
        1 -> listOf(distinct[0], distinct[0].blendedTowardWhite(COMPANION_BLEND))
        else -> distinct
    }
    return ColorPalette(
        id = ALBUM_PALETTE_ID,
        name = "Album art",
        colorsArgb = colors,
        isBuiltIn = false,
        nameRes = R.string.palette_album_art
    )
}

private fun colorDistance(first: Int, second: Int): Int = abs(first.redChannel() - second.redChannel()) +
    abs(first.greenChannel() - second.greenChannel()) +
    abs(first.blueChannel() - second.blueChannel())

@Singleton
class AlbumPaletteExtractor @Inject constructor() {
    @Volatile
    private var cached: Pair<String, ColorPalette?>? = null

    suspend fun extract(key: String, art: Bitmap): ColorPalette? {
        cached?.let { if (it.first == key) return it.second }
        val palette = withContext(Dispatchers.Default) {
            buildAlbumPalette(art.dominantColors(CANDIDATE_COLORS))
        }
        cached = key to palette
        return palette
    }
}

package com.zdmgold.orlumen.platform.media

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import com.zdmgold.orlumen.R
import com.zdmgold.orlumen.core.model.ColorPalette
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

const val WALLPAPER_PALETTE_ID = "wallpaper"

fun wallpaperPalette(colors: List<Int>): ColorPalette? = buildAlbumPalette(colors)?.copy(
    id = WALLPAPER_PALETTE_ID,
    name = "Wallpaper",
    nameRes = R.string.palette_wallpaper
)

/** Reads the dominant colors of the current system wallpaper (Android 8.1 and newer). */
@Singleton
class WallpaperColorsProvider @Inject constructor(@ApplicationContext private val context: Context) {
    fun colors(): List<Int> {
        val found = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            WallpaperManager.getInstance(context).getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
        } else {
            null
        }
        return found
            ?.let { listOfNotNull(it.primaryColor, it.secondaryColor, it.tertiaryColor).map(Color::toArgb) }
            .orEmpty()
    }
}

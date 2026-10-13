package com.zdmgold.orlumen.core.viewmodel

import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.model.StockPalettes
import com.zdmgold.orlumen.platform.media.ALBUM_PALETTE_ID
import com.zdmgold.orlumen.platform.media.WALLPAPER_PALETTE_ID
import com.zdmgold.orlumen.platform.media.wallpaperPalette
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteViewModelTest {
    private fun palette(id: String, builtIn: Boolean) = ColorPalette(id, "Name", listOf(-1, -2), builtIn)

    @Test
    fun computedPalettesMustBeSavedBeforeTheyAreSelected() {
        assertTrue(needsSaving(palette(ALBUM_PALETTE_ID, builtIn = false)))
        assertTrue(needsSaving(palette(WALLPAPER_PALETTE_ID, builtIn = false)))
    }

    @Test
    fun stockAndCustomPalettesAreUsedAsTheyAre() {
        assertFalse(needsSaving(StockPalettes.all.first()))
        assertFalse(needsSaving(palette("custom_123", builtIn = false)))
    }

    @Test
    fun wallpaperPaletteNeedsColors() {
        assertNull(wallpaperPalette(emptyList()))
    }

    @Test
    fun wallpaperPaletteGetsItsOwnIdentity() {
        val result = wallpaperPalette(listOf(0xFF9B6BFF.toInt(), 0xFFFF5FD2.toInt()))
        assertNotNull(result)
        assertEquals(WALLPAPER_PALETTE_ID, result!!.id)
        assertNotNull(result.nameRes)
        assertFalse(result.isBuiltIn)
    }
}

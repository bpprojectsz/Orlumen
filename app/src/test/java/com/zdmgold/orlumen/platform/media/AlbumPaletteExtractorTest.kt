package com.zdmgold.orlumen.platform.media

import com.zdmgold.orlumen.core.utils.perceivedBrightness
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumPaletteExtractorTest {
    private val violet = 0xFF9B6BFF.toInt()
    private val nearViolet = 0xFF9C6CFF.toInt()
    private val pink = 0xFFFF5FD2.toInt()
    private val teal = 0xFF2DE2C9.toInt()
    private val amber = 0xFFFFC857.toInt()

    @Test
    fun emptySwatchesGiveNoPalette() {
        assertNull(buildAlbumPalette(emptyList()))
    }

    @Test
    fun singleColorGetsABrighterCompanion() {
        val palette = buildAlbumPalette(listOf(violet))
        assertNotNull(palette)
        assertEquals(2, palette!!.colorsArgb.size)
        assertTrue(palette.colorsArgb[1].perceivedBrightness() > palette.colorsArgb[0].perceivedBrightness())
    }

    @Test
    fun nearDuplicatesAreCollapsed() {
        val palette = buildAlbumPalette(listOf(violet, nearViolet, pink))
        assertEquals(2, palette!!.colorsArgb.size)
    }

    @Test
    fun atMostThreeColorsAreKept() {
        val palette = buildAlbumPalette(listOf(violet, pink, teal, amber))
        assertEquals(3, palette!!.colorsArgb.size)
        assertEquals(listOf(violet, pink, teal), palette.colorsArgb)
    }

    @Test
    fun darkSwatchesAreLiftedToBeVisible() {
        val palette = buildAlbumPalette(listOf(0xFF101020.toInt(), 0xFF200820.toInt()))
        assertTrue(palette!!.colorsArgb.all { it.perceivedBrightness() >= 0.44f })
    }

    @Test
    fun resultIsACustomPaletteWithAStableId() {
        val palette = buildAlbumPalette(listOf(violet, pink))
        assertEquals(ALBUM_PALETTE_ID, palette!!.id)
        assertFalse(palette.isBuiltIn)
    }
}

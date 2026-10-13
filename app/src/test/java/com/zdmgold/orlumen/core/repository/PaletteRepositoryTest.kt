package com.zdmgold.orlumen.core.repository

import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.model.StockPalettes
import com.zdmgold.orlumen.testing.FakePaletteDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PaletteRepositoryTest {
    private val repository = PaletteRepository(FakePaletteDao())

    @Test
    fun stockPalettesAreAlwaysPresentAndValid() = runTest {
        val palettes = repository.palettes.first()
        assertEquals(StockPalettes.all, palettes)
        assertTrue(palettes.all { it.isBuiltIn && it.nameRes != null })
        assertEquals(StockPalettes.all.size, StockPalettes.all.map { it.id }.toSet().size)
        assertNotNull(repository.findById("obsidian_violet"))
    }

    @Test
    fun customPaletteIsSavedFoundAndDeleted() = runTest {
        val saved = repository.saveCustomPalette("Mine", listOf(-65536, -16711936, -16776961))
        assertFalse(saved.isBuiltIn)
        assertTrue(saved.id.startsWith(PaletteRepository.CUSTOM_ID_PREFIX))
        assertEquals(saved, repository.findById(saved.id))
        assertEquals(StockPalettes.all.size + 1, repository.palettes.first().size)
        assertEquals(listOf(saved), repository.customPalettes.first())

        repository.deleteCustomPalette(saved.id)
        assertNull(repository.findById(saved.id))
        assertEquals(StockPalettes.all, repository.palettes.first())
    }

    @Test
    fun paletteNeedsAtLeastTwoColors() = runTest {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.saveCustomPalette("Bad", listOf(-1)) }
        }
    }

    @Test
    fun resolvePaletteHonoursAlbumColorsThenSelectionThenFallback() {
        val album = ColorPalette(id = "album_art", name = "Album", colorsArgb = listOf(-1, -2), isBuiltIn = false)
        val custom = ColorPalette(id = "mine", name = "Mine", colorsArgb = listOf(-3, -4), isBuiltIn = false)
        val all = StockPalettes.all + custom
        assertEquals(album, resolvePalette("mine", all, album, autoApplyAlbumColors = true))
        assertEquals(custom, resolvePalette("mine", all, album, autoApplyAlbumColors = false))
        assertEquals(custom, resolvePalette("mine", all, null, autoApplyAlbumColors = true))
        assertEquals(StockPalettes.all.first(), resolvePalette("missing", all, null, autoApplyAlbumColors = false))
    }
}

package com.zdmgold.orlumen.core.repository

import com.zdmgold.orlumen.core.database.PaletteDao
import com.zdmgold.orlumen.core.database.PaletteEntity
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.model.StockPalettes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

private class FakePaletteDao : PaletteDao {
    private val rows = MutableStateFlow<List<PaletteEntity>>(emptyList())

    override fun observeAll(): Flow<List<PaletteEntity>> = rows

    override suspend fun getById(id: String): PaletteEntity? = rows.value.firstOrNull { it.id == id }

    override suspend fun upsert(palette: PaletteEntity) {
        rows.value = rows.value.filterNot { it.id == palette.id } + palette
    }

    override suspend fun delete(id: String) {
        rows.value = rows.value.filterNot { it.id == id }
    }
}

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

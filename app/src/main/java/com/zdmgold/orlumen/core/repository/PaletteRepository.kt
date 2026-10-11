package com.zdmgold.orlumen.core.repository

import com.zdmgold.orlumen.core.database.PaletteDao
import com.zdmgold.orlumen.core.database.PaletteEntity
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.model.StockPalettes
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PaletteRepository @Inject constructor(private val paletteDao: PaletteDao) {
    val palettes: Flow<List<ColorPalette>> = paletteDao.observeAll()
        .map { saved -> StockPalettes.all + saved.map { it.toModel() } }

    val customPalettes: Flow<List<ColorPalette>> = paletteDao.observeAll()
        .map { saved -> saved.map { it.toModel() } }

    suspend fun findById(id: String): ColorPalette? = StockPalettes.byId(id) ?: paletteDao.getById(id)?.toModel()

    suspend fun saveCustomPalette(name: String, colorsArgb: List<Int>): ColorPalette {
        val palette = ColorPalette(
            id = CUSTOM_ID_PREFIX + UUID.randomUUID(),
            name = name,
            colorsArgb = colorsArgb,
            isBuiltIn = false
        )
        paletteDao.upsert(
            PaletteEntity(
                id = palette.id,
                name = palette.name,
                colors = palette.colorsArgb,
                createdAtMillis = System.currentTimeMillis()
            )
        )
        return palette
    }

    suspend fun deleteCustomPalette(id: String) = paletteDao.delete(id)

    private fun PaletteEntity.toModel(): ColorPalette =
        ColorPalette(id = id, name = name, colorsArgb = colors, isBuiltIn = false)

    companion object {
        const val CUSTOM_ID_PREFIX = "custom_"
    }
}

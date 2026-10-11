package com.zdmgold.orlumen.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaletteDao {
    @Query("SELECT * FROM palettes ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<PaletteEntity>>

    @Query("SELECT * FROM palettes WHERE id = :id")
    suspend fun getById(id: String): PaletteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(palette: PaletteEntity)

    @Query("DELETE FROM palettes WHERE id = :id")
    suspend fun delete(id: String)
}

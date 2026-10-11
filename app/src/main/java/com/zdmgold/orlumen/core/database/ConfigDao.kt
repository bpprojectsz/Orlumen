package com.zdmgold.orlumen.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.zdmgold.orlumen.core.model.DesignKind
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {
    @Query("SELECT * FROM presets WHERE kind = :kind ORDER BY createdAtMillis DESC")
    fun observePresets(kind: DesignKind): Flow<List<PresetEntity>>

    @Query("SELECT * FROM presets WHERE id = :id")
    suspend fun getPreset(id: Long): PresetEntity?

    @Insert
    suspend fun insert(preset: PresetEntity): Long

    @Update
    suspend fun update(preset: PresetEntity)

    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun delete(id: Long)
}

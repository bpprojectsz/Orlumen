package com.zdmgold.orlumen.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zdmgold.orlumen.core.model.DesignKind

@Entity(tableName = "presets", indices = [Index("kind")])
data class PresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: DesignKind,
    val designId: String,
    val paletteId: String,
    val numbers: Map<String, Float>,
    val texts: Map<String, String>,
    val createdAtMillis: Long
)

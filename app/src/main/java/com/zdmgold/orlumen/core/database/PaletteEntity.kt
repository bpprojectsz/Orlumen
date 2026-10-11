package com.zdmgold.orlumen.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "palettes")
data class PaletteEntity(@PrimaryKey val id: String, val name: String, val colors: List<Int>, val createdAtMillis: Long)

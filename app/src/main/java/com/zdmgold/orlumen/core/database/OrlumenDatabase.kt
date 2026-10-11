package com.zdmgold.orlumen.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PresetEntity::class, PaletteEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class OrlumenDatabase : RoomDatabase() {
    abstract fun configDao(): ConfigDao

    abstract fun paletteDao(): PaletteDao

    companion object {
        const val NAME = "orlumen.db"
    }
}

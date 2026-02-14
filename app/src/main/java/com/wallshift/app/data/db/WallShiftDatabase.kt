package com.wallshift.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WallpaperHistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WallShiftDatabase : RoomDatabase() {
    abstract fun wallpaperHistoryDao(): WallpaperHistoryDao
}

package com.wallshift.app.di

import android.content.Context
import androidx.room.Room
import com.wallshift.app.data.db.WallShiftDatabase
import com.wallshift.app.data.db.WallpaperHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WallShiftDatabase =
        Room.databaseBuilder(
            context,
            WallShiftDatabase::class.java,
            "wallshift_database",
        ).build()

    @Provides
    @Singleton
    fun provideWallpaperHistoryDao(database: WallShiftDatabase): WallpaperHistoryDao =
        database.wallpaperHistoryDao()
}

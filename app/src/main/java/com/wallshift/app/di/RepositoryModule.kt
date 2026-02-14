package com.wallshift.app.di

import com.wallshift.app.data.repository.SettingsRepositoryImpl
import com.wallshift.app.data.repository.WallpaperRepositoryImpl
import com.wallshift.app.domain.repository.SettingsRepository
import com.wallshift.app.domain.repository.WallpaperRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWallpaperRepository(
        impl: WallpaperRepositoryImpl,
    ): WallpaperRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository
}

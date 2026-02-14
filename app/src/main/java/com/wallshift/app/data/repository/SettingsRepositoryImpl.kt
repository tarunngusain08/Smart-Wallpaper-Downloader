package com.wallshift.app.data.repository

import com.wallshift.app.domain.model.WallpaperTarget
import com.wallshift.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
) : SettingsRepository {

    override fun getSelectedCategories(): Flow<Set<String>> =
        dataStore.selectedCategories

    override suspend fun setSelectedCategories(categories: Set<String>) =
        dataStore.setSelectedCategories(categories)

    override fun getCustomCategories(): Flow<Set<String>> =
        dataStore.customCategories

    override suspend fun setCustomCategories(categories: Set<String>) =
        dataStore.setCustomCategories(categories)

    override fun getFrequencyMinutes(): Flow<Int> =
        dataStore.frequencyMinutes

    override suspend fun setFrequencyMinutes(minutes: Int) =
        dataStore.setFrequencyMinutes(minutes)

    override fun getWallpaperTarget(): Flow<WallpaperTarget> =
        dataStore.wallpaperTarget

    override suspend fun setWallpaperTarget(target: WallpaperTarget) =
        dataStore.setWallpaperTarget(target)

    override fun isAutoChangeEnabled(): Flow<Boolean> =
        dataStore.autoChangeEnabled

    override suspend fun setAutoChangeEnabled(enabled: Boolean) =
        dataStore.setAutoChangeEnabled(enabled)

    override fun getCacheSizeMb(): Flow<Int> =
        dataStore.cacheSizeMb

    override suspend fun setCacheSizeMb(sizeMb: Int) =
        dataStore.setCacheSizeMb(sizeMb)

    override fun isOnboarded(): Flow<Boolean> =
        dataStore.isOnboarded

    override suspend fun setOnboarded(onboarded: Boolean) =
        dataStore.setOnboarded(onboarded)
}

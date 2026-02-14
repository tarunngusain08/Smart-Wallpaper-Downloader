package com.wallshift.app.domain.repository

import com.wallshift.app.domain.model.WallpaperTarget
import kotlinx.coroutines.flow.Flow

/** Repository interface for user settings / preferences. */
interface SettingsRepository {

    fun getSelectedCategories(): Flow<Set<String>>
    suspend fun setSelectedCategories(categories: Set<String>)

    fun getCustomCategories(): Flow<Set<String>>
    suspend fun setCustomCategories(categories: Set<String>)

    fun getFrequencyMinutes(): Flow<Int>
    suspend fun setFrequencyMinutes(minutes: Int)

    fun getWallpaperTarget(): Flow<WallpaperTarget>
    suspend fun setWallpaperTarget(target: WallpaperTarget)

    fun isAutoChangeEnabled(): Flow<Boolean>
    suspend fun setAutoChangeEnabled(enabled: Boolean)

    fun getCacheSizeMb(): Flow<Int>
    suspend fun setCacheSizeMb(sizeMb: Int)

    fun isOnboarded(): Flow<Boolean>
    suspend fun setOnboarded(onboarded: Boolean)
}

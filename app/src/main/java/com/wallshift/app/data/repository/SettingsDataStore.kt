package com.wallshift.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.wallshift.app.domain.model.WallpaperTarget
import com.wallshift.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Typed wrapper around DataStore<Preferences> for app settings. */
@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private companion object {
        val KEY_SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")
        val KEY_FREQUENCY_MINUTES = intPreferencesKey("change_frequency_minutes")
        val KEY_WALLPAPER_TARGET = stringPreferencesKey("wallpaper_target")
        val KEY_AUTO_CHANGE_ENABLED = booleanPreferencesKey("auto_change_enabled")
        val KEY_CACHE_SIZE_MB = intPreferencesKey("cache_size_mb")
        val KEY_CUSTOM_CATEGORIES = stringSetPreferencesKey("custom_categories")
        val KEY_IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
    }

    // --- Categories ---

    val selectedCategories: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_CATEGORIES] ?: Constants.DEFAULT_CATEGORIES
    }

    suspend fun setSelectedCategories(categories: Set<String>) {
        dataStore.edit { it[KEY_SELECTED_CATEGORIES] = categories }
    }

    // --- Custom Categories ---

    val customCategories: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_CATEGORIES] ?: emptySet()
    }

    suspend fun setCustomCategories(categories: Set<String>) {
        dataStore.edit { it[KEY_CUSTOM_CATEGORIES] = categories }
    }

    // --- Frequency ---

    val frequencyMinutes: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_FREQUENCY_MINUTES] ?: Constants.DEFAULT_FREQUENCY_MINUTES
    }

    suspend fun setFrequencyMinutes(minutes: Int) {
        dataStore.edit { it[KEY_FREQUENCY_MINUTES] = minutes }
    }

    // --- Wallpaper Target ---

    val wallpaperTarget: Flow<WallpaperTarget> = dataStore.data.map { prefs ->
        val value = prefs[KEY_WALLPAPER_TARGET] ?: WallpaperTarget.BOTH.name
        WallpaperTarget.fromString(value)
    }

    suspend fun setWallpaperTarget(target: WallpaperTarget) {
        dataStore.edit { it[KEY_WALLPAPER_TARGET] = target.name }
    }

    // --- Auto Change ---

    val autoChangeEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AUTO_CHANGE_ENABLED] ?: true
    }

    suspend fun setAutoChangeEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_CHANGE_ENABLED] = enabled }
    }

    // --- Cache Size ---

    val cacheSizeMb: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_CACHE_SIZE_MB] ?: Constants.DEFAULT_CACHE_SIZE_MB
    }

    suspend fun setCacheSizeMb(sizeMb: Int) {
        dataStore.edit { it[KEY_CACHE_SIZE_MB] = sizeMb }
    }

    // --- Onboarding ---

    val isOnboarded: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_IS_ONBOARDED] ?: false
    }

    suspend fun setOnboarded(onboarded: Boolean) {
        dataStore.edit { it[KEY_IS_ONBOARDED] = onboarded }
    }
}

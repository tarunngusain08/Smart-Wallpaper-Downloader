package com.wallshift.app.domain.repository

import com.wallshift.app.data.db.WallpaperHistoryEntity
import com.wallshift.app.domain.model.WallpaperImage
import kotlinx.coroutines.flow.Flow

/** Repository interface for wallpaper data operations. */
interface WallpaperRepository {

    /** Fetch wallpapers from remote APIs for the given [categories]. */
    suspend fun fetchWallpapers(
        categories: Set<String>,
        page: Int = 1,
    ): List<WallpaperImage>

    /** Returns IDs of all previously applied wallpapers. */
    suspend fun getAppliedIds(): List<String>

    /** Returns a map of image ID to applied-at timestamp (epoch millis). */
    suspend fun getAppliedTimestamps(): Map<String, Long>

    /** Mark an image as applied at the current time. */
    suspend fun markApplied(imageId: String)

    /** Download an image and cache it locally. Returns the local file path. */
    suspend fun downloadAndCache(image: WallpaperImage): String?

    /** Returns the most recently applied wallpaper entity. */
    suspend fun getCurrentWallpaper(): WallpaperHistoryEntity?

    /** Observe the current wallpaper reactively. */
    fun observeCurrentWallpaper(): Flow<WallpaperHistoryEntity?>

    /** Store fetched images in the local database. */
    suspend fun saveToHistory(images: List<WallpaperImage>)

    /** Clear all wallpaper history. */
    suspend fun clearHistory()

    /** Fallback search with a generic query when category-specific search yields no results. */
    suspend fun searchFallback(query: String): List<WallpaperImage>
}

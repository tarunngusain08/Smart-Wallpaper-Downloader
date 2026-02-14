package com.wallshift.app.data.repository

import com.wallshift.app.data.api.WallpaperApiAggregator
import com.wallshift.app.data.cache.ImageCacheManager
import com.wallshift.app.data.db.WallpaperHistoryDao
import com.wallshift.app.data.db.WallpaperHistoryEntity
import com.wallshift.app.domain.model.WallpaperImage
import com.wallshift.app.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperRepositoryImpl @Inject constructor(
    private val apiAggregator: WallpaperApiAggregator,
    private val dao: WallpaperHistoryDao,
    private val cacheManager: ImageCacheManager,
    private val settingsDataStore: SettingsDataStore,
) : WallpaperRepository {

    override suspend fun fetchWallpapers(
        categories: Set<String>,
        page: Int,
    ): List<WallpaperImage> {
        return apiAggregator.searchCategories(categories, page)
    }

    override suspend fun getAppliedIds(): List<String> {
        return dao.getAppliedImageIds()
    }

    override suspend fun getAppliedTimestamps(): Map<String, Long> {
        return dao.getAppliedImageTimestamps().associate { it.id to it.appliedAt }
    }

    override suspend fun markApplied(imageId: String) {
        dao.markApplied(imageId, System.currentTimeMillis())
    }

    override suspend fun downloadAndCache(image: WallpaperImage): String? {
        val fileName = "${image.id}.jpg"
        val existingPath = cacheManager.getCachedPath(fileName)
        if (existingPath != null) return existingPath

        val localPath = cacheManager.saveImage(image.downloadUrl, fileName)

        if (localPath != null) {
            // Store in DB and update local path
            dao.insert(image.toEntity())
            dao.updateLocalPath(image.id, localPath)

            // Evict old files if cache is too large
            val maxCacheMb = settingsDataStore.cacheSizeMb.firstOrNull()
                ?: com.wallshift.app.util.Constants.DEFAULT_CACHE_SIZE_MB
            cacheManager.evictIfNeeded(maxCacheMb)
        }

        return localPath
    }

    override suspend fun getCurrentWallpaper(): WallpaperHistoryEntity? {
        return dao.getCurrentWallpaper()
    }

    override fun observeCurrentWallpaper(): Flow<WallpaperHistoryEntity?> {
        return dao.observeCurrentWallpaper()
    }

    override suspend fun saveToHistory(images: List<WallpaperImage>) {
        dao.insertAll(images.map { it.toEntity() })
    }

    override suspend fun clearHistory() {
        dao.clearAll()
        cacheManager.clearCache()
    }

    override suspend fun searchFallback(query: String): List<WallpaperImage> {
        return apiAggregator.search(query)
    }
}

private fun WallpaperImage.toEntity(): WallpaperHistoryEntity =
    WallpaperHistoryEntity(
        id = id,
        source = source.name,
        category = category,
        downloadUrl = downloadUrl,
        thumbnailUrl = thumbnailUrl,
        likes = likes,
        width = width,
        height = height,
        photographer = photographer,
        attributionUrl = attributionUrl,
    )

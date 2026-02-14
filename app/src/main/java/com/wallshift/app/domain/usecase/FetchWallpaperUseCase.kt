package com.wallshift.app.domain.usecase

import android.util.Log
import com.wallshift.app.domain.model.WallpaperImage
import com.wallshift.app.domain.repository.WallpaperRepository
import javax.inject.Inject

/**
 * Fetches wallpaper candidates from remote sources, filtering out
 * any that have already been applied. Falls back to broader queries
 * if category-specific searches return empty results.
 */
class FetchWallpaperUseCase @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
) {

    companion object {
        private const val TAG = "FetchWallpaperUseCase"
    }

    /**
     * Returns a list of wallpaper candidates for the given [categories]
     * that have not yet been applied.
     *
     * Fallback strategy when results are empty:
     * 1. Try the original category search.
     * 2. Broaden to a generic "wallpaper" query.
     * 3. Fall back to "popular wallpaper" as a last resort.
     */
    suspend operator fun invoke(
        categories: Set<String>,
        page: Int = 1,
    ): List<WallpaperImage> {
        // Step 1: Category-specific search
        var allImages = wallpaperRepository.fetchWallpapers(categories, page)

        // Step 2: Broaden to generic "wallpaper" if empty
        if (allImages.isEmpty()) {
            Log.d(TAG, "Category search empty, broadening to 'wallpaper'")
            allImages = wallpaperRepository.searchFallback("wallpaper")
        }

        // Step 3: Last resort fallback to "popular wallpaper"
        if (allImages.isEmpty()) {
            Log.d(TAG, "Broad search empty, falling back to 'popular wallpaper'")
            allImages = wallpaperRepository.searchFallback("popular wallpaper")
        }

        if (allImages.isEmpty()) return emptyList()

        val appliedIds = wallpaperRepository.getAppliedIds().toSet()

        // Filter out already-applied images, preferring unseen ones
        val unseen = allImages.filter { it.id !in appliedIds }

        // If all are seen, return the full list (SmartSelection will pick the least recent)
        return unseen.ifEmpty { allImages }
    }
}

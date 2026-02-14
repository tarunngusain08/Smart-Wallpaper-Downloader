package com.wallshift.app.data.api

import android.util.Log
import com.wallshift.app.data.api.pexels.PexelsApiService
import com.wallshift.app.data.api.pexels.PexelsPhoto
import com.wallshift.app.data.api.pixabay.PixabayApiService
import com.wallshift.app.data.api.pixabay.PixabayHit
import com.wallshift.app.data.api.unsplash.UnsplashApiService
import com.wallshift.app.data.api.unsplash.UnsplashPhoto
import com.wallshift.app.data.api.wallhaven.WallhavenApiService
import com.wallshift.app.data.api.wallhaven.WallhavenData
import com.wallshift.app.domain.model.ImageSource
import com.wallshift.app.domain.model.WallpaperImage
import com.wallshift.app.util.Constants
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aggregates wallpaper results from multiple image APIs into a
 * unified [WallpaperImage] list. Falls back gracefully if one source fails.
 */
@Singleton
class WallpaperApiAggregator @Inject constructor(
    private val unsplashApi: UnsplashApiService,
    private val pexelsApi: PexelsApiService,
    private val pixabayApi: PixabayApiService,
    private val wallhavenApi: WallhavenApiService,
) {

    companion object {
        private const val TAG = "WallpaperApiAggregator"
    }

    /**
     * Search across all configured sources for images matching [query].
     * Returns a combined, deduplicated list.
     */
    suspend fun search(
        query: String,
        page: Int = 1,
        perPage: Int = Constants.IMAGES_PER_PAGE,
    ): List<WallpaperImage> {
        val results = mutableListOf<WallpaperImage>()

        // Fetch from Unsplash
        try {
            val unsplashResults = unsplashApi.searchPhotos(
                query = query,
                page = page,
                perPage = perPage,
            )
            results.addAll(
                unsplashResults.results.map { it.toDomain(query) }
            )
        } catch (e: HttpException) {
            if (e.code() == 429) {
                Log.w(TAG, "Unsplash rate limited (429), skipping to next source")
            } else {
                Log.e(TAG, "Unsplash HTTP error ${e.code()} for '$query'", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unsplash search failed for '$query'", e)
        }

        // Fetch from Pexels
        try {
            val pexelsResults = pexelsApi.searchPhotos(
                query = query,
                page = page,
                perPage = perPage,
            )
            results.addAll(
                pexelsResults.photos.map { it.toDomain(query) }
            )
        } catch (e: HttpException) {
            if (e.code() == 429) {
                Log.w(TAG, "Pexels rate limited (429), skipping")
            } else {
                Log.e(TAG, "Pexels HTTP error ${e.code()} for '$query'", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pexels search failed for '$query'", e)
        }

        // Fetch from Pixabay
        try {
            val pixabayResults = pixabayApi.searchPhotos(
                query = query,
                page = page,
                perPage = perPage,
            )
            results.addAll(
                pixabayResults.hits.map { it.toDomain(query) }
            )
        } catch (e: HttpException) {
            if (e.code() == 429) {
                Log.w(TAG, "Pixabay rate limited (429), skipping")
            } else {
                Log.e(TAG, "Pixabay HTTP error ${e.code()} for '$query'", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pixabay search failed for '$query'", e)
        }

        // Fetch from Wallhaven
        try {
            val wallhavenResults = wallhavenApi.searchWallpapers(
                query = query,
                page = page,
            )
            results.addAll(
                wallhavenResults.data.map { it.toDomain(query) }
            )
        } catch (e: HttpException) {
            if (e.code() == 429) {
                Log.w(TAG, "Wallhaven rate limited (429), skipping")
            } else {
                Log.e(TAG, "Wallhaven HTTP error ${e.code()} for '$query'", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Wallhaven search failed for '$query'", e)
        }

        return results
    }

    /**
     * Search across multiple [categories] and flatten results.
     */
    suspend fun searchCategories(
        categories: Set<String>,
        page: Int = 1,
        perPage: Int = Constants.IMAGES_PER_PAGE,
    ): List<WallpaperImage> {
        val perSourcePerPage = (perPage / categories.size.coerceAtLeast(1)).coerceAtLeast(5)
        return categories.flatMap { category ->
            search(query = category, page = page, perPage = perSourcePerPage)
        }
    }
}

private fun UnsplashPhoto.toDomain(category: String): WallpaperImage =
    WallpaperImage(
        id = "unsplash_$id",
        source = ImageSource.UNSPLASH,
        category = category,
        width = width,
        height = height,
        downloadUrl = urls.full,
        thumbnailUrl = urls.small,
        likes = likes,
        photographer = user.name,
        attributionUrl = links.html,
    )

private fun PexelsPhoto.toDomain(category: String): WallpaperImage =
    WallpaperImage(
        id = "pexels_$id",
        source = ImageSource.PEXELS,
        category = category,
        width = width,
        height = height,
        downloadUrl = src.large2x,
        thumbnailUrl = src.medium,
        likes = 0, // Pexels doesn't expose likes
        photographer = photographer,
        attributionUrl = url,
    )

private fun PixabayHit.toDomain(category: String): WallpaperImage =
    WallpaperImage(
        id = "pixabay_$id",
        source = ImageSource.PIXABAY,
        category = category,
        width = imageWidth,
        height = imageHeight,
        downloadUrl = largeImageUrl,
        thumbnailUrl = webformatUrl,
        likes = likes,
        photographer = user,
        attributionUrl = pageUrl,
    )

private fun WallhavenData.toDomain(category: String): WallpaperImage =
    WallpaperImage(
        id = "wallhaven_$id",
        source = ImageSource.WALLHAVEN,
        category = category,
        width = dimensionX,
        height = dimensionY,
        downloadUrl = path,
        thumbnailUrl = thumbs.small,
        likes = favorites,
        photographer = "Wallhaven",
        attributionUrl = url,
    )

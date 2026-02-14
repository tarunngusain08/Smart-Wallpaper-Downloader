package com.wallshift.app.data.cache

import android.content.Context
import android.util.Log
import com.wallshift.app.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Manages a local file cache for downloaded wallpaper images.
 * Enforces LRU eviction when the cache exceeds the configured size limit.
 */
@Singleton
class ImageCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("imageDownload") private val okHttpClient: OkHttpClient,
) {

    companion object {
        private const val TAG = "ImageCacheManager"
    }

    private val cacheDir: File
        get() = File(context.cacheDir, Constants.WALLPAPER_CACHE_DIR).also {
            if (!it.exists()) it.mkdirs()
        }

    /**
     * Downloads an image from [url] and saves it to the cache.
     * Returns the absolute local file path, or null on failure.
     */
    suspend fun saveImage(url: String, fileName: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val file = File(cacheDir, fileName)
                if (file.exists()) return@withContext file.absolutePath

                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e(TAG, "Download failed: ${response.code} for $url")
                    return@withContext null
                }

                response.body?.byteStream()?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                file.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save image from $url", e)
                null
            }
        }

    /** Returns the local file path if the image is already cached. */
    fun getCachedPath(fileName: String): String? {
        val file = File(cacheDir, fileName)
        return if (file.exists()) file.absolutePath else null
    }

    /** Returns total cache size in bytes. */
    fun getCacheSizeBytes(): Long {
        return cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    /** Returns total cache size in megabytes. */
    fun getCacheSizeMb(): Long = getCacheSizeBytes() / (1024 * 1024)

    /**
     * Evicts oldest files until total cache size is under [maxSizeMb].
     */
    suspend fun evictIfNeeded(maxSizeMb: Int) = withContext(Dispatchers.IO) {
        val maxBytes = maxSizeMb.toLong() * 1024 * 1024
        var totalSize = getCacheSizeBytes()

        if (totalSize <= maxBytes) return@withContext

        val files = cacheDir.listFiles()
            ?.filter { it.isFile }
            ?.sortedBy { it.lastModified() }
            ?: return@withContext

        for (file in files) {
            if (totalSize <= maxBytes) break
            val size = file.length()
            if (file.delete()) {
                totalSize -= size
                Log.d(TAG, "Evicted cached file: ${file.name}")
            }
        }
    }

    /** Deletes all cached wallpaper images. */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        cacheDir.listFiles()?.forEach { it.delete() }
        Log.d(TAG, "Cache cleared")
    }
}

package com.wallshift.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperHistoryDao {

    /** Returns IDs of all images that have been applied as wallpaper. */
    @Query("SELECT id FROM wallpaper_history WHERE applied_at IS NOT NULL")
    suspend fun getAppliedImageIds(): List<String>

    /** Returns IDs with their applied timestamps for recency-based scoring. */
    @Query("SELECT id, applied_at AS appliedAt FROM wallpaper_history WHERE applied_at IS NOT NULL")
    suspend fun getAppliedImageTimestamps(): List<AppliedImageInfo>

    /** Returns the most recently applied wallpaper. */
    @Query(
        "SELECT * FROM wallpaper_history WHERE applied_at IS NOT NULL " +
            "ORDER BY applied_at DESC LIMIT 1"
    )
    suspend fun getCurrentWallpaper(): WallpaperHistoryEntity?

    /** Observe the most recently applied wallpaper reactively. */
    @Query(
        "SELECT * FROM wallpaper_history WHERE applied_at IS NOT NULL " +
            "ORDER BY applied_at DESC LIMIT 1"
    )
    fun observeCurrentWallpaper(): Flow<WallpaperHistoryEntity?>

    /** Returns the last [limit] applied wallpapers, most recent first. */
    @Query(
        "SELECT * FROM wallpaper_history WHERE applied_at IS NOT NULL " +
            "ORDER BY applied_at DESC LIMIT :limit"
    )
    suspend fun getRecentlyApplied(limit: Int): List<WallpaperHistoryEntity>

    /** Insert or update wallpapers in the history. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<WallpaperHistoryEntity>)

    /** Insert or update a single wallpaper entry. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WallpaperHistoryEntity)

    /** Mark an image as applied at the given [timestamp]. */
    @Query("UPDATE wallpaper_history SET applied_at = :timestamp WHERE id = :imageId")
    suspend fun markApplied(imageId: String, timestamp: Long)

    /** Update the local cached file path for an image. */
    @Query("UPDATE wallpaper_history SET local_path = :localPath WHERE id = :imageId")
    suspend fun updateLocalPath(imageId: String, localPath: String)

    /** Delete the oldest applied entries beyond [keepCount]. */
    @Query(
        "DELETE FROM wallpaper_history WHERE id IN (" +
            "SELECT id FROM wallpaper_history WHERE applied_at IS NOT NULL " +
            "ORDER BY applied_at ASC LIMIT :deleteCount)"
    )
    suspend fun deleteOldest(deleteCount: Int)

    /** Returns total count of stored wallpapers. */
    @Query("SELECT COUNT(*) FROM wallpaper_history")
    suspend fun getCount(): Int

    /** Delete all history records. */
    @Query("DELETE FROM wallpaper_history")
    suspend fun clearAll()
}

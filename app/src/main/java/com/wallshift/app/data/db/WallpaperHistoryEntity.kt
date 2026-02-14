package com.wallshift.app.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpaper_history")
data class WallpaperHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "download_url")
    val downloadUrl: String,

    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String,

    @ColumnInfo(name = "local_path")
    val localPath: String? = null,

    @ColumnInfo(name = "applied_at")
    val appliedAt: Long? = null,

    @ColumnInfo(name = "likes")
    val likes: Int = 0,

    @ColumnInfo(name = "width")
    val width: Int = 0,

    @ColumnInfo(name = "height")
    val height: Int = 0,

    @ColumnInfo(name = "photographer")
    val photographer: String = "",

    @ColumnInfo(name = "attribution_url")
    val attributionUrl: String = "",
)

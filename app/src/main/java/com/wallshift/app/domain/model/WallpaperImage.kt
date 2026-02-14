package com.wallshift.app.domain.model

/** Unified wallpaper image model used across the app. */
data class WallpaperImage(
    val id: String,
    val source: ImageSource,
    val category: String,
    val width: Int,
    val height: Int,
    val downloadUrl: String,
    val thumbnailUrl: String,
    val likes: Int,
    val photographer: String,
    val attributionUrl: String,
)

enum class ImageSource {
    UNSPLASH,
    PEXELS,
    PIXABAY,
    WALLHAVEN,
}

enum class WallpaperTarget {
    HOME,
    LOCK,
    BOTH;

    companion object {
        fun fromString(value: String): WallpaperTarget =
            entries.firstOrNull { it.name == value } ?: BOTH
    }
}

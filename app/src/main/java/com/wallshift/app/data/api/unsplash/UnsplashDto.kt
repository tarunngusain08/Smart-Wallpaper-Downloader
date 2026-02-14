package com.wallshift.app.data.api.unsplash

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnsplashSearchResponse(
    @Json(name = "total") val total: Int,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "results") val results: List<UnsplashPhoto>,
)

@JsonClass(generateAdapter = true)
data class UnsplashPhoto(
    @Json(name = "id") val id: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int,
    @Json(name = "likes") val likes: Int,
    @Json(name = "urls") val urls: UnsplashUrls,
    @Json(name = "user") val user: UnsplashUser,
    @Json(name = "links") val links: UnsplashLinks,
)

@JsonClass(generateAdapter = true)
data class UnsplashUrls(
    @Json(name = "raw") val raw: String,
    @Json(name = "full") val full: String,
    @Json(name = "regular") val regular: String,
    @Json(name = "small") val small: String,
    @Json(name = "thumb") val thumb: String,
)

@JsonClass(generateAdapter = true)
data class UnsplashUser(
    @Json(name = "name") val name: String,
    @Json(name = "links") val links: UnsplashUserLinks,
)

@JsonClass(generateAdapter = true)
data class UnsplashUserLinks(
    @Json(name = "html") val html: String,
)

@JsonClass(generateAdapter = true)
data class UnsplashLinks(
    @Json(name = "html") val html: String,
    @Json(name = "download_location") val downloadLocation: String,
)

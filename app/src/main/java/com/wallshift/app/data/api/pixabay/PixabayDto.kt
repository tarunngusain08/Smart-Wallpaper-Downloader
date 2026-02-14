package com.wallshift.app.data.api.pixabay

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PixabaySearchResponse(
    @Json(name = "total") val total: Int,
    @Json(name = "totalHits") val totalHits: Int,
    @Json(name = "hits") val hits: List<PixabayHit>,
)

@JsonClass(generateAdapter = true)
data class PixabayHit(
    @Json(name = "id") val id: Int,
    @Json(name = "webformatURL") val webformatUrl: String,
    @Json(name = "largeImageURL") val largeImageUrl: String,
    @Json(name = "imageWidth") val imageWidth: Int,
    @Json(name = "imageHeight") val imageHeight: Int,
    @Json(name = "likes") val likes: Int,
    @Json(name = "user") val user: String,
    @Json(name = "pageURL") val pageUrl: String,
)

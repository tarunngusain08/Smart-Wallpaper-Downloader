package com.wallshift.app.data.api.wallhaven

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WallhavenSearchResponse(
    @Json(name = "data") val data: List<WallhavenData>,
)

@JsonClass(generateAdapter = true)
data class WallhavenData(
    @Json(name = "id") val id: String,
    @Json(name = "url") val url: String,
    @Json(name = "path") val path: String,
    @Json(name = "dimension_x") val dimensionX: Int,
    @Json(name = "dimension_y") val dimensionY: Int,
    @Json(name = "favorites") val favorites: Int,
    @Json(name = "thumbs") val thumbs: WallhavenThumbs,
)

@JsonClass(generateAdapter = true)
data class WallhavenThumbs(
    @Json(name = "large") val large: String,
    @Json(name = "original") val original: String,
    @Json(name = "small") val small: String,
)

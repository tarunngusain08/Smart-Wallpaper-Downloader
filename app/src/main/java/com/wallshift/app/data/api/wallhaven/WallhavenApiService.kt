package com.wallshift.app.data.api.wallhaven

import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit interface for the Wallhaven API. */
interface WallhavenApiService {

    @GET("api/v1/search")
    suspend fun searchWallpapers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("categories") categories: String = "100",  // General only
        @Query("purity") purity: String = "100",          // SFW only
        @Query("sorting") sorting: String = "relevance",
        @Query("atleast") atleast: String = "1080x1920",  // Minimum resolution
    ): WallhavenSearchResponse
}

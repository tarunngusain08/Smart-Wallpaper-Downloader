package com.wallshift.app.data.api.pexels

import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit interface for the Pexels API. */
interface PexelsApiService {

    @GET("v1/search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("orientation") orientation: String = "portrait",
    ): PexelsSearchResponse
}

package com.wallshift.app.data.api.unsplash

import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit interface for the Unsplash API. */
interface UnsplashApiService {

    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("orientation") orientation: String = "portrait",
        @Query("order_by") orderBy: String = "relevant",
    ): UnsplashSearchResponse
}

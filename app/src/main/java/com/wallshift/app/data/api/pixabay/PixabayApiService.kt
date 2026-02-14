package com.wallshift.app.data.api.pixabay

import retrofit2.http.GET
import retrofit2.http.Query

/** Retrofit interface for the Pixabay API. */
interface PixabayApiService {

    @GET("api/")
    suspend fun searchPhotos(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("orientation") orientation: String = "vertical",
        @Query("image_type") imageType: String = "photo",
        @Query("safesearch") safeSearch: Boolean = true,
        @Query("order") order: String = "popular",
    ): PixabaySearchResponse
}

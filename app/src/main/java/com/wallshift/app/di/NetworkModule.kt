package com.wallshift.app.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wallshift.app.BuildConfig
import com.wallshift.app.data.api.pexels.PexelsApiService
import com.wallshift.app.data.api.pixabay.PixabayApiService
import com.wallshift.app.data.api.unsplash.UnsplashApiService
import com.wallshift.app.data.api.wallhaven.WallhavenApiService
import com.wallshift.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    // --- Unsplash ---

    @Provides
    @Singleton
    @Named("unsplash_auth")
    fun provideUnsplashAuthInterceptor(): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("client_id", BuildConfig.UNSPLASH_API_KEY)
            .build()
        chain.proceed(original.newBuilder().url(url).build())
    }

    @Provides
    @Singleton
    @Named("unsplash")
    fun provideUnsplashOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("unsplash_auth") authInterceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideUnsplashApiService(
        @Named("unsplash") okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): UnsplashApiService =
        Retrofit.Builder()
            .baseUrl(Constants.UNSPLASH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UnsplashApiService::class.java)

    // --- Pexels ---

    @Provides
    @Singleton
    @Named("pexels_auth")
    fun providePexelsAuthInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", BuildConfig.PEXELS_API_KEY)
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    @Named("pexels")
    fun providePexelsOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("pexels_auth") authInterceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun providePexelsApiService(
        @Named("pexels") okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): PexelsApiService =
        Retrofit.Builder()
            .baseUrl(Constants.PEXELS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PexelsApiService::class.java)

    // --- Pixabay ---

    @Provides
    @Singleton
    @Named("pixabay_auth")
    fun providePixabayAuthInterceptor(): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("key", BuildConfig.PIXABAY_API_KEY)
            .build()
        chain.proceed(original.newBuilder().url(url).build())
    }

    @Provides
    @Singleton
    @Named("pixabay")
    fun providePixabayOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("pixabay_auth") authInterceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun providePixabayApiService(
        @Named("pixabay") okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): PixabayApiService =
        Retrofit.Builder()
            .baseUrl(Constants.PIXABAY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PixabayApiService::class.java)

    // --- Wallhaven ---

    @Provides
    @Singleton
    @Named("wallhaven_auth")
    fun provideWallhavenAuthInterceptor(): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val apiKey = BuildConfig.WALLHAVEN_API_KEY
        val newRequest = if (apiKey.isNotBlank()) {
            val url = original.url.newBuilder()
                .addQueryParameter("apikey", apiKey)
                .build()
            original.newBuilder().url(url).build()
        } else {
            original
        }
        chain.proceed(newRequest)
    }

    @Provides
    @Singleton
    @Named("wallhaven")
    fun provideWallhavenOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("wallhaven_auth") authInterceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideWallhavenApiService(
        @Named("wallhaven") okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): WallhavenApiService =
        Retrofit.Builder()
            .baseUrl(Constants.WALLHAVEN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WallhavenApiService::class.java)

    // --- OkHttpClient for image downloads (no auth) ---

    @Provides
    @Singleton
    @Named("imageDownload")
    fun provideImageDownloadOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
}

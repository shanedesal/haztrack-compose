package com.danger.haztrack.di

import com.danger.haztrack.BuildConfig
import com.danger.haztrack.data.remote.api.UploadApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.auth.Auth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Provides the shared Retrofit/OkHttp/Moshi stack used to talk to our own backend (currently
 * just the image-upload endpoints; future REST APIs should reuse this same [Retrofit] instance
 * rather than building their own client).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    /**
     * Attaches the signed-in user's Supabase access token to every request. Unlike the old
     * Firebase version, this needs no async fetch or Task-bridging: Supabase's Auth plugin
     * keeps the current session's access token cached in memory and refreshes it automatically
     * in the background, so reading it here is a plain synchronous call.
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(auth: Auth): Interceptor {
        return Interceptor { chain ->
            val accessToken = auth.currentAccessTokenOrNull()
            val request = chain.request().newBuilder()
                .apply { accessToken?.let { addHeader("Authorization", "Bearer $it") } }
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BACKEND_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideUploadApi(retrofit: Retrofit): UploadApi = retrofit.create(UploadApi::class.java)
}

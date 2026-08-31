package com.danger.haztrack.di

import com.danger.haztrack.BuildConfig
import com.danger.haztrack.data.remote.api.UploadApi
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
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
     * Attaches the signed-in user's Firebase ID token to every request. Runs on OkHttp's
     * background dispatcher, so blocking on [Tasks.await] here (there is no suspend-friendly
     * hook inside [Interceptor.intercept]) is safe and mirrors the standard pattern for bridging
     * GMS `Task`s into synchronous code.
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(firebaseAuth: FirebaseAuth): Interceptor {
        return Interceptor { chain ->
            val idToken = firebaseAuth.currentUser?.let { user ->
                runCatching { Tasks.await(user.getIdToken(false)).token }
                    .onFailure { Timber.w(it, "Fetching Firebase ID token for backend request failed") }
                    .getOrNull()
            }
            val request = chain.request().newBuilder()
                .apply { idToken?.let { addHeader("Authorization", "Bearer $it") } }
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
                // Never log request/response bodies (which may include image bytes and the
                // bearer token) outside of debug builds.
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

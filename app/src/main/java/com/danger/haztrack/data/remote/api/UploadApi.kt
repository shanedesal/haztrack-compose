package com.danger.haztrack.data.remote.api

import com.danger.haztrack.data.remote.dto.UploadResponseDto
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Retrofit service for the self-hosted image-upload backend (see
 * `docs/backend-image-upload-spec.md`). Every call requires a Firebase ID token, attached
 * automatically by [com.danger.haztrack.di.NetworkModule]'s auth interceptor — callers never
 * pass a token explicitly.
 *
 * [context] is one of [com.danger.haztrack.domain.model.UploadContext.pathSegment] (e.g.
 * `"profile-picture"`); the backend whitelists which contexts are valid.
 */
interface UploadApi {
    @Multipart
    @POST("uploads/{context}")
    suspend fun upload(
        @Path("context") context: String,
        @Part file: MultipartBody.Part,
    ): UploadResponseDto

    @DELETE("uploads/{context}")
    suspend fun delete(@Path("context") context: String)
}

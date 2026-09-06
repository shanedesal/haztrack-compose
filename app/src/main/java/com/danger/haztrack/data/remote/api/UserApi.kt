package com.danger.haztrack.data.remote.api

import com.danger.haztrack.data.remote.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApi {
    @GET("users/me")
    suspend fun getUserProfile(): UserProfileDto

    @PUT("users/me")
    suspend fun saveUserProfile(@Body profile: UserProfileDto): UserProfileDto
}

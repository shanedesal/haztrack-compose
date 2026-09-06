package com.danger.haztrack.data.remote.api

import com.danger.haztrack.data.remote.dto.UserProfileDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Talks to our backend's `/users/me` endpoints. The backend resolves the target user from the
 * caller's Supabase bearer token (attached by NetworkModule's auth interceptor), not from a path
 * parameter — [userId] is accepted here only to keep this class's public shape matching what
 * [com.danger.haztrack.data.repository.profile.UserProfileRepositoryImpl] already calls; it is
 * not sent over the wire.
 */
@Singleton
class UserRemoteDataSource @Inject constructor(
    private val userApi: UserApi,
) {
    suspend fun getUserProfile(): UserProfileDto {
        return userApi.getUserProfile()
    }

    suspend fun saveUserProfile(profile: UserProfileDto): UserProfileDto {
        return userApi.saveUserProfile(profile)
    }
}

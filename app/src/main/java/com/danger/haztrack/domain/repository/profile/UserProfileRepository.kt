package com.danger.haztrack.domain.repository.profile

import com.danger.haztrack.domain.model.UserProfile

interface UserProfileRepository {
    suspend fun getUserProfile(userId: String): UserProfile?

    suspend fun saveUserProfile(profile: UserProfile)
}

package com.danger.haztrack.domain.repository.profile

import com.danger.haztrack.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface UserProfileRepository {

    val userProfile: StateFlow<UserProfile?>
    suspend fun getUserProfile(userId: String): UserProfile?

    suspend fun saveUserProfile(profile: UserProfile)

    fun clearCachedProfile()
}

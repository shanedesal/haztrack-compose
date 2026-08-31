package com.danger.haztrack.domain.usecase.profile

import com.danger.haztrack.domain.model.UserProfile
import com.danger.haztrack.domain.repository.profile.UserProfileRepository
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(userId: String): UserProfile? {
        return userProfileRepository.getUserProfile(userId)
    }
}

package com.danger.haztrack.domain.usecase.profile

import com.danger.haztrack.domain.model.UserProfile
import com.danger.haztrack.domain.repository.profile.UserProfileRepository
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(profile: UserProfile) {
        userProfileRepository.saveUserProfile(
            profile.copy(
                firstName = profile.firstName.trim(),
                lastName = profile.lastName.trim(),
            ),
        )
    }
}

package com.danger.haztrack.domain.usecase.profile

import com.danger.haztrack.domain.model.UserProfile
import com.danger.haztrack.domain.repository.profile.UserProfileRepository
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(
        userId: String,
        firstName: String,
        lastName: String,
        email: String?,
        photoUrl: String?,
    ) {
        userProfileRepository.saveUserProfile(
            UserProfile(
                id = userId,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email,
                photoUrl = photoUrl,
            ),
        )
    }
}

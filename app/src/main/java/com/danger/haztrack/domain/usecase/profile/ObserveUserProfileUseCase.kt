package com.danger.haztrack.domain.usecase.profile

import com.danger.haztrack.domain.model.UserProfile
import com.danger.haztrack.domain.repository.profile.UserProfileRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    operator fun invoke(): StateFlow<UserProfile?> = userProfileRepository.userProfile
}

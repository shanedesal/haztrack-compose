package com.danger.haztrack.domain.usecase.profile

import com.danger.haztrack.domain.repository.profile.UserProfileRepository
import javax.inject.Inject

class ClearCachedUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    operator fun invoke() = userProfileRepository.clearCachedProfile()
}

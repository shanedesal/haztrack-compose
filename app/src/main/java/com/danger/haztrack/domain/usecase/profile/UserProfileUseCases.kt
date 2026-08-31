package com.danger.haztrack.domain.usecase.profile

import javax.inject.Inject

data class UserProfileUseCases @Inject constructor(
    val getUserProfile: GetUserProfileUseCase,
    val saveUserProfile: SaveUserProfileUseCase,
    val ensureUserProfile: EnsureUserProfileUseCase,
)

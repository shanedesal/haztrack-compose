package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.model.AuthUser
import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): AuthUser? {
        return authRepository.getCurrentUser()
    }
}

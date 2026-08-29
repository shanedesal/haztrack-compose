package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

class SendPasswordResetEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String) {
        authRepository.sendPasswordResetEmail(
            email = AuthInputValidation.email(email),
        )
    }
}

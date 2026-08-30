package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

class VerifyPasswordResetCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(oobCode: String): String {
        return authRepository.verifyPasswordResetCode(oobCode)
    }
}

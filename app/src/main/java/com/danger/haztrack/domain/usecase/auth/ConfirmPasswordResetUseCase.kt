package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

class ConfirmPasswordResetUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(oobCode: String, newPassword: String){
        authRepository.confirmPasswordReset(oobCode, newPassword)
    }
}

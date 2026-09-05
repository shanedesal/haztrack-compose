package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

private const val PASSWORD_LENGTH: Int = 6
class UpdatePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(newPassword: String) {
        require(newPassword.length >= PASSWORD_LENGTH) { "Password must be at least 6 characters" }
        authRepository.updatePassword(newPassword)
    }
}

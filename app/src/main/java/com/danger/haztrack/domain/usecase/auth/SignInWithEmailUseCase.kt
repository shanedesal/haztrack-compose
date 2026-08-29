package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.model.AuthUser
import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): AuthUser {
        val validEmail = AuthInputValidation.email(email)
        val validPassword = AuthInputValidation.password(password)
        return authRepository.signInWithEmail(validEmail, validPassword)
    }
}

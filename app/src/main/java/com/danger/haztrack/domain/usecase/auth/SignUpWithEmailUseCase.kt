package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.model.AuthUser
import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

class SignUpWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(firstName: String, lastName: String, email: String, password: String): AuthUser {
        val validFirstName = AuthInputValidation.name(firstName)
        val validLastName = AuthInputValidation.name(lastName)
        val validEmail = AuthInputValidation.email(email)
        val validPassword = AuthInputValidation.password(password)
        return authRepository.signUpWithEmail(validFirstName, validLastName, validEmail, validPassword)
    }
}

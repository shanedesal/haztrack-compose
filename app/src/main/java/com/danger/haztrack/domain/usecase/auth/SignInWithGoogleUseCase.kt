package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.model.AuthUser
import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(idToken: String, rawNonce: String): AuthUser {
        return authRepository.signInWithGoogle(
            idToken = AuthInputValidation.googleIdToken(idToken),
            rawNonce = rawNonce
        )
    }
}

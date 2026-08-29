package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke() {
        authRepository.signOut()
    }
}

package com.danger.haztrack.domain.usecase.auth

import com.danger.haztrack.domain.model.AuthUser
import com.danger.haztrack.domain.repository.auth.AuthRepository
import javax.inject.Inject

class EstablishSessionFromUrlUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(url: String): AuthUser = authRepository.establishSessionFromUrl(url)
}

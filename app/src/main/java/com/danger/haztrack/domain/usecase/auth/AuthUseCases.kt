package com.danger.haztrack.domain.usecase.auth

import javax.inject.Inject

data class AuthUseCases @Inject constructor(
    val getCurrentUser: GetCurrentUserUseCase,
    val signInWithEmail: SignInWithEmailUseCase,
    val signUpWithEmail: SignUpWithEmailUseCase,
    val signInWithGoogle: SignInWithGoogleUseCase,
    val sendPasswordResetEmail: SendPasswordResetEmailUseCase,
    val signOut: SignOutUseCase,
)

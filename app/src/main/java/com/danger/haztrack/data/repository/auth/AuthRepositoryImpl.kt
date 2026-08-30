package com.danger.haztrack.data.repository.auth

import com.danger.haztrack.data.remote.api.AuthRemoteDataSource
import com.danger.haztrack.domain.model.AuthUser
import com.danger.haztrack.domain.repository.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthRemoteDataSource,
) : AuthRepository {
    override fun getCurrentUser(): AuthUser? {
        return authService.currentUser?.toAuthUser()
    }

    override suspend fun signInWithEmail(email: String, password: String): AuthUser {
        return authService.signInWithEmail(email, password).toAuthUser()
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthUser {
        return authService.signUpWithEmail(email, password).toAuthUser()
    }

    override suspend fun signInWithGoogle(idToken: String): AuthUser {
        return authService.signInWithGoogle(idToken).toAuthUser()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        authService.sendPasswordResetEmail(email)
    }

    override suspend fun verifyPasswordResetCode(oobCode: String): String {
        return authService.verifyPasswordResetCode(oobCode)
    }

    override suspend fun confirmPasswordReset(oobCode: String, newPassword: String) {
        authService.confirmPasswordReset(oobCode, newPassword)
    }

    override fun signOut() {
        authService.signOut()
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            id = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isEmailVerified = isEmailVerified,
        )
    }
}

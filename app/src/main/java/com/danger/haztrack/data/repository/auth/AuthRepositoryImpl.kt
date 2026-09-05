package com.danger.haztrack.data.repository.auth

import com.danger.haztrack.data.remote.api.AuthRemoteDataSource
import com.danger.haztrack.domain.model.AuthUser
import com.danger.haztrack.domain.repository.auth.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.JsonPrimitive
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthRemoteDataSource,
) : AuthRepository {
    override fun getCurrentUser(): AuthUser? = authService.currentUser?.toAuthUser()

    override suspend fun signInWithEmail(email: String, password: String): AuthUser {
        return authService.signInWithEmail(email, password).toAuthUser()
    }

    override suspend fun signUpWithEmail(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
    ): AuthUser {
        Timber.d("SignUpWithEmail started")
        val displayName = "$firstName $lastName".trim()
        return authService.signUpWithEmail(displayName, email, password).toAuthUser()
    }

    override suspend fun signInWithGoogle(idToken: String, rawNonce: String): AuthUser {
        return authService.signInWithGoogle(idToken, rawNonce).toAuthUser()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        authService.sendPasswordResetEmail(email)
    }

    override suspend fun establishSessionFromUrl(url: String): AuthUser {
        return authService.establishSessionFromUrl(url).toAuthUser()
    }

    override suspend fun updatePassword(newPassword: String) {
        authService.updatePassword(newPassword)
    }

    override suspend fun signOut() {
        authService.signOut()
    }

    override suspend fun awaitSessionReady() = authService.awaitInitialization()

    private fun UserInfo.toAuthUser(): AuthUser {
        val displayName = (userMetadata?.get("display_name") as? JsonPrimitive)?.content
        val photoUrl = (userMetadata?.get("photo_url") as? JsonPrimitive)?.content
        val isGoogleAccount = identities?.any { it.provider == "google"} == true

        return AuthUser(
            id = id,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            isEmailVerified = emailConfirmedAt != null,
            isGoogleAccount = isGoogleAccount,
        )
    }
}

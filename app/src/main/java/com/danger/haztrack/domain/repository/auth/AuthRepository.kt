package com.danger.haztrack.domain.repository.auth

import com.danger.haztrack.domain.model.AuthUser

interface   AuthRepository {
    fun getCurrentUser(): AuthUser?

    suspend fun signInWithEmail(email: String, password: String): AuthUser

    suspend fun signUpWithEmail(firstName: String, lastName: String, email: String, password: String): AuthUser

    suspend fun signInWithGoogle(idToken: String, rawNonce: String): AuthUser

    suspend fun sendPasswordResetEmail(email: String)

    suspend fun establishSessionFromUrl(url: String): AuthUser

    suspend fun updatePassword(newPassword: String)

    suspend fun signOut()

    suspend fun awaitSessionReady()
}

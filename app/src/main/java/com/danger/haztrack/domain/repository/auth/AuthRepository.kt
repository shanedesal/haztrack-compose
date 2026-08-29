package com.danger.haztrack.domain.repository.auth

import com.danger.haztrack.domain.model.AuthUser

interface AuthRepository {
    fun getCurrentUser(): AuthUser?

    suspend fun signInWithEmail(email: String, password: String): AuthUser

    suspend fun signUpWithEmail(email: String, password: String): AuthUser

    suspend fun signInWithGoogle(idToken: String): AuthUser

    suspend fun sendPasswordResetEmail(email: String)

    fun signOut()
}

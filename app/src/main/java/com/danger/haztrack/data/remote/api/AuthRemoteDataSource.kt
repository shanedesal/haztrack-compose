package com.danger.haztrack.data.remote.api

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.parseSessionFromUrl
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val auth: Auth,
) {
    val currentUser: UserInfo?
        get() = auth.currentUserOrNull()

    suspend fun signInWithEmail(email: String, password: String): UserInfo {
        auth.signInWith(Email){
            this.email = email
            this.password = password
        }
        return auth.currentUserOrNull()
            ?: error("Supabase did not return a user after email sign-in")
    }

    suspend fun signUpWithEmail(displayName: String, email: String, password: String): UserInfo {
        auth.signUpWith(Email){
            this.email = email
            this.password = password
            data = buildJsonObject { put("display_name", displayName) }
        }
        return auth.currentUserOrNull()
            ?: error("Supabase did not return a user after account creation")
    }

    suspend fun signInWithGoogle(idToken: String, rawNonce: String): UserInfo {
        auth.signInWith(IDToken){
            this.idToken = idToken
            this.provider = Google
            this.nonce = rawNonce
        }
        return auth.currentUserOrNull()
            ?: error("Supabase did not return a user after Google sign-in")
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "com.danger.haztrack://reset-password"
        )
    }

    suspend fun establishSessionFromUrl(url: String): UserInfo {
        auth.parseSessionFromUrl(url)
        return auth.currentUserOrNull()
            ?: error("Supabase did not return a user after processing the recovery link")
    }

    suspend fun updatePassword(newPassword: String) {
        auth.updateUser { password = newPassword }
    }

    suspend fun awaitInitialization() = auth.awaitInitialization()

    suspend fun signOut() {
        auth.signOut()
    }
}

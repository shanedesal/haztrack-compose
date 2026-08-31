package com.danger.haztrack.data.remote.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.actionCodeSettings
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        return firebaseAuth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Firebase did not return a user after email sign-in")
    }

    suspend fun signUpWithEmail(displayName: String, email: String, password: String): FirebaseUser {
        val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Firebase did not return a user after account creation")

        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        runCatching { user.updateProfile(profileUpdate).await() }
            .onFailure { Timber.w(it, "SignUpWithEmail: failed to set display name on new account") }

        return user
    }

    suspend fun signInWithGoogle(idToken: String): FirebaseUser {
        val firebaseCredential = GoogleAuthProvider.getCredential(
            idToken,
            null,
        )

        return firebaseAuth.signInWithCredential(firebaseCredential).await().user
            ?: error("Firebase did not return a user after Google sign-in")
    }

    suspend fun sendPasswordResetEmail(email: String) {
        val  actionCodeSettings = actionCodeSettings {
            url = "https://haztrack-62a3c.firebaseapp.com/resetPassword"
            handleCodeInApp = true
            setAndroidPackageName(
                "com.danger.haztrack",
                true,
                null
            )
        }
        firebaseAuth.sendPasswordResetEmail(email, actionCodeSettings).await()
    }

    suspend fun verifyPasswordResetCode(code: String): String {
        return firebaseAuth.verifyPasswordResetCode(code).await()
    }

    suspend fun confirmPasswordReset(code: String, newPassword: String) {
        firebaseAuth.confirmPasswordReset(code, newPassword).await()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}

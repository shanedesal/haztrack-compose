package com.danger.haztrack.data.remote.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
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

    suspend fun signUpWithEmail(email: String, password: String): FirebaseUser {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Firebase did not return a user after account creation")
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
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}

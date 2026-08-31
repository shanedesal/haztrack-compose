package com.danger.haztrack.data.remote.api

import com.danger.haztrack.data.remote.dto.UserProfileDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    suspend fun getUserProfile(userId: String): UserProfileDto? {
        val snapshot = firestore.usersCollection().document(userId).get().await()
        return if (snapshot.exists()) snapshot.toObject(UserProfileDto::class.java) else null
    }

    suspend fun saveUserProfile(userId: String, profile: UserProfileDto) {
        Timber.d("SaveUserProfile: writing profile document")
        firestore.usersCollection().document(userId).set(profile).await()
    }

    private fun FirebaseFirestore.usersCollection() = collection(USERS_COLLECTION)

    companion object {
        private const val USERS_COLLECTION = "users"
    }
}

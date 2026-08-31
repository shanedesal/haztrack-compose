package com.danger.haztrack.data.remote.dto

/**
 * Firestore representation of a `users/{uid}` document. All fields have defaults so the
 * Firestore SDK can construct instances via reflection when deserializing (`toObject`).
 */
data class UserProfileDto(
    val firstName: String = "",
    val lastName: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
)

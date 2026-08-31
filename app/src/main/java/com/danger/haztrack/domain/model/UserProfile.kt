package com.danger.haztrack.domain.model

/**
 * The user's structured profile data, persisted in Firestore (`users/{id}`) and keyed by the
 * Firebase Auth uid. Distinct from [AuthUser], which only carries session/identity data Firebase
 * Auth itself knows about.
 */
data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val photoUrl: String?,
)

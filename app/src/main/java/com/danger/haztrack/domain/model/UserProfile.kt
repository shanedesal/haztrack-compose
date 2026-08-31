package com.danger.haztrack.domain.model

/**
 * The user's structured profile data, persisted in Firestore (`users/{id}`) and keyed by the
 * Firebase Auth uid. Distinct from [AuthUser], which only carries session/identity data Firebase
 * Auth itself knows about.
 *
 * Every profile read in the app (Home, Settings, Profile) sources its data from here rather than
 * from [AuthUser], so edits made on the Profile screen are reflected everywhere.
 */
data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val photoUrl: String?,
    val photoSource: PhotoSource = PhotoSource.NONE,
    val dateOfBirth: String? = null,
    val gender: Gender? = null,
    val phoneRegionCode: String? = null,
    val phoneDialCode: String? = null,
    val phoneNumber: String? = null,
)

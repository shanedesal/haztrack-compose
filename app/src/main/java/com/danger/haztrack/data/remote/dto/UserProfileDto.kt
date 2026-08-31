package com.danger.haztrack.data.remote.dto

/**
 * Firestore representation of a `users/{uid}` document. All fields have defaults so the
 * Firestore SDK can construct instances via reflection when deserializing (`toObject`).
 *
 * `photoSource`/`gender` are stored as the enum's `name` (or `null`/blank) rather than an
 * ordinal, so the stored value stays readable and stable if enum entries are reordered later.
 * `dateOfBirth` is an ISO-8601 `yyyy-MM-dd` string.
 */
data class UserProfileDto(
    val firstName: String = "",
    val lastName: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val photoSource: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val phoneRegionCode: String? = null,
    val phoneDialCode: String? = null,
    val phoneNumber: String? = null,
)

package com.danger.haztrack.data.remote.dto

import com.squareup.moshi.JsonClass

/**
 * Backend representation of a `profiles` row. Field names here are camelCase to match the
 * backend's JSON contract exactly (see the profile-endpoints spec) — no @Json(name=...)
 * remapping needed since the backend already returns/accepts camelCase keys.
 */
@JsonClass(generateAdapter = true)
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

package com.danger.haztrack.domain.model

data class AuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val isGoogleAccount: Boolean = false,
)

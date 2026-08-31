package com.danger.haztrack.presentation.profile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val isGoogleAccount: Boolean = false,
)

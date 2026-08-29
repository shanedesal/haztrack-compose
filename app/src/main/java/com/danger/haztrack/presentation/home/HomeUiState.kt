package com.danger.haztrack.presentation.home

import com.danger.haztrack.domain.model.AuthUser

data class HomeUiState(
    val user: AuthUser? = null,
)

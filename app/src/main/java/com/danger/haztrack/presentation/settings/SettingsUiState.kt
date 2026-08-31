package com.danger.haztrack.presentation.settings

import com.danger.haztrack.domain.model.AuthUser

data class SettingsUiState(
    val user: AuthUser? = null,
)

package com.danger.haztrack.presentation.settings

sealed interface SettingsEvent {
    data object NavigateToLogin : SettingsEvent
}

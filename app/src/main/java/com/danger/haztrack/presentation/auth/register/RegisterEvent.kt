package com.danger.haztrack.presentation.auth.register

sealed interface RegisterEvent {
    data object NavigateToHome : RegisterEvent
}

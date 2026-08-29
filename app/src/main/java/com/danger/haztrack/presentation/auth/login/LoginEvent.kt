package com.danger.haztrack.presentation.auth.login

sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent
}

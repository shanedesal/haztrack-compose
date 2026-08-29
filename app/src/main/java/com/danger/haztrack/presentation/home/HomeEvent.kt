package com.danger.haztrack.presentation.home

sealed interface HomeEvent {
    data object NavigateToLogin : HomeEvent
}

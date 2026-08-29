package com.danger.haztrack.presentation.auth.login

import androidx.annotation.StringRes

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isGoogleSignInLoading: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val isSignInEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading && !isGoogleSignInLoading
}

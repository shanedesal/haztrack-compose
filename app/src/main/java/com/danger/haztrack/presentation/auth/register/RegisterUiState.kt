package com.danger.haztrack.presentation.auth.register

import androidx.annotation.StringRes

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val isSignUpEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && !isLoading
}

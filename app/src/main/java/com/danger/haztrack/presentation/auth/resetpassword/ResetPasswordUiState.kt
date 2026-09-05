package com.danger.haztrack.presentation.auth.resetpassword

import androidx.annotation.StringRes

data class ResetPasswordUiState(
    val email: String? = null,
    val newPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isResetSuccessful: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
){
    val isSubmitEnabled: Boolean
        get() = newPassword.length >= 6 && !isLoading
}

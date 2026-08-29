package com.danger.haztrack.presentation.auth.forgotpassword

import androidx.annotation.StringRes

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val isEmailSent: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
) {
    val isSubmitEnabled: Boolean
        get() = email.isNotBlank() && !isLoading
}

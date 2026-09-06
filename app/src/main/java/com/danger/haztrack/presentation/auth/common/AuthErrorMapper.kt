package com.danger.haztrack.presentation.auth.common

import androidx.annotation.StringRes
import com.danger.haztrack.R
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthSessionMissingException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import java.io.IOException

@StringRes
fun Throwable.toAuthErrorMessageRes(): Int = when (this) {
    is IllegalArgumentException -> R.string.auth_error_invalid_input
    is AuthWeakPasswordException -> R.string.auth_error_weak_password
    is AuthSessionMissingException -> R.string.auth_error_no_account
    is AuthRestException -> when (errorCode?.name) {
        "INVALID_CREDENTIALS" -> R.string.auth_error_invalid_credentials
        "USER_ALREADY_EXISTS" -> R.string.auth_error_account_exists
        "USER_NOT_FOUND" -> R.string.auth_error_no_account
        else -> R.string.auth_error_generic
    }
    is IOException -> R.string.auth_error_network
    else -> R.string.auth_error_generic
}

package com.danger.haztrack.presentation.auth.common

import androidx.annotation.StringRes
import com.danger.haztrack.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * Maps low-level exceptions thrown by the auth use cases to a user-facing string resource,
 * keeping ViewModels free of Android [android.content.Context] dependencies while still
 * honoring the project convention of centralizing user-facing text in `strings.xml`.
 */
@StringRes
fun Throwable.toAuthErrorMessageRes(): Int = when (this) {
    is IllegalArgumentException -> R.string.auth_error_invalid_input
    is FirebaseAuthWeakPasswordException -> R.string.auth_error_weak_password
    is FirebaseAuthInvalidCredentialsException -> R.string.auth_error_invalid_credentials
    is FirebaseAuthUserCollisionException -> R.string.auth_error_account_exists
    is FirebaseAuthInvalidUserException -> R.string.auth_error_no_account
    is FirebaseNetworkException -> R.string.auth_error_network
    else -> R.string.auth_error_generic
}

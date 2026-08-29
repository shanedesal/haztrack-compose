package com.danger.haztrack.presentation.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import com.danger.haztrack.presentation.auth.common.toAuthErrorMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessageRes = null) }
    }

    fun onSendResetLinkClick() {
        val state = _uiState.value
        if (!state.isSubmitEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }
            runCatching {
                authUseCases.sendPasswordResetEmail(state.email.trim())
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isEmailSent = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = throwable.toAuthErrorMessageRes())
                }
            }
        }
    }
}

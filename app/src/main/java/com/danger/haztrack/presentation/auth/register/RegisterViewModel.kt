package com.danger.haztrack.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danger.haztrack.R
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import com.danger.haztrack.presentation.auth.common.toAuthErrorMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events: Flow<RegisterEvent> = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessageRes = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessageRes = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessageRes = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onConfirmPasswordVisibilityToggle() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun onSignUpClick() {
        val state = _uiState.value
        if (!state.isSignUpEnabled) return

        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessageRes = R.string.register_error_password_mismatch) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }
            runCatching {
                authUseCases.signUpWithEmail(state.email.trim(), state.password)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(RegisterEvent.NavigateToHome)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = throwable.toAuthErrorMessageRes())
                }
            }
        }
    }
}

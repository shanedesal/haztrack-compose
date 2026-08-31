package com.danger.haztrack.presentation.auth.login

import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import com.danger.haztrack.domain.usecase.profile.UserProfileUseCases
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
class LoginViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userProfileUseCases: UserProfileUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessageRes = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessageRes = null) }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSignInClick() {
        val state = _uiState.value
        if (!state.isSignInEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null) }
            runCatching {
                authUseCases.signInWithEmail(state.email.trim(), state.password)
            }.onSuccess { authUser ->
                runCatching { userProfileUseCases.ensureUserProfile(authUser) }
                _uiState.update { it.copy(isLoading = false) }
                _events.send(LoginEvent.NavigateToHome)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = throwable.toAuthErrorMessageRes())
                }
            }
        }
    }

    fun onGoogleSignInStarted() {
        _uiState.update { it.copy(isGoogleSignInLoading = true, errorMessageRes = null) }
    }

    fun onGoogleIdTokenReceived(idToken: String) {
        viewModelScope.launch {
            runCatching {
                authUseCases.signInWithGoogle(idToken)
            }.onSuccess { authUser ->
                runCatching { userProfileUseCases.ensureUserProfile(authUser) }
                _uiState.update { it.copy(isGoogleSignInLoading = false) }
                _events.send(LoginEvent.NavigateToHome)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isGoogleSignInLoading = false, errorMessageRes = throwable.toAuthErrorMessageRes())
                }
            }
        }
    }

    fun onGoogleSignInFailed(throwable: Throwable) {
        val errorMessageRes = if (throwable is GetCredentialCancellationException) {
            null
        } else {
            throwable.toAuthErrorMessageRes()
        }
        _uiState.update { it.copy(isGoogleSignInLoading = false, errorMessageRes = errorMessageRes) }
    }
}

package com.danger.haztrack.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danger.haztrack.R
import com.danger.haztrack.domain.model.PhotoSource
import com.danger.haztrack.domain.model.UserProfile
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
class RegisterViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userProfileUseCases: UserProfileUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events: Flow<RegisterEvent> = _events.receiveAsFlow()

    fun onFirstNameChange(firstName: String) {
        _uiState.update { it.copy(firstName = firstName, errorMessageRes = null) }
    }

    fun onLastNameChange(lastName: String) {
        _uiState.update { it.copy(lastName = lastName, errorMessageRes = null) }
    }

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
                authUseCases.signUpWithEmail(
                    state.firstName.trim(),
                    state.lastName.trim(),
                    state.email.trim(),
                    state.password,
                )
            }.onSuccess { authUser ->
                // The Firestore profile write is best-effort here: if it fails, ProfileScreen
                // self-heals via EnsureUserProfileUseCase the next time the user opens it.
                runCatching {
                    userProfileUseCases.saveUserProfile(
                        UserProfile(
                            id = authUser.id,
                            firstName = state.firstName,
                            lastName = state.lastName,
                            email = authUser.email,
                            photoUrl = authUser.photoUrl,
                            photoSource = PhotoSource.NONE,
                        ),
                    )
                }
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

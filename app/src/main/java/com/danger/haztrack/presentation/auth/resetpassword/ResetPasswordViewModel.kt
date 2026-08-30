package com.danger.haztrack.presentation.auth.resetpassword

import androidx.lifecycle.SavedStateHandle
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
class ResetPasswordViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val oobCode: String = checkNotNull(savedStateHandle["oobCode"])
    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    init{
        verifyCode()
    }

    private fun verifyCode() {
        viewModelScope.launch {
            runCatching {
                authUseCases.verifyPasswordResetCode(oobCode)
            }.onSuccess { email ->
                _uiState.update {
                    it.copy(email = email, isCodeValid = true, isVerifying = false)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isVerifying = false, errorMessageRes = throwable.toAuthErrorMessageRes())
                }
            }
        }
    }

    fun onNewPasswordChange(password: String) {
        _uiState.update {
            it.copy(newPassword = password, errorMessageRes = null)
        }
    }

    fun onPasswordVisibilityToggle(){
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun onConfirmResetClick(){
        val state = _uiState.value
        if(!state.isSubmitEnabled) return

        viewModelScope.launch {
            _uiState.update{
                it.copy(isLoading = true, errorMessageRes = null)
            }
            runCatching {
                authUseCases.confirmPasswordReset(oobCode, state.newPassword)
            }.onSuccess {
                _uiState.update {
                    it.copy(isLoading = false, isResetSuccessful = true)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessageRes = throwable.toAuthErrorMessageRes())
                }
            }
        }
    }

}

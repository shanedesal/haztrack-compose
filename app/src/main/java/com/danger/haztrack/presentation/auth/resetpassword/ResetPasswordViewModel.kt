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
    private val _uiState = MutableStateFlow(
        ResetPasswordUiState(email = savedStateHandle["email"])
    )
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

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
                authUseCases.updatePassword(state.newPassword)
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

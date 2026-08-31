package com.danger.haztrack.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import com.danger.haztrack.domain.usecase.profile.UserProfileUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userProfileUseCases: UserProfileUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val authUser = authUseCases.getCurrentUser()
        if (authUser == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        _uiState.update {
            it.copy(
                email = authUser.email,
                photoUrl = authUser.photoUrl,
                isGoogleAccount = authUser.isGoogleAccount,
            )
        }

        viewModelScope.launch {
            // ensureUserProfile self-heals: it creates the Firestore document on the fly for
            // Google/legacy accounts or a failed registration write, instead of showing blanks.
            runCatching { userProfileUseCases.ensureUserProfile(authUser) }
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            firstName = profile.firstName,
                            lastName = profile.lastName,
                            email = profile.email ?: authUser.email,
                            photoUrl = profile.photoUrl ?: authUser.photoUrl,
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }
}

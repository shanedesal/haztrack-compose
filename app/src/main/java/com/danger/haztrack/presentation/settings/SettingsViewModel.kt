package com.danger.haztrack.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import com.danger.haztrack.domain.usecase.profile.UserProfileUseCases
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
class SettingsViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userProfileUseCases: UserProfileUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val user = authUseCases.getCurrentUser() ?: return
        _uiState.update { it.copy(email = user.email, photoUrl = user.photoUrl) }

        viewModelScope.launch {
            // Every profile read comes from Firestore (not Firebase Auth's displayName/photo),
            // so an edit made on the Profile screen is reflected here immediately.
            runCatching { userProfileUseCases.ensureUserProfile(user) }
                .onSuccess { profile ->
                    val fullName = listOf(profile.firstName, profile.lastName)
                        .filter(String::isNotBlank)
                        .joinToString(separator = " ")
                    _uiState.update {
                        it.copy(
                            displayName = fullName.takeIf(String::isNotBlank),
                            email = profile.email ?: user.email,
                            photoUrl = profile.photoUrl ?: user.photoUrl,
                        )
                    }
                }
        }
    }

    fun onSignOutClick() {
        viewModelScope.launch {
            authUseCases.signOut()
            _events.send(SettingsEvent.NavigateToLogin)
        }
    }
}

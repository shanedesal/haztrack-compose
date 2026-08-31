package com.danger.haztrack.presentation.home

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
class HomeViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val userProfileUseCases: UserProfileUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val user = authUseCases.getCurrentUser() ?: return
        _uiState.update { it.copy(email = user.email) }

        viewModelScope.launch {
            // Every profile read comes from Firestore (not Firebase Auth's displayName), so an
            // edit made on the Profile screen is reflected here without extra cache invalidation.
            runCatching { userProfileUseCases.ensureUserProfile(user) }
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            displayName = profile.firstName.takeIf(String::isNotBlank),
                            email = profile.email ?: user.email,
                        )
                    }
                }
        }
    }
}

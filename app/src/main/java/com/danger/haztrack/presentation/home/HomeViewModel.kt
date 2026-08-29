package com.danger.haztrack.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(user = authUseCases.getCurrentUser()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    fun onSignOutClick() {
        authUseCases.signOut()
        viewModelScope.launch {
            _events.send(HomeEvent.NavigateToLogin)
        }
    }
}

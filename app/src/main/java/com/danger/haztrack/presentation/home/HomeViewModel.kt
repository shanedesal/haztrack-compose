package com.danger.haztrack.presentation.home

import androidx.lifecycle.ViewModel
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    authUseCases: AuthUseCases,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(user = authUseCases.getCurrentUser()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}

package com.danger.haztrack.presentation.navigation

import androidx.lifecycle.ViewModel
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Decides where the navigation graph should start based on whether a user session
 * already exists, so a signed-in user is never sent back through the login screen.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    authUseCases: AuthUseCases,
) : ViewModel() {

    val startDestination: String = if (authUseCases.getCurrentUser() != null) {
        HaztrackDestination.Home.route
    } else {
        HaztrackDestination.Login.route
    }
}

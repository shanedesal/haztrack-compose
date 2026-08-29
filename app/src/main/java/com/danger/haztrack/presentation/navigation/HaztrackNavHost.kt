package com.danger.haztrack.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.danger.haztrack.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.danger.haztrack.presentation.auth.login.LoginScreen
import com.danger.haztrack.presentation.auth.register.RegisterScreen
import com.danger.haztrack.presentation.home.HomeScreen

@Composable
fun HaztrackNavHost(navController: NavHostController = rememberNavController()) {
    val sessionViewModel: SessionViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = sessionViewModel.startDestination,
    ) {
        composable(HaztrackDestination.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(HaztrackDestination.Register.route) },
                onNavigateToForgotPassword = {
                    navController.navigate(HaztrackDestination.ForgotPassword.route)
                },
                onSignedIn = { navController.navigateToHomeAndClearAuth() },
            )
        }

        composable(HaztrackDestination.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignedUp = { navController.navigateToHomeAndClearAuth() },
            )
        }

        composable(HaztrackDestination.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(HaztrackDestination.Home.route) {
            HomeScreen(
                onSignedOut = {
                    navController.navigate(HaztrackDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}

private fun NavHostController.navigateToHomeAndClearAuth() {
    navigate(HaztrackDestination.Home.route) {
        popUpTo(HaztrackDestination.Login.route) { inclusive = true }
    }
}

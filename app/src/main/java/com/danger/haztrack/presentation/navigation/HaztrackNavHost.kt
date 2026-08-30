package com.danger.haztrack.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danger.haztrack.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.danger.haztrack.presentation.auth.login.LoginScreen
import com.danger.haztrack.presentation.auth.register.RegisterScreen
import com.danger.haztrack.presentation.auth.resetpassword.ResetPasswordScreen
import com.danger.haztrack.presentation.home.HomeScreen
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HaztrackNavHost(
    navController: NavHostController = rememberNavController(),
    oobCodeFlow: StateFlow<String?>
) {
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val oobCode by oobCodeFlow.collectAsStateWithLifecycle()

    LaunchedEffect(oobCode) {
        oobCode?.let { code ->
            navController.navigate(HaztrackDestination.ResetPassword.createRoute(code))
        }
    }

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

        composable(
            route = HaztrackDestination.ResetPassword.route,
            arguments = listOf(navArgument("oobCode") { type = NavType.StringType})
        ) {
            ResetPasswordScreen(
                onResetComplete = {
                    navController.navigate(HaztrackDestination.Login.route) {
                        popUpTo(0) { inclusive = true}
                    }
                }
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

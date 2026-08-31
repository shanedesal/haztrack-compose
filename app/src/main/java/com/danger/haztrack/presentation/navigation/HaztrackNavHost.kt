package com.danger.haztrack.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.danger.haztrack.presentation.myreports.MyReportsScreen
import com.danger.haztrack.presentation.notifications.NotificationsScreen
import com.danger.haztrack.presentation.report.ReportScreen
import com.danger.haztrack.presentation.settings.SettingsScreen
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
                onNavigateBack = { navController.navigateToLoginAndClearStack() },
                onResetComplete = { navController.navigateToLoginAndClearStack() },
            )
        }

        composable(HaztrackDestination.Home.route) {
            MainScaffold(navController = navController) { paddingValues ->
                HomeScreen(
                    modifier = Modifier.padding(paddingValues),
                    onNavigateToReport = { navController.navigate(HaztrackDestination.Report.route) },
                    onNavigateToMyReports = { navController.navigate(HaztrackDestination.MyReports.route) },
                )
            }
        }

        composable(HaztrackDestination.Report.route) {
            MainScaffold(navController = navController) { paddingValues ->
                ReportScreen(modifier = Modifier.padding(paddingValues))
            }
        }

        composable(HaztrackDestination.MyReports.route) {
            MainScaffold(navController = navController) { paddingValues ->
                MyReportsScreen(modifier = Modifier.padding(paddingValues))
            }
        }

        composable(HaztrackDestination.Notifications.route) {
            MainScaffold(navController = navController) { paddingValues ->
                NotificationsScreen(modifier = Modifier.padding(paddingValues))
            }
        }

        composable(HaztrackDestination.Settings.route) {
            MainScaffold(navController = navController) { paddingValues ->
                SettingsScreen(
                    modifier = Modifier.padding(paddingValues),
                    onSignedOut = { navController.navigateToLoginAndClearStack() },
                )
            }
        }
    }
}

private fun NavHostController.navigateToHomeAndClearAuth() {
    navigate(HaztrackDestination.Home.route) {
        popUpTo(HaztrackDestination.Login.route) { inclusive = true }
    }
}

private fun NavHostController.navigateToLoginAndClearStack() {
    navigate(HaztrackDestination.Login.route) {
        popUpTo(0) { inclusive = true }
    }
}

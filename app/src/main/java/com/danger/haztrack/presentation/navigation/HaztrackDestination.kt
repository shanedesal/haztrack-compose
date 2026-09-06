package com.danger.haztrack.presentation.navigation

import android.net.Uri

sealed class HaztrackDestination(val route: String) {
    data object Login : HaztrackDestination("login")
    data object Register : HaztrackDestination("register")
    data object ForgotPassword : HaztrackDestination("forgot_password")
    data object Home : HaztrackDestination("home")
    data object Report : HaztrackDestination("report")
    data object MyReports : HaztrackDestination("my_reports")
    data object Notifications : HaztrackDestination("notifications")
    data object Settings : HaztrackDestination("settings")
    data object Profile : HaztrackDestination("profile")

    object ResetPassword : HaztrackDestination("reset_password/{email}"){
        fun createRoute(email: String) = "reset_password/${Uri.encode(email)}"
    }
}

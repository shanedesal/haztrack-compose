package com.danger.haztrack.presentation.navigation

sealed class HaztrackDestination(val route: String) {
    data object Login : HaztrackDestination("login")
    data object Register : HaztrackDestination("register")
    data object ForgotPassword : HaztrackDestination("forgot_password")
    data object Home : HaztrackDestination("home")

    object ResetPassword : HaztrackDestination("reset_password/{oobCode}"){
        fun createRoute(oobCode: String) = "reset_password/$oobCode"
    }
}

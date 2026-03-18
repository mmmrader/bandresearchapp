package com.tkachukmo.bandresearchapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tkachukmo.bandresearchapp.feature.auth.ui.ForgotPasswordScreen
import com.tkachukmo.bandresearchapp.feature.auth.ui.LoginScreen
import com.tkachukmo.bandresearchapp.feature.auth.ui.RegisterScreen

@Composable
fun BandResearchNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.DISCOVER) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.DISCOVER) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.DISCOVER) {
            // Тимчасова заглушка
            androidx.compose.material3.Text("Discover — скоро буде!")
        }
    }
}

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DISCOVER = "discover"
    const val CATALOG = "catalog"
    const val SEARCH = "search"
    const val EVENTS = "events"
    const val PROFILE = "profile"
    const val BAND_DETAIL = "band_detail/{bandId}"
    const val PLAYER = "player"
    const val BAND_MANAGER = "band_manager"
    const val NOTIFICATIONS = "notifications"
}
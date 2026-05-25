package com.tkachukmo.bandresearchapp.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.tkachukmo.bandresearchapp.feature.auth.ui.ForgotPasswordScreen
import com.tkachukmo.bandresearchapp.feature.auth.ui.LoginScreen
import com.tkachukmo.bandresearchapp.feature.auth.ui.RegisterScreen
import com.tkachukmo.bandresearchapp.feature.auth.ui.SplashScreen
import com.tkachukmo.bandresearchapp.feature.catalog.ui.BandDetailScreen
import com.tkachukmo.bandresearchapp.feature.catalog.ui.BandManagerScreen
import com.tkachukmo.bandresearchapp.feature.discover.ui.MainScreen
import com.tkachukmo.bandresearchapp.feature.discover.ui.NotificationsScreen
import com.tkachukmo.bandresearchapp.feature.discover.ui.PlayerScreen

@Composable
fun BandResearchNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
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
                    navController.navigate(Routes.MAIN) {
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

        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToBandDetail = { bandId ->
                    navController.navigate("${Routes.BAND_DETAIL_BASE}/$bandId")
                },
                onNavigateToNotifications = {
                    navController.navigate(Routes.NOTIFICATIONS)
                },
                onNavigateToBandManager = {
                    navController.navigate(Routes.BAND_MANAGER)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                // ДОДАНО: Тепер MainScreen знає, як відкрити плеєр при кліку на міні-плеєр
                onNavigateToPlayer = { trackId ->
                    navController.navigate("${Routes.PLAYER_BASE}/$trackId")
                }
            )
        }

        composable(Routes.BAND_DETAIL) { backStackEntry ->
            val bandId = backStackEntry.arguments?.getString("bandId") ?: "1"
            BandDetailScreen(
                bandId = bandId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlayTrack = { track ->
                    navController.navigate("${Routes.PLAYER_BASE}/${track.id}")
                }
            )
        }

        // ОНОВЛЕНО: Додані анімації виїзду/згортання та Deep Link для шторки
        composable(
            route = Routes.PLAYER,
            deepLinks = listOf(navDeepLink { uriPattern = "bandmatch://player" }),
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(400)
                )
            }
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId") ?: "1"
            PlayerScreen(
                trackId = trackId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.BAND_MANAGER) {
            BandManagerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val MAIN = "main"
    const val BAND_DETAIL_BASE = "band_detail"
    const val BAND_DETAIL = "band_detail/{bandId}"
    const val PLAYER_BASE = "player"
    const val PLAYER = "player/{trackId}"
    const val NOTIFICATIONS = "notifications"
    const val BAND_MANAGER = "band_manager"
}
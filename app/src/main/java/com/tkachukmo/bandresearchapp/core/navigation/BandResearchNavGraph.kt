package com.tkachukmo.bandresearchapp.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

import com.tkachukmo.bandresearchapp.feature.auth.ui.ForgotPasswordScreen
import com.tkachukmo.bandresearchapp.feature.auth.ui.LoginScreen
import com.tkachukmo.bandresearchapp.feature.auth.ui.RegisterScreen

import com.tkachukmo.bandresearchapp.feature.catalog.ui.BandDetailScreen
import com.tkachukmo.bandresearchapp.feature.catalog.ui.BandManagerScreen

import com.tkachukmo.bandresearchapp.feature.discover.ui.MainScreen
import com.tkachukmo.bandresearchapp.feature.discover.ui.NotificationsScreen
import com.tkachukmo.bandresearchapp.feature.discover.ui.PlayerScreen

import com.tkachukmo.bandresearchapp.feature.profile.ui.EditProfileScreen
import com.tkachukmo.bandresearchapp.feature.profile.ui.HelpScreen
import com.tkachukmo.bandresearchapp.feature.profile.ui.HistoryScreen
import com.tkachukmo.bandresearchapp.feature.profile.ui.PlaylistDetailScreen
import com.tkachukmo.bandresearchapp.feature.profile.ui.PlaylistsScreen
import com.tkachukmo.bandresearchapp.feature.profile.ui.ProfileScreen
import com.tkachukmo.bandresearchapp.feature.profile.ui.SecurityScreen

import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistDto

// -------------------- ROUTES --------------------

object Routes {

    // Auth
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // Main
    const val MAIN_BASE = "main"
    const val MAIN = "main?tabIndex={tabIndex}"

    // Catalog
    const val BAND_DETAIL_BASE = "band_detail"
    const val BAND_DETAIL = "band_detail/{bandId}"

    // Player
    const val PLAYER_BASE = "player"
    const val PLAYER = "player/{trackId}"

    // Other
    const val NOTIFICATIONS = "notifications"
    const val BAND_MANAGER = "band_manager"

    // Profile
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val PLAYLISTS = "playlists"
    const val HISTORY = "history"
    const val SECURITY = "security"
    const val HELP = "help"

    // Playlist detail — передаємо id і name через аргументи
    const val PLAYLIST_DETAIL_BASE = "playlist_detail"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}/{playlistName}/{isPublic}"
}

// -------------------- NAV GRAPH --------------------

@Composable
fun BandResearchNavGraph(
    modifier: Modifier = Modifier,
    startDestination: String = Routes.LOGIN,
    navController: NavHostController = rememberNavController()
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // -------------------- LOGIN --------------------

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN_BASE) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // -------------------- REGISTER --------------------

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.MAIN_BASE) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // -------------------- FORGOT PASSWORD --------------------

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // -------------------- MAIN SCREEN --------------------

        composable(
            route = Routes.MAIN,
            arguments = listOf(
                navArgument("tabIndex") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->

            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0

            MainScreen(
                initialTab = tabIndex,

                onNavigateToBandDetail = { bandId ->
                    navController.navigate("${Routes.BAND_DETAIL_BASE}/$bandId")
                },
                onNavigateToNotifications = {
                    navController.navigate(Routes.NOTIFICATIONS)
                },
                onNavigateToBandManager = {
                    navController.navigate(Routes.BAND_MANAGER)
                },
                onNavigateToPlayer = { trackId ->
                    navController.navigate("${Routes.PLAYER_BASE}/$trackId")
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN_BASE) { inclusive = true }
                    }
                },
                onNavigateToEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onNavigateToPlaylists = { navController.navigate(Routes.PLAYLISTS) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToSecurity = { navController.navigate(Routes.SECURITY) },
                onNavigateToHelp = { navController.navigate(Routes.HELP) }
            )
        }

        // -------------------- BAND DETAIL --------------------

        composable(Routes.BAND_DETAIL) { backStackEntry ->

            val bandId = backStackEntry.arguments?.getString("bandId") ?: "1"

            BandDetailScreen(
                bandId = bandId,

                onNavigateBack = {
                    navController.popBackStack()
                },

                onPlayTrack = { track ->
                    navController.navigate("${Routes.PLAYER_BASE}/${track.id}")
                },

                onNavigateToPlayer = { trackId ->
                    navController.navigate("${Routes.PLAYER_BASE}/$trackId")
                },

                onNavigateToTab = { tabIndex ->
                    navController.navigate("${Routes.MAIN_BASE}?tabIndex=$tabIndex") {
                        popUpTo(Routes.MAIN_BASE) { inclusive = true }
                    }
                }
            )
        }
        // -------------------- PLAYER --------------------

        composable(
            route = Routes.PLAYER,
            deepLinks = listOf(
                navDeepLink { uriPattern = "bandmatch://player" }
            ),
            enterTransition = {
                slideInVertically(initialOffsetY = { it }, animationSpec = tween(400))
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400))
            }
        ) { backStackEntry ->

            val trackId = backStackEntry.arguments?.getString("trackId") ?: "1"

            PlayerScreen(
                trackId = trackId,
                onNavigateBack = {
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(Routes.MAIN_BASE) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // -------------------- NOTIFICATIONS --------------------

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // -------------------- PROFILE --------------------

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToBandManager = { navController.navigate(Routes.BAND_MANAGER) },
                onNavigateToEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onNavigateToPlaylists = { navController.navigate(Routes.PLAYLISTS) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToSecurity = { navController.navigate(Routes.SECURITY) },
                onNavigateToHelp = { navController.navigate(Routes.HELP) },
                onNavigateToBandDetail = { bandId ->
                    navController.navigate("${Routes.BAND_DETAIL_BASE}/$bandId")
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // -------------------- PROFILE SUB SCREENS --------------------

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Список плейлистів
        composable(Routes.PLAYLISTS) {
            PlaylistsScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenPlaylist = { playlist ->
                    // Кодуємо ім'я плейлиста (може містити пробіли)
                    val encodedName = java.net.URLEncoder.encode(playlist.name, "UTF-8")
                    navController.navigate(
                        "${Routes.PLAYLIST_DETAIL_BASE}/${playlist.id}/$encodedName/${playlist.isPublic}"
                    )
                }
            )
        }

        // Деталі плейлиста (треки)
        composable(
            route = Routes.PLAYLIST_DETAIL,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.StringType },
                navArgument("playlistName") { type = NavType.StringType },
                navArgument("isPublic") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
            val encodedName = backStackEntry.arguments?.getString("playlistName") ?: ""
            val playlistName = java.net.URLDecoder.decode(encodedName, "UTF-8")
            val isPublic = backStackEntry.arguments?.getBoolean("isPublic") ?: false

            // Відновлюємо PlaylistDto з аргументів навігації
            val playlist = PlaylistDto(
                id = playlistId,
                userId = "",        // не потрібен для відображення треків
                name = playlistName,
                isPublic = isPublic
            )

            PlaylistDetailScreen(
                playlist = playlist,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { trackId ->
                    navController.navigate("${Routes.PLAYER_BASE}/$trackId")
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SECURITY) {
            SecurityScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HELP) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // -------------------- BAND MANAGER --------------------

        composable(Routes.BAND_MANAGER) {
            BandManagerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTab = { tabIndex ->
                    navController.navigate("${Routes.MAIN_BASE}?tabIndex=$tabIndex") {
                        popUpTo(Routes.MAIN_BASE) { inclusive = true }
                    }
                }
            )
        }
    }
}

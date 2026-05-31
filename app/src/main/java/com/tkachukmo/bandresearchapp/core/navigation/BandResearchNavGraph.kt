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

import com.tkachukmo.bandresearchapp.core.messages.ui.ChatListScreen
import com.tkachukmo.bandresearchapp.core.messages.ui.ChatDetailScreen

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

    // Playlist detail
    const val PLAYLIST_DETAIL_BASE = "playlist_detail"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}/{playlistName}/{isPublic}"

    // Чати
    const val CHAT_LIST = "chat_list"
    const val CHAT_DETAIL_BASE = "chat_detail"
    // chatPartnerId — UUID співрозмовника; chatName — URL-encoded ім'я (опціонально)
    const val CHAT_DETAIL = "chat_detail/{chatPartnerId}?chatName={chatName}"
}

// Хелпер для безпечного URL-encode імені чату
private fun encodeChatName(name: String): String =
    java.net.URLEncoder.encode(name, "UTF-8")

// -------------------- NAV GRAPH --------------------

@Composable
fun BandResearchNavGraph(
    modifier: Modifier = Modifier,
    startDestination: String = Routes.LOGIN,
    navController: NavHostController = rememberNavController()
) {

    // Загальний лямбда для переходу до чату — використовується і з BandManager, і з ChatList
    val navigateToChat: (partnerId: String, chatName: String) -> Unit = { partnerId, chatName ->
        val encoded = encodeChatName(chatName)
        navController.navigate("${Routes.CHAT_DETAIL_BASE}/$partnerId?chatName=$encoded")
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // -------------------- AUTH --------------------

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN_BASE) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

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

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        // -------------------- MAIN --------------------

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
                onNavigateToChatList = {
                    navController.navigate(Routes.CHAT_LIST)
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
                onNavigateBack = { navController.popBackStack() },
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
                },
                onNavigateToChat = navigateToChat
            )
        }

        // -------------------- PLAYER --------------------

        composable(
            route = Routes.PLAYER,
            deepLinks = listOf(navDeepLink { uriPattern = "bandmatch://player" }),
            enterTransition = { slideInVertically(initialOffsetY = { it }, animationSpec = tween(400)) },
            exitTransition = { slideOutVertically(targetOffsetY = { it }, animationSpec = tween(400)) }
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
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
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
            EditProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.PLAYLISTS) {
            PlaylistsScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenPlaylist = { playlist ->
                    val encodedName = java.net.URLEncoder.encode(playlist.name, "UTF-8")
                    navController.navigate(
                        "${Routes.PLAYLIST_DETAIL_BASE}/${playlist.id}/$encodedName/${playlist.isPublic}"
                    )
                }
            )
        }

        composable(
            route = Routes.PLAYLIST_DETAIL,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.StringType },
                navArgument("playlistName") { type = NavType.StringType },
                navArgument("isPublic") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val playlistId   = backStackEntry.arguments?.getString("playlistId") ?: return@composable
            val encodedName  = backStackEntry.arguments?.getString("playlistName") ?: ""
            val playlistName = java.net.URLDecoder.decode(encodedName, "UTF-8")
            val isPublic     = backStackEntry.arguments?.getBoolean("isPublic") ?: false

            PlaylistDetailScreen(
                playlist = PlaylistDto(id = playlistId, userId = "", name = playlistName, isPublic = isPublic),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { trackId ->
                    navController.navigate("${Routes.PLAYER_BASE}/$trackId")
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { trackId ->
                    navController.navigate("${Routes.PLAYER_BASE}/$trackId")
                }
            )
        }

        composable(Routes.SECURITY) {
            SecurityScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.HELP) {
            HelpScreen(onNavigateBack = { navController.popBackStack() })
        }

        // -------------------- BAND MANAGER --------------------

        composable(Routes.BAND_MANAGER) {
            BandManagerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTab = { tabIndex ->
                    navController.navigate("${Routes.MAIN_BASE}?tabIndex=$tabIndex") {
                        popUpTo(Routes.MAIN_BASE) { inclusive = true }
                    }
                },
                // Адмін натискає "Написати" -> відкривається ChatDetailScreen з цим кандидатом
                onNavigateToChat = navigateToChat
            )
        }

        // -------------------- ЧАТИ --------------------

        composable(Routes.CHAT_LIST) {
            ChatListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { partnerId ->
                    // З ChatList ім'я береться з профілю в ChatDetailScreen автоматично
                    navController.navigate("${Routes.CHAT_DETAIL_BASE}/$partnerId")
                }
            )
        }

        composable(
            route = Routes.CHAT_DETAIL,
            arguments = listOf(
                navArgument("chatPartnerId") { type = NavType.StringType },
                navArgument("chatName") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val partnerId   = backStackEntry.arguments?.getString("chatPartnerId") ?: ""
            val encodedName = backStackEntry.arguments?.getString("chatName") ?: ""
            val chatName    = try {
                java.net.URLDecoder.decode(encodedName, "UTF-8")
            } catch (e: Exception) { encodedName }

            ChatDetailScreen(
                chatPartnerId = partnerId,
                chatName      = chatName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
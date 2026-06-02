package com.tkachukmo.bandresearchapp.feature.discover.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tkachukmo.bandresearchapp.feature.catalog.ui.CatalogScreen
import com.tkachukmo.bandresearchapp.feature.profile.ui.ProfileScreen

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        label = "Каталог",
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic,
        route = "catalog"
    ),
    BottomNavItem(
        label = "Пошук",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        route = "search"
    ),
    BottomNavItem(
        label = "Події",
        selectedIcon = Icons.Filled.Event,
        unselectedIcon = Icons.Outlined.Event,
        route = "events"
    ),
    BottomNavItem(
        label = "Профіль",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        route = "profile"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    initialTab: Int = 0,
    onNavigateToBandDetail: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToBandManager: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToPlayer: (String) -> Unit = {},
    // НОВИЙ параметр для переходу до чатів
    onNavigateToChatList: () -> Unit = {},
    onRefreshApp: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToEqualizer: () -> Unit = {}
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(initialTab) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            when (selectedIndex) {
                0 -> TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🎵")
                            Text(
                                text = "BandMatch",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // ІКОНКА ЧАТІВ (повідомлення) — НОВА, зліва від дзвіночка
                        IconButton(onClick = onNavigateToChatList) {
                            Icon(Icons.Default.Message, contentDescription = "Повідомлення")
                        }
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Сповіщення")
                        }
                    }
                )
                1 -> TopAppBar(
                    title = { Text("Пошук") }
                )
                2 -> TopAppBar(
                    title = { Text("Події") }
                )
                3 -> TopAppBar(
                    title = { Text("Профіль") },
                    actions = {}
                )
            }
        },
        bottomBar = {
            Column {
                MiniPlayer(onNavigateToPlayer = onNavigateToPlayer)

                // Кнопка "Оновити" над NavBar
                RefreshBar(onRefresh = onRefreshApp)

                NavigationBar {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = {
                                Icon(
                                    imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        when (selectedIndex) {
            0 -> CatalogScreen(
                modifier = Modifier.padding(paddingValues),
                onBandClick = onNavigateToBandDetail
            )
            1 -> SearchScreen(
                modifier = Modifier.padding(paddingValues),
                onBandClick = onNavigateToBandDetail
            )
            2 -> EventsScreen(
                modifier = Modifier.padding(paddingValues)
            )
            3 -> ProfileScreen(
                modifier = Modifier.padding(paddingValues),
                onNavigateToBandManager = onNavigateToBandManager,
                onLogout = onLogout,
                onNavigateToEditProfile = onNavigateToEditProfile,
                onNavigateToPlaylists = onNavigateToPlaylists,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSecurity = onNavigateToSecurity,
                onNavigateToHelp = onNavigateToHelp,
                onNavigateToEqualizer = onNavigateToEqualizer,
                onNavigateToBandDetail = onNavigateToBandDetail
            )
        }
    }
}

// ----------------------------------------------------------------
// RefreshBar — смуга "Оновити" над таскбаром з перевіркою мережі
// ----------------------------------------------------------------
@Composable
fun RefreshBar(onRefresh: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    var noInternet by remember { mutableStateOf(false) }

    fun isOnline(): Boolean {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager
        val cap = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return cap.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            kotlinx.coroutines.delay(1200)
            isRefreshing = false
        }
    }

    if (noInternet) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "⚠️ Немає підключення до інтернету",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .clickable {
                if (!isOnline()) {
                    noInternet = true
                    return@clickable
                }
                noInternet = false
                isRefreshing = true
                onRefresh()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isRefreshing) {
            androidx.compose.material3.LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Оновити",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
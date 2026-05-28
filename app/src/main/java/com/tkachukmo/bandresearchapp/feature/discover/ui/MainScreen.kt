package com.tkachukmo.bandresearchapp.feature.discover.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
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

// Строго 4 менюшки, які є в нашому додатку
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

    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
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
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Сповіщення")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Sort, contentDescription = "Сортування")
                        }
                    }
                )
                1 -> TopAppBar(
                    title = { Text("Пошук") }
                )
                2 -> TopAppBar(
                    title = { Text("Події") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Map, contentDescription = "Карта")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Tune, contentDescription = "Фільтри")
                        }
                    }
                )
                3 -> TopAppBar(
                    title = { Text("Профіль") },
                    actions = {} // ВИПРАВЛЕНО ТУТ: Порожньо, ніяких олівців чи аватарок зверху!
                )
            }
        },
        bottomBar = {
            Column {
                MiniPlayer(onNavigateToPlayer = onNavigateToPlayer)

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
                onNavigateToBandDetail = onNavigateToBandDetail
            )
        }
    }
}

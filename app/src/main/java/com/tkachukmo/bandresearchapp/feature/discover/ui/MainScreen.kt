package com.tkachukmo.bandresearchapp.feature.discover.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Explore
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
        label = "Відкрити",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        route = "discover"
    ),
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
    onNavigateToBandDetail: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToBandManager: () -> Unit = {},
    onLogout: () -> Unit = {}
){
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
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
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Сповіщення"
                            )
                        }
                    }
                )
                1 -> TopAppBar(
                    title = { Text("Каталог") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = "Сортування"
                            )
                        }
                    }
                )
                2 -> TopAppBar(
                    title = { Text("Пошук") }
                )
                3 -> TopAppBar(
                    title = { Text("Події") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Map,
                                contentDescription = "Карта"
                            )
                        }
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Фільтри"
                            )
                        }
                    }
                )
                4 -> TopAppBar(
                    title = { Text("Профіль") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Редагувати"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedIndex == index)
                                    item.selectedIcon
                                else
                                    item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedIndex) {
            0 -> DiscoverScreen(
                modifier = Modifier.padding(paddingValues),
                onBandClick = onNavigateToBandDetail,
                onNavigateToNotifications = onNavigateToNotifications
            )
            1 -> CatalogScreen(
                modifier = Modifier.padding(paddingValues),
                onBandClick = onNavigateToBandDetail
            )
            2 -> SearchScreen(
                modifier = Modifier.padding(paddingValues),
                onBandClick = onNavigateToBandDetail
            )
            3 -> EventsScreen(
                modifier = Modifier.padding(paddingValues)
            )
            4 -> ProfileScreen(
                modifier = Modifier.padding(paddingValues),
                onNavigateToBandManager = onNavigateToBandManager,
                onLogout = onLogout
            )
        }
    }
}
package com.tkachukmo.bandresearchapp.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val hasSwitch: Boolean = false,
    val hasArrow: Boolean = true,
    val tint: Color? = null
)

val musicGenres = listOf(
    "Рок", "Поп", "Метал", "Джаз",
    "Фолк", "Електроніка", "Хіп-хоп", "Класика"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateToBandManager: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    val selectedGenres = remember {
        mutableStateOf(setOf("Рок", "Поп", "Фолк"))
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {

        // Шапка профілю
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "МТ",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Михайло Ткачук",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "m.tkachuk@email.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        ProfileStatItem(value = "24", label = "Підписки")
                        ProfileStatItem(value = "142", label = "Прослухано")
                        ProfileStatItem(value = "8", label = "Плейлисти")
                    }
                }
            }
        }

        // Музичні вподобання
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Музичні стилі",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(musicGenres) { genre ->
                    FilterChip(
                        selected = selectedGenres.value.contains(genre),
                        onClick = {
                            val current = selectedGenres.value.toMutableSet()
                            if (current.contains(genre)) current.remove(genre)
                            else current.add(genre)
                            selectedGenres.value = current
                        },
                        label = { Text(genre) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Статистика карток
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "💜",
                    value = "24",
                    label = "Підписок",
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🎵",
                    value = "142",
                    label = "Треків",
                    color = MaterialTheme.colorScheme.secondaryContainer
                )
                ProfileStatCard(
                    modifier = Modifier.weight(1f),
                    emoji = "🎬",
                    value = "38",
                    label = "Відео",
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Кнопка Band Manager
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onNavigateToBandManager() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Кабінет гурту",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Керуй своїм гуртом",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme
                                .onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Секція — Бібліотека
        item {
            ProfileSectionHeader(title = "Бібліотека")
        }

        item {
            ProfileMenuItemRow(
                item = ProfileMenuItem(
                    icon = Icons.Default.Favorite,
                    title = "Підписки",
                    subtitle = "24 гурти",
                    tint = Color(0xFFE91E63)
                ),
                onClick = {}
            )
            ProfileMenuItemRow(
                item = ProfileMenuItem(
                    icon = Icons.Default.LibraryMusic,
                    title = "Мої плейлисти",
                    subtitle = "8 плейлістів"
                ),
                onClick = {}
            )
            ProfileMenuItemRow(
                item = ProfileMenuItem(
                    icon = Icons.Default.MusicNote,
                    title = "Історія прослуховувань",
                    subtitle = "142 треки"
                ),
                onClick = {}
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Секція — Налаштування
        item {
            ProfileSectionHeader(title = "Налаштування")
        }

        item {
            ProfileMenuItemRow(
                item = ProfileMenuItem(
                    icon = Icons.Default.Person,
                    title = "Редагувати профіль"
                ),
                onClick = {}
            )
            ProfileMenuItemRow(
                item = ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Сповіщення",
                    hasSwitch = true,
                    hasArrow = false
                ),
                switchValue = notificationsEnabled,
                onSwitchChange = { notificationsEnabled = it },
                onClick = {}
            )
            ProfileMenuItemRow(
                item = ProfileMenuItem(
                    icon = Icons.Default.DarkMode,
                    title = "Темна тема",
                    hasSwitch = true,
                    hasArrow = false
                ),
                switchValue = isDarkMode,
                onSwitchChange = { isDarkMode = it },
                onClick = {}
            )
            ProfileMenuItemRow(
                item = ProfileMenuItem(
                    icon = Icons.Default.Security,
                    title = "Безпека та пароль"
                ),
                onClick = {}
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Секція — Інше
        item {
            ProfileSectionHeader(title = "Інше")
        }

        item {
            ProfileMenuItemRow(
                item = ProfileMenuItem(
                    icon = Icons.Default.Help,
                    title = "Допомога та підтримка"
                ),
                onClick = {}
            )
            ProfileMenuItemRow(
                item = ProfileMenuItem(
                    icon = Icons.Default.Logout,
                    title = "Вийти",
                    hasArrow = false,
                    tint = MaterialTheme.colorScheme.error
                ),
                onClick = onLogout
            )
        }
    }
}

@Composable
fun ProfileStatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    )
}

@Composable
fun ProfileMenuItemRow(
    item: ProfileMenuItem,
    switchValue: Boolean = false,
    onSwitchChange: (Boolean) -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!item.hasSwitch) onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.tint ?: MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = item.tint ?: MaterialTheme.colorScheme.onSurface
            )
            item.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (item.hasSwitch) {
            Switch(
                checked = switchValue,
                onCheckedChange = onSwitchChange
            )
        } else if (item.hasArrow) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
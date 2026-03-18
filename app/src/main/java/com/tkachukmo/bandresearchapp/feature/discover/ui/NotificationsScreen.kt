package com.tkachukmo.bandresearchapp.feature.discover.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NotifType {
    NEW_RELEASE, NEW_VIDEO, EVENT, NEWS, SYSTEM
}

data class NotificationItem(
    val id: String,
    val type: NotifType,
    val title: String,
    val body: String,
    val time: String,
    val isRead: Boolean = false,
    val emoji: String = ""
)

val sampleNotifications = listOf(
    NotificationItem(
        id = "1",
        type = NotifType.NEW_RELEASE,
        title = "Новий реліз — Океан Ельзи",
        body = "Вийшов новий альбом «Земля» — 12 треків вже доступні",
        time = "5 хв тому",
        isRead = false,
        emoji = "🎸"
    ),
    NotificationItem(
        id = "2",
        type = NotifType.EVENT,
        title = "Концерт через тиждень",
        body = "Океан Ельзи виступлять 12 квітня у Палаці Спорту, Київ",
        time = "2 год тому",
        isRead = false,
        emoji = "🎤"
    ),
    NotificationItem(
        id = "3",
        type = NotifType.NEW_VIDEO,
        title = "Новий кліп — Kazka",
        body = "Дивись новий відеокліп «Зоря» вже зараз",
        time = "5 год тому",
        isRead = true,
        emoji = "🎹"
    ),
    NotificationItem(
        id = "4",
        type = NotifType.NEWS,
        title = "Новини — DakhaBrakha",
        body = "DakhaBrakha вирушають у світовий тур по Європі та США",
        time = "1 день тому",
        isRead = true,
        emoji = "🥁"
    ),
    NotificationItem(
        id = "5",
        type = NotifType.NEW_RELEASE,
        title = "Новий сингл — Brutto",
        body = "Новий трек «Вільний» вже доступний для прослуховування",
        time = "2 дні тому",
        isRead = true,
        emoji = "🎸"
    ),
    NotificationItem(
        id = "6",
        type = NotifType.EVENT,
        title = "Фестиваль UkrainianWave",
        body = "3 травня — великий фестиваль у Brave Factory, Київ",
        time = "3 дні тому",
        isRead = true,
        emoji = "🎪"
    ),
    NotificationItem(
        id = "7",
        type = NotifType.SYSTEM,
        title = "Ласкаво просимо до BandMatch!",
        body = "Підпишись на улюблені гурти щоб отримувати сповіщення",
        time = "7 днів тому",
        isRead = true,
        emoji = "🎵"
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val notifications = remember {
        mutableStateListOf(*sampleNotifications.toTypedArray())
    }

    val unreadCount = notifications.count { !it.isRead }

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Сповіщення")
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$unreadCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
            },
            actions = {
                if (unreadCount > 0) {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            val updated = notifications.map { it.copy(isRead = true) }
                            notifications.clear()
                            notifications.addAll(updated)
                        }
                    ) {
                        Text(
                            text = "Всі прочитані",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        )

        if (notifications.isEmpty()) {
            // Порожній стан
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Немає сповіщень",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Підпишись на гурти щоб\nотримувати новини",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Непрочитані
                val unread = notifications.filter { !it.isRead }
                val read = notifications.filter { it.isRead }

                if (unread.isNotEmpty()) {
                    item {
                        Text(
                            text = "Нові",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        )
                    }
                    items(unread, key = { it.id }) { notif ->
                        NotificationRow(
                            notification = notif,
                            onClick = {
                                val index = notifications.indexOfFirst { it.id == notif.id }
                                if (index >= 0) {
                                    notifications[index] = notif.copy(isRead = true)
                                }
                            }
                        )
                    }
                }

                if (read.isNotEmpty()) {
                    item {
                        Text(
                            text = "Раніше",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        )
                    }
                    items(read, key = { it.id }) { notif ->
                        NotificationRow(
                            notification = notif,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationRow(
    notification: NotificationItem,
    onClick: () -> Unit = {}
) {
    val bgColor = if (!notification.isRead)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Іконка типу
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(notifTypeColor(notification.type)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = notification.emoji,
                fontSize = 20.sp
            )
        }

        // Контент
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!notification.isRead)
                    FontWeight.SemiBold
                else
                    FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Крапка непрочитаного
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterVertically)
            )
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun notifTypeColor(type: NotifType): Color {
    return when (type) {
        NotifType.NEW_RELEASE -> MaterialTheme.colorScheme.primaryContainer
        NotifType.NEW_VIDEO -> MaterialTheme.colorScheme.tertiaryContainer
        NotifType.EVENT -> MaterialTheme.colorScheme.secondaryContainer
        NotifType.NEWS -> MaterialTheme.colorScheme.surfaceVariant
        NotifType.SYSTEM -> MaterialTheme.colorScheme.surfaceVariant
    }
}

fun notifTypeIcon(type: NotifType): ImageVector {
    return when (type) {
        NotifType.NEW_RELEASE -> Icons.Default.MusicNote
        NotifType.NEW_VIDEO -> Icons.Default.LibraryMusic
        NotifType.EVENT -> Icons.Default.Event
        NotifType.NEWS -> Icons.Default.Newspaper
        NotifType.SYSTEM -> Icons.Default.Campaign
    }
}
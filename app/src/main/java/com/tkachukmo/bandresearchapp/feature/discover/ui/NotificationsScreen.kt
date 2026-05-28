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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tkachukmo.bandresearchapp.data.remote.dto.NotificationDto
import com.tkachukmo.bandresearchapp.feature.discover.viewmodel.NotificationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

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
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadCount.toString(),
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            actions = {
                if (unreadCount > 0) {
                    TextButton(onClick = { viewModel.markAllRead() }) {
                        Text("Усі прочитані")
                    }
                }
            }
        )

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            notifications.isEmpty() -> EmptyNotifications()
            else -> NotificationList(
                notifications = notifications,
                onNotificationClick = { viewModel.markAsRead(it.id) }
            )
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
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
                text = "Тут з'являться заявки, повідомлення та новини від гуртів.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun NotificationList(
    notifications: List<NotificationDto>,
    onNotificationClick: (NotificationDto) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        val unread = notifications.filter { !it.isRead }
        val read = notifications.filter { it.isRead }

        if (unread.isNotEmpty()) {
            item { NotificationSectionTitle("Нові", isPrimary = true) }
            items(unread, key = { it.id }) { notification ->
                NotificationRow(notification = notification, onClick = { onNotificationClick(notification) })
            }
        }

        if (read.isNotEmpty()) {
            item { NotificationSectionTitle("Раніше", isPrimary = false) }
            items(read, key = { it.id }) { notification ->
                NotificationRow(notification = notification, onClick = {})
            }
        }
    }
}

@Composable
private fun NotificationSectionTitle(text: String, isPrimary: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun NotificationRow(
    notification: NotificationDto,
    onClick: () -> Unit
) {
    val unread = !notification.isRead
    val type = notification.type.toNotificationType()
    val bgColor = if (unread) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(type.color()),
            contentAlignment = Alignment.Center
        ) {
            Icon(type.icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.createdAt?.take(16)?.replace("T", " ") ?: "щойно",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (unread) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterVertically)
            )
        }
    }
}

private enum class NotificationType(val icon: ImageVector) {
    Release(Icons.Default.LibraryMusic),
    Video(Icons.Default.MusicNote),
    Event(Icons.Default.Event),
    Application(Icons.Default.PersonSearch),
    Message(Icons.Default.Email),
    System(Icons.Default.Campaign),
    Read(Icons.Default.MarkEmailRead)
}

@Composable
private fun NotificationType.color(): Color {
    return when (this) {
        NotificationType.Release -> MaterialTheme.colorScheme.primaryContainer
        NotificationType.Video -> MaterialTheme.colorScheme.tertiaryContainer
        NotificationType.Event -> MaterialTheme.colorScheme.secondaryContainer
        NotificationType.Application -> MaterialTheme.colorScheme.primaryContainer
        NotificationType.Message -> MaterialTheme.colorScheme.secondaryContainer
        NotificationType.System -> MaterialTheme.colorScheme.surfaceVariant
        NotificationType.Read -> MaterialTheme.colorScheme.surfaceVariant
    }
}

private fun String.toNotificationType(): NotificationType {
    return when (this) {
        "new_release" -> NotificationType.Release
        "new_video" -> NotificationType.Video
        "event", "concert", "tour" -> NotificationType.Event
        "new_application", "application_status" -> NotificationType.Application
        "message" -> NotificationType.Message
        "read" -> NotificationType.Read
        else -> NotificationType.System
    }
}

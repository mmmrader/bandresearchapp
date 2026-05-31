package com.tkachukmo.bandresearchapp.core.messages.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.tkachukmo.bandresearchapp.core.messages.viewmodel.ChatPreview
import com.tkachukmo.bandresearchapp.core.messages.viewmodel.ChatViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val chatList by viewModel.chatList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChatList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Повідомлення", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            chatList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "У вас ще немає діалогів",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Адміністратор гурту може написати вам після подачі заявки",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(chatList, key = { it.chatPartnerId }) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = { onNavigateToChat(chat.chatPartnerId) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 82.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар
        Surface(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            if (chat.chatPartnerAvatar != null) {
                AsyncImage(
                    model = chat.chatPartnerAvatar,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = chat.chatPartnerName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.chatPartnerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                chat.lastMessageTime?.let { iso ->
                    Text(
                        text = formatChatTime(iso),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = chat.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Форматуємо ISO timestamp у людський вигляд: "14:32" або "вчора" або "12.05"
 */
private fun formatChatTime(isoString: String): String {
    return try {
        val odt = OffsetDateTime.parse(isoString)
        val local = odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        val now = java.time.LocalDateTime.now()

        when {
            local.toLocalDate() == now.toLocalDate() ->
                local.format(DateTimeFormatter.ofPattern("HH:mm"))
            local.toLocalDate() == now.toLocalDate().minusDays(1) ->
                "вчора"
            else ->
                local.format(DateTimeFormatter.ofPattern("dd.MM"))
        }
    } catch (e: DateTimeParseException) {
        ""
    }
}
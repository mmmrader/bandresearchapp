package com.tkachukmo.bandresearchapp.core.messages.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tkachukmo.bandresearchapp.core.messages.viewmodel.ChatViewModel

private val SAFE_DOMAINS = listOf(
    "youtube.com", "youtu.be",
    "t.me", "telegram.me",
    "spotify.com", "open.spotify.com",
    "instagram.com", "tiktok.com",
    "facebook.com", "fb.com",
    "twitter.com", "x.com",
    "soundcloud.com", "bandcamp.com",
    "apple.com", "music.apple.com",
    "google.com", "wikipedia.org", "github.com"
)

private val URL_REGEX = "(https?://[\\w\\d./_\\-?=&%+#@~:!*,;]+)".toRegex()

private const val SAFE_LINK_TAG   = "SAFE_LINK"
private const val UNSAFE_LINK_TAG = "UNSAFE_LINK"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatPartnerId: String,
    chatName: String = "Чат",
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages       by viewModel.messages.collectAsState()
    val partnerProfile by viewModel.partnerProfile.collectAsState()
    val currentUserId  = viewModel.currentUserId ?: ""
    val context        = LocalContext.current
    val listState      = rememberLazyListState()

    var text by remember { mutableStateOf("") }

    val displayName = partnerProfile?.displayName?.takeIf { it.isNotBlank() } ?: chatName

    LaunchedEffect(chatPartnerId) {
        viewModel.openChat(chatPartnerId)
    }

    DisposableEffect(chatPartnerId) {
        onDispose { viewModel.closeChat() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(displayName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Повідомлення...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.sendMessage(chatPartnerId, text)
                            text = ""
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Надіслати")
                }
            }
        }
    ) { paddingValues ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Почніть розмову першим!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        text      = msg.text,
                        isMe      = msg.senderId == currentUserId,
                        timestamp = msg.createdAt
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(text: String, isMe: Boolean, timestamp: String? = null) {
    val context = LocalContext.current
    var warningUrl by remember { mutableStateOf<String?>(null) }

    val textColor = if (isMe)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val linkColor = if (isMe) Color(0xFF4FC3F7) else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isMe) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(
                            topStart    = 16.dp,
                            topEnd      = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd   = if (isMe) 4.dp  else 16.dp
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Будуємо AnnotatedString — стабільний підхід без FlowRow
                val annotated = buildAnnotatedString {
                    val matches = URL_REGEX.findAll(text).toList()

                    if (matches.isEmpty()) {
                        withStyle(SpanStyle(color = textColor)) { append(text) }
                    } else {
                        var cursor = 0
                        for (match in matches) {
                            // Звичайний текст до посилання
                            if (match.range.first > cursor) {
                                withStyle(SpanStyle(color = textColor)) {
                                    append(text.substring(cursor, match.range.first))
                                }
                            }
                            val url    = match.value
                            val isSafe = SAFE_DOMAINS.any { url.contains(it, ignoreCase = true) }
                            val tag    = if (isSafe) SAFE_LINK_TAG else UNSAFE_LINK_TAG

                            pushStringAnnotation(tag = tag, annotation = url)
                            withStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.SemiBold)) {
                                append(url)
                                if (!isSafe) append(" ⚠️")
                            }
                            pop()

                            cursor = match.range.last + 1
                        }
                        // Залишок тексту
                        if (cursor < text.length) {
                            withStyle(SpanStyle(color = textColor)) {
                                append(text.substring(cursor))
                            }
                        }
                    }
                }

                ClickableText(
                    text  = annotated,
                    style = MaterialTheme.typography.bodyMedium,
                    onClick = { offset ->
                        // Спочатку перевіряємо безпечні посилання
                        annotated.getStringAnnotations(SAFE_LINK_TAG, offset, offset)
                            .firstOrNull()?.let { ann ->
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(ann.item))
                                )
                                return@ClickableText
                            }
                        // Потім небезпечні — показуємо попередження
                        annotated.getStringAnnotations(UNSAFE_LINK_TAG, offset, offset)
                            .firstOrNull()?.let { ann ->
                                warningUrl = ann.item
                            }
                    }
                )
            }

            // Час
            if (timestamp != null) {
                Text(
                    text  = formatMessageTime(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }

    // Діалог-попередження для підозрілих посилань
    if (warningUrl != null) {
        AlertDialog(
            onDismissRequest = { warningUrl = null },
            title = { Text("⚠️ Підозріле посилання") },
            text = {
                Text(
                    "Братан, обережно з цим покликанням! Воно веде на неперевірений ресурс:\n\n" +
                            "${warningUrl}\n\n" +
                            "Ми не можемо гарантувати безпеку цього сайту. Ти впевнений, що хочеш перейти?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(warningUrl)))
                        warningUrl = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Все одно перейти") }
            },
            dismissButton = {
                TextButton(onClick = { warningUrl = null }) { Text("Скасувати") }
            }
        )
    }
}

private fun formatMessageTime(isoString: String): String {
    return try {
        val odt   = java.time.OffsetDateTime.parse(isoString)
        val local = odt.atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime()
        local.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) { "" }
}
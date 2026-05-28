package com.tkachukmo.bandresearchapp.feature.discover.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tkachukmo.bandresearchapp.data.remote.dto.BandEventDto
import com.tkachukmo.bandresearchapp.data.remote.dto.EventCommentDto
import com.tkachukmo.bandresearchapp.feature.discover.viewmodel.EventsViewModel
import kotlinx.coroutines.launch

private data class EventFilter(val key: String, val label: String)

private val eventFilters = listOf(
    EventFilter("all", "Усі"),
    EventFilter("release", "Релізи"),
    EventFilter("video", "Кліпи"),
    EventFilter("concert", "Концерти"),
    EventFilter("tour", "Тури"),
    EventFilter("news", "Новини")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val events by viewModel.events.collectAsState()
    val followedBands by viewModel.followedBands.collectAsState()
    val likedIds by viewModel.likedEventIds.collectAsState()
    val rsvpIds by viewModel.rsvpEventIds.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var selectedFilter by remember { mutableStateOf("all") }
    var selectedEvent by remember { mutableStateOf<BandEventDto?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val visibleEvents = remember(events, selectedFilter) {
        if (selectedFilter == "all") events else events.filter { it.type == selectedFilter }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Стрічка підписок",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (followedBands.isEmpty()) {
                                "Тут з'являються новини гуртів, за якими ви стежите"
                            } else {
                                "Події від ${followedBands.size} ваших підписок"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(eventFilters) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter.key,
                                onClick = { selectedFilter = filter.key },
                                label = { Text(filter.label) },
                                leadingIcon = if (selectedFilter == filter.key) {
                                    { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (visibleEvents.isEmpty()) {
                    item {
                        EmptyEventsState(
                            hasSubscriptions = followedBands.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                } else {
                    items(visibleEvents, key = { it.id }) { event ->
                        EventFeedCard(
                            event = event,
                            isLiked = event.id in likedIds,
                            isGoing = event.id in rsvpIds,
                            onLike = { viewModel.toggleLike(event.id) },
                            onRsvp = { viewModel.toggleRsvp(event.id) },
                            onComments = {
                                selectedEvent = event
                                viewModel.loadComments(event.id)
                            }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    selectedEvent?.let { event ->
        ModalBottomSheet(
            onDismissRequest = { selectedEvent = null },
            sheetState = sheetState
        ) {
            EventCommentsSheet(
                event = event,
                comments = comments[event.id].orEmpty(),
                onAddComment = { viewModel.addComment(event.id, it) },
                onClose = { selectedEvent = null }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventFeedCard(
    event: BandEventDto,
    isLiked: Boolean,
    isGoing: Boolean,
    onLike: () -> Unit,
    onRsvp: () -> Unit,
    onComments: () -> Unit
) {
    val context = LocalContext.current
    val accent = eventAccent(event.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accent.container, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(accent.icon, contentDescription = null, tint = accent.content)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.bandName ?: "Гурт",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = eventTitleLabel(event.type),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = shortDate(event),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            event.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (event.venue != null || event.city != null || event.eventDate != null) {
                Spacer(modifier = Modifier.height(10.dp))
                EventMetaLine(event)
            }

            val links = streamingLinks(event)
            if (links.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    links.forEach { (label, url) ->
                        AssistChip(
                            onClick = { openUrl(context, url) },
                            label = { Text(label) },
                            trailingIcon = {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Лайк",
                        tint = if (isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("${event.likesCount}")

                IconButton(onClick = onComments) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Коментарі")
                }
                Text("${event.commentsCount}")

                Spacer(modifier = Modifier.weight(1f))

                if (event.type == "concert" || event.type == "tour") {
                    val buttonText = if (isGoing) "Йду (${event.rsvpCount})" else "Піду (${event.rsvpCount})"
                    if (isGoing) {
                        Button(onClick = onRsvp) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(buttonText)
                        }
                    } else {
                        OutlinedButton(onClick = onRsvp) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(buttonText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventMetaLine(event: BandEventDto) {
    Text(
        text = listOfNotNull(event.eventDate, event.venue, event.city).joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun EventCommentsSheet(
    event: BandEventDto,
    comments: List<EventCommentDto>,
    onAddComment: (String) -> Unit,
    onClose: () -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(20.dp)) {
        Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (comments.isEmpty()) {
            Text(
                "Коментарів ще немає",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            comments.forEach { comment ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(comment.authorName ?: "Музикант", fontWeight = FontWeight.SemiBold)
                    Text(comment.text, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text("Коментар") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                Text("Закрити")
            }
            Button(
                onClick = {
                    onAddComment(draft)
                    draft = ""
                    scope.launch { }
                },
                enabled = draft.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Надіслати")
            }
        }
    }
}

@Composable
private fun EmptyEventsState(hasSubscriptions: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hasSubscriptions) "У підписок поки немає нових подій" else "Підпишіться на гурти",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (hasSubscriptions) {
                "Нові релізи, кліпи та концерти з'являться тут автоматично."
            } else {
                "Стрічка показує тільки активність гуртів, за якими ви стежите."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class EventAccent(
    val icon: ImageVector,
    val container: Color,
    val content: Color
)

@Composable
private fun eventAccent(type: String): EventAccent {
    return when (type) {
        "release" -> EventAccent(Icons.Default.LibraryMusic, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        "video" -> EventAccent(Icons.Default.Movie, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        "concert", "tour" -> EventAccent(Icons.Default.Event, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        "live" -> EventAccent(Icons.Default.Podcasts, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        else -> EventAccent(Icons.Default.MusicNote, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
    }
}

private fun eventTitleLabel(type: String): String {
    return when (type) {
        "release" -> "Музичний реліз"
        "video" -> "Кліп"
        "concert" -> "Концерт"
        "tour" -> "Тур"
        "live" -> "Живий виступ"
        else -> "Новина"
    }
}

private fun streamingLinks(event: BandEventDto): List<Pair<String, String>> {
    return listOfNotNull(
        event.smartLink?.let { "Smart-link" to it },
        event.spotifyUrl?.let { "Spotify" to it },
        event.appleMusicUrl?.let { "Apple Music" to it },
        event.youtubeMusicUrl?.let { "YouTube Music" to it }
    )
}

private fun shortDate(event: BandEventDto): String {
    val source = event.createdAt ?: event.eventDate ?: return ""
    return source.take(10)
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

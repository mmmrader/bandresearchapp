package com.tkachukmo.bandresearchapp.feature.discover.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class BandEvent(
    val id: String,
    val bandName: String,
    val title: String,
    val venue: String,
    val city: String,
    val day: String,
    val month: String,
    val type: String,
    val genre: String,
    val color: Color
)

val sampleEvents = listOf(
    BandEvent(
        id = "1",
        bandName = "Океан Ельзи",
        title = "Океан Ельзи — Tour 2025",
        venue = "Палац Спорту",
        city = "Київ",
        day = "12",
        month = "Квіт",
        type = "Концерт",
        genre = "Рок",
        color = Color(0xFF4A0080)
    ),
    BandEvent(
        id = "2",
        bandName = "Kazka",
        title = "Kazka — Великий концерт",
        venue = "Жовтневий Палац",
        city = "Київ",
        day = "19",
        month = "Квіт",
        type = "Концерт",
        genre = "Поп",
        color = Color(0xFF006064)
    ),
    BandEvent(
        id = "3",
        bandName = "Різні гурти",
        title = "UkrainianWave Festival",
        venue = "Brave Factory",
        city = "Київ",
        day = "03",
        month = "Трав",
        type = "Фестиваль",
        genre = "Різне",
        color = Color(0xFF880E4F)
    ),
    BandEvent(
        id = "4",
        bandName = "DakhaBrakha",
        title = "DakhaBrakha Live",
        venue = "Stereo Plaza",
        city = "Київ",
        day = "17",
        month = "Трав",
        type = "Концерт",
        genre = "Фолк",
        color = Color(0xFF880E4F)
    ),
    BandEvent(
        id = "5",
        bandName = "Haydamaky",
        title = "Haydamaky — Весняний тур",
        venue = "Atlas",
        city = "Київ",
        day = "24",
        month = "Трав",
        type = "Концерт",
        genre = "Рок-фолк",
        color = Color(0xFFE65100)
    ),
    BandEvent(
        id = "6",
        bandName = "Brutto",
        title = "Brutto — Rock Night",
        venue = "Caribbean Club",
        city = "Харків",
        day = "07",
        month = "Черв",
        type = "Концерт",
        genre = "Рок",
        color = Color(0xFF1B5E20)
    ),
)

val eventTypeFilters = listOf(
    "Всі", "Концерти", "Фестивалі", "Презентації", "Онлайн"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf("Всі") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredEvents = if (selectedType == "Всі") {
        sampleEvents
    } else {
        sampleEvents.filter { it.type.contains(selectedType.dropLast(1), ignoreCase = true) }
    }

    // Групуємо по місяцях
    val groupedEvents = filteredEvents.groupBy { it.month }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                // Фільтри типів
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(eventTypeFilters) { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Події згруповані по місяцях
                groupedEvents.forEach { (month, events) ->
                    item {
                        Text(
                            text = getMonthFull(month),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        )
                    }

                    items(events) { event ->
                        EventCard(
                            event = event,
                            onAddToCalendar = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "📅 ${event.title} додано в календар"
                                    )
                                }
                            },
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "📍 ${event.title} · ${event.venue}"
                                    )
                                }
                            }
                        )
                    }
                }

                // Секція новин
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Новини гуртів",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Всі",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {}
                        )
                    }
                }

                // Новини
                val newsList = listOf(
                    Triple("🎸", "Океан Ельзи анонсували новий альбом у 2025 році", "2 год. тому"),
                    Triple("🎹", "Kazka випустили новий сингл 'Зоря'", "5 год. тому"),
                    Triple("🥁", "DakhaBrakha вирушають у світовий тур", "1 день тому"),
                )

                items(newsList) { (emoji, title, time) ->
                    NewsRow(
                        emoji = emoji,
                        title = title,
                        time = time
                    )
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun EventCard(
    event: BandEvent,
    onAddToCalendar: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Дата блок
            Card(
                modifier = Modifier
                    .width(64.dp)
                    .height(80.dp),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    bottomStart = 16.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = event.day,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = event.month.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Інфо
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "📍 ${event.venue}, ${event.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EventTag(text = event.type)
                    EventTag(text = event.genre)
                }
            }

            // Кнопка календаря
            IconButton(
                onClick = onAddToCalendar,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    Icons.Default.EventAvailable,
                    contentDescription = "Додати в календар",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun EventTag(text: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun NewsRow(
    emoji: String,
    title: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

fun getMonthFull(short: String): String {
    return when (short) {
        "Квіт" -> "Квітень 2025"
        "Трав" -> "Травень 2025"
        "Черв" -> "Червень 2025"
        else -> short
    }
}
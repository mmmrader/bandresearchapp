package com.tkachukmo.bandresearchapp.feature.catalog.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.feature.catalog.viewmodel.CatalogViewModel
import com.tkachukmo.bandresearchapp.feature.discover.ui.BandCard

data class GenreFilter(
    val label: String,
    val emoji: String
)

val genreFilters = listOf(
    GenreFilter("Всі", "🎵"),
    GenreFilter("Рок", "🎸"),
    GenreFilter("Поп", "🎹"),
    GenreFilter("Метал", "⚡"),
    GenreFilter("Джаз", "🎷"),
    GenreFilter("Електроніка", "🎧"),
    GenreFilter("Фолк", "🎻"),
    GenreFilter("Хіп-хоп", "🎤"),
)

// Функція-конвертер з мережевої моделі в UI-модель
fun BandDto.toBandCard(): BandCard {
    val defaultColors = listOf(
        Color(0xFF6750A4), Color(0xFF006064), Color(0xFF880E4F), Color(0xFFE65100), Color(0xFF1B5E20)
    )
    val colorIndex = this.id.hashCode().let { if (it == Int.MIN_VALUE) 0 else Math.abs(it) } % defaultColors.size

    return BandCard(
        id = this.id,
        name = this.name,
        genre = this.genres.firstOrNull() ?: "Різне",
        country = this.country ?: "Невідомо",
        emoji = "🎸", // Поки що ставимо заглушку, пізніше можна буде вантажити аватарку
        color = defaultColors[colorIndex], // Генеруємо випадковий стабільний колір
        tags = this.genres
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    onBandClick: (String) -> Unit = {},
    viewModel: CatalogViewModel = hiltViewModel() // Підключаємо ViewModel
) {
    // Спостерігаємо за станами з Supabase
    val bandsDto by viewModel.bands.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedGenre by remember { mutableStateOf("Всі") }

    // Конвертуємо DTO в UI-карточки
    val allBands = remember(bandsDto) { bandsDto.map { it.toBandCard() } }

    val filteredBands = if (selectedGenre == "Всі") {
        allBands
    } else {
        allBands.filter { band ->
            band.tags.any { it.contains(selectedGenre, ignoreCase = true) } ||
                    band.genre.contains(selectedGenre, ignoreCase = true)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            // Показуємо індикатор завантаження
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {

                // Жанрові фільтри
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(genreFilters) { genre ->
                            FilterChip(
                                selected = selectedGenre == genre.label,
                                onClick = { selectedGenre = genre.label },
                                label = {
                                    Text("${genre.emoji} ${genre.label}")
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Секція "Популярні зараз"
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Популярні зараз",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Грід гуртів (по 2 в ряд)
                items(filteredBands.chunked(2)) { rowBands ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowBands.forEach { band ->
                            BandGridCard(
                                band = band,
                                modifier = Modifier.weight(1f),
                                onClick = { onBandClick(band.id) }
                            )
                        }
                        if (rowBands.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (filteredBands.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("У цьому жанрі поки немає гуртів", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Секція "Топ гурти тижня"
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
                            text = "Топ гурти тижня",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Список топ гуртів
                items(filteredBands.take(5).mapIndexed { i, b -> Pair(i + 1, b) }) { (num, band) ->
                    BandListRow(
                        number = num,
                        band = band,
                        onClick = { onBandClick(band.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BandGridCard(
    band: BandCard,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Зображення / emoji
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = band.color)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = band.emoji, fontSize = 48.sp)
                    }
                }
            }

            // Інфо
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = band.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = band.genre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "2.4M",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BandListRow(
    number: Int,
    band: BandCard,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Номер
        Text(
            text = "$number",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp)
        )

        // Аватар
        Card(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = band.color)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = band.emoji, fontSize = 24.sp)
            }
        }

        // Інфо
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = band.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${band.genre} · ${band.country}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Підписники
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFE91E63),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "2.4M",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
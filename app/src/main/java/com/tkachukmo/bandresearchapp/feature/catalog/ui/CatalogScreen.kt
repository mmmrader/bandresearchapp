package com.tkachukmo.bandresearchapp.feature.catalog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.feature.catalog.viewmodel.CatalogViewModel

fun formatFollowersCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

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

@Composable
fun CatalogScreen(
    modifier: Modifier = Modifier,
    onBandClick: (String) -> Unit = {},
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val followedBands by viewModel.bands.collectAsState()
    val topBands by viewModel.topBands.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedGenre by remember { mutableStateOf("Всі") }

    val filteredFollowedBands = if (selectedGenre == "Всі") {
        followedBands
    } else {
        followedBands.filter { band ->
            band.genres.any { it.contains(selectedGenre, ignoreCase = true) }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage!!,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp) // Прибираємо зайві відступи
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(genreFilters) { genre ->
                                FilterChip(
                                    selected = selectedGenre == genre.label,
                                    onClick = { selectedGenre = genre.label },
                                    label = { Text("${genre.emoji} ${genre.label}") }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ваші підписки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (filteredFollowedBands.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Тут будуть гурти, на які ви підписані", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(filteredFollowedBands.chunked(2)) { rowBands ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Топ гурти", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    itemsIndexed(topBands.take(10)) { index, band ->
                        BandListRow(
                            number = index + 1,
                            band = band,
                            onClick = { onBandClick(band.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BandGridCard(band: BandDto, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                if (band.avatarUrl != null || band.coverUrl != null) {
                    AsyncImage(
                        model = band.avatarUrl ?: band.coverUrl,
                        contentDescription = band.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = "🎸", fontSize = 48.sp)
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = band.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1)
                Text(
                    text = band.genres.firstOrNull() ?: "Музика",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(12.dp))
                    Text(
                        text = formatFollowersCount(band.followersCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BandListRow(number: Int, band: BandDto, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "$number",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(24.dp)
        )

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (band.avatarUrl != null || band.coverUrl != null) {
                AsyncImage(
                    model = band.avatarUrl ?: band.coverUrl,
                    contentDescription = band.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = "🎸", fontSize = 24.sp)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = band.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = "${band.genres.firstOrNull() ?: "Музика"} · ${band.country ?: "Світ"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(14.dp))
            Text(
                text = formatFollowersCount(band.followersCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
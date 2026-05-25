package com.tkachukmo.bandresearchapp.feature.catalog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.feature.catalog.viewmodel.BandDetailViewModel

@Composable
fun BandDetailScreen(
    bandId: String,
    onNavigateBack: () -> Unit,
    onPlayTrack: (TrackDto) -> Unit,
    viewModel: BandDetailViewModel = hiltViewModel()
) {
    val band by viewModel.band.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Завантажуємо дані при вході на екран
    LaunchedEffect(bandId) {
        viewModel.loadBandDetails(bandId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        band?.let { currentBand ->
            BandDetailContent(
                band = currentBand,
                tracks = tracks,
                onNavigateBack = onNavigateBack,
                onPlayTrack = onPlayTrack
            )
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Гурт не знайдено")
            }
        }
    }
}

@Composable
fun BandDetailContent(
    band: BandDto,
    tracks: List<TrackDto>,
    onNavigateBack: () -> Unit,
    onPlayTrack: (TrackDto) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Треки", "Релізи", "Відео", "Про гурт")

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Hero Section (Обкладинка та кнопка назад)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Заглушка для обкладинки з градієнтом
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF6750A4), Color(0xFF21005D))
                            )
                        )
                )

                // Кнопка назад
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .padding(top = 48.dp, start = 16.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }

                // Назва гурту поверх обкладинки
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = band.name,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${band.genres.joinToString(", ")} · ${band.country ?: "Україна"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Кнопки дій
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { if (tracks.isNotEmpty()) onPlayTrack(tracks.first()) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Слухати")
                }
                OutlinedButton(
                    onClick = { /* FR-50 Підписка */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Підписатись")
                }
            }
        }

        // Вкладки
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Контент вкладок
        when (selectedTab) {
            0 -> { // Список треків (FR-30)
                items(tracks) { track ->
                    TrackItemRow(track = track, onPlay = { onPlayTrack(track) })
                }
            }
            1 -> { // Релізи (FR-12)
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Тут будуть альбоми гурту", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            3 -> { // Опис гурту
                item {
                    Text(
                        text = band.description ?: "Опис гурту відсутній.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Розділ у розробці", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackItemRow(
    track: TrackDto,
    onPlay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = "${track.durationSec / 60}:${(track.durationSec % 60).toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = { /* Додати в чергу FR-32 */ }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }
    }
}
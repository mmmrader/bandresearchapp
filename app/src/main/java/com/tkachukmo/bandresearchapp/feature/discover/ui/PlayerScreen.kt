package com.tkachukmo.bandresearchapp.feature.discover.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil3.compose.AsyncImage // ВАЖЛИВО: Правильний імпорт Coil 3
import com.tkachukmo.bandresearchapp.feature.discover.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    trackId: String,
    onNavigateBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val track by viewModel.track.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val duration by viewModel.trackDuration.collectAsState()

    // Дані з метаданих файлу (Title залишаємо як fallback)
    val extractedTitle by viewModel.extractedTitle.collectAsState()
    val extractedArtwork by viewModel.extractedArtwork.collectAsState()

    // Назва гурту з бази даних
    val bandName by viewModel.bandName.collectAsState()

    // Стани кнопок керування
    val shuffleMode by viewModel.shuffleModeEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    LaunchedEffect(trackId) {
        viewModel.loadTrack(trackId)
    }

    if (track == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // --- СТАРА ЛОГІКА ДЛЯ EMBEDDED ARTWORK ---
    // Ми використовуємо це ТІЛЬКИ як fallback, якщо немає URL з Supabase
    val embeddedArtworkBitmap = remember(extractedArtwork) {
        extractedArtwork?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }

    // Визначаємо, яку назву показувати
    val displayTitle = extractedTitle ?: track!!.title
    val displayArtist = bandName // Тепер тут динамічна назва з БД

    val artScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.85f,
        animationSpec = tween(durationMillis = 500),
        label = "artScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // --- 1. РОЗМИТИЙ ФОН ---
        // Ми використовуємо Coil також для завантаження фону і розмиваємо Modifier'ом
        if (track!!.coverUrl != null) {
            AsyncImage(
                model = track!!.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 80.dp) // Розмиття Modifier'ом
            )
        } else if (embeddedArtworkBitmap != null) {
            // Fallback до embedded картинки для фону
            Image(
                bitmap = embeddedArtworkBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 80.dp)
            )
        } else {
            // Заглушка, якщо картинки немає взагалі
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2A2A35), Color(0xFF121218))
                        )
                    )
            )
        }

        // Темний шар поверх розмитого фону
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Верхній бар
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.KeyboardArrowDown, "Згорнути", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("Зараз грає", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                IconButton(onClick = { /* Меню */ }) {
                    Icon(Icons.Default.MoreVert, "Опції", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- 2. ОБКЛАДИНКА ТРЕКУ (В КВАДРАТІ) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .scale(artScale)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.DarkGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                // Пріоритет завантаження: URL > Embedded > Icon
                if (track!!.coverUrl != null) {
                    AsyncImage(
                        model = track!!.coverUrl,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (embeddedArtworkBitmap != null) {
                    Image(
                        bitmap = embeddedArtworkBitmap,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Інфо про трек
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = displayArtist,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = { /* Додати в обране */ }) {
                    Icon(Icons.Default.FavoriteBorder, "В обране", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Slider(
                value = progress,
                onValueChange = { viewModel.seekTo(it) },
                valueRange = 0f..(duration.takeIf { it > 0f } ?: 100f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(progress.toInt()), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                Text(formatTime(duration.toInt()), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f)
                    )
                }

                IconButton(onClick = { viewModel.skipToPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, "Попередній", tint = Color.White, modifier = Modifier.size(40.dp))
                }

                FilledIconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }

                IconButton(onClick = { viewModel.skipToNext() }) {
                    Icon(Icons.Default.SkipNext, "Наступний", tint = Color.White, modifier = Modifier.size(40.dp))
                }

                IconButton(onClick = { viewModel.toggleRepeat() }) {
                    val repeatTint = if (repeatMode == Player.REPEAT_MODE_OFF) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                    Icon(Icons.Default.Repeat, "Repeat", tint = repeatTint)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun formatTime(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "$min:${sec.toString().padStart(2, '0')}"
}
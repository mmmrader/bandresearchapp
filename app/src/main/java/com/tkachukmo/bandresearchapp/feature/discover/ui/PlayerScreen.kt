package com.tkachukmo.bandresearchapp.feature.discover.ui

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.feature.discover.viewmodel.PlayerViewModel
import kotlin.math.roundToInt

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
    val upcomingTracks by viewModel.upcomingTracks.collectAsState()

    val extractedTitle by viewModel.extractedTitle.collectAsState()
    val extractedArtwork by viewModel.extractedArtwork.collectAsState()
    val bandName by viewModel.bandName.collectAsState()
    val shuffleMode by viewModel.shuffleModeEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val uniqueListenersCount by viewModel.uniqueListenersCount.collectAsState()

    val playlists by viewModel.playlists.collectAsState()

    var isQueueVisible by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Перевірка орієнтації екрану
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(trackId) {
        viewModel.loadTrack(trackId)
        viewModel.checkIfTrackIsLiked(trackId)
    }

    if (track == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val embeddedArtworkBitmap = remember(extractedArtwork) {
        extractedArtwork?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
        }
    }

    val displayTitle = extractedTitle ?: track!!.title
    val displayArtist = bandName

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        // ================= MAIN (Background + UI) =================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = isQueueVisible) { isQueueVisible = false }
                .graphicsLayer {
                    scaleX = if (isQueueVisible) 0.92f else 1f
                    scaleY = if (isQueueVisible) 0.92f else 1f
                    clip = true
                    shape = RoundedCornerShape(if (isQueueVisible) 24.dp.toPx() else 0f)
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -20f && !isQueueVisible) isQueueVisible = true
                        if (dragAmount > 20f && !isQueueVisible) onNavigateBack()
                    }
                }
        ) {
            // Фон з блюром (Залишається однаковим для обох орієнтацій)
            if (track!!.coverUrl != null) {
                AsyncImage(
                    model = track!!.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(80.dp)
                )
            } else if (embeddedArtworkBitmap != null) {
                Image(
                    bitmap = embeddedArtworkBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().blur(80.dp)
                )
            } else {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color(0xFF2A2A35), Color(0xFF121218)))
                    )
                )
            }

            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)))

            // КОНТЕНТ ПЛЕЄРА ЗАЛЕЖНО ВІД ОРІЄНТАЦІЇ
            if (isLandscape) {
                // --- АЛЬБОМНА ОРІЄНТАЦІЯ ---
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ліва частина: Обкладинка
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayerArtwork(track!!, embeddedArtworkBitmap, isPlaying, viewModel)
                    }

                    // Права частина: Керування
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight()
                            .padding(end = 24.dp, top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Верхня панель
                        Row(
                            Modifier.fillMaxWidth(),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                            }
                            Text("Зараз грає", color = Color.White.copy(0.8f))
                            IconButton(onClick = {
                                viewModel.loadUserPlaylists()
                                showBottomSheet = true
                            }) {
                                Icon(Icons.Default.MoreVert, null, tint = Color.White)
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        // Інформація про трек
                        TrackInfoRow(displayTitle, displayArtist, uniqueListenersCount, isLiked) {
                            viewModel.toggleLikeTrack(track!!.id)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Повзунок
                        PlayerSlider(progress, duration) {
                            viewModel.seekTo(it)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Кнопки плеєра
                        PlayerControls(isPlaying, shuffleMode, repeatMode, viewModel)

                        Spacer(Modifier.weight(1f))

                        // Індикатор черги
                        Box(modifier = Modifier.fillMaxWidth().height(20.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            } else {
                // --- ПОРТРЕТНА ОРІЄНТАЦІЯ ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Верхня панель
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                        }
                        Text("Зараз грає", color = Color.White.copy(0.8f))
                        IconButton(onClick = {
                            viewModel.loadUserPlaylists()
                            showBottomSheet = true
                        }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.White)
                        }
                    }

                    // Обкладинка треку
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        PlayerArtwork(track!!, embeddedArtworkBitmap, isPlaying, viewModel)
                    }

                    // Блок керування
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        TrackInfoRow(displayTitle, displayArtist, uniqueListenersCount, isLiked) {
                            viewModel.toggleLikeTrack(track!!.id)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        PlayerSlider(progress, duration) {
                            viewModel.seekTo(it)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        PlayerControls(isPlaying, shuffleMode, repeatMode, viewModel)

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(modifier = Modifier.fillMaxWidth().height(20.dp), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // ================= QUEUE =================
        AnimatedVisibility(isQueueVisible, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)))
        }

        AnimatedVisibility(
            isQueueVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            var dragOffset by remember { mutableFloatStateOf(0f) }

            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .offset { IntOffset(0, dragOffset.roundToInt()) }
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color(0xFF18181C))
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragOffset > 200f) isQueueVisible = false
                                dragOffset = 0f
                            }
                        ) { c, d ->
                            c.consume()
                            if (d > 0) dragOffset += d
                        }
                    }
            ) {
                QueueHeader { isQueueVisible = false }

                if (upcomingTracks.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Черга порожня", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(upcomingTracks) { nextTrack ->
                            QueueTrackItem(nextTrack, displayArtist)
                        }
                    }
                }
            }
        }

        // ================= BOTTOM SHEET =================
        if (showBottomSheet && track != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                PlayerTrackOptionsSheet(
                    track = track!!,
                    playlists = playlists,
                    onAddToQueue = {
                        viewModel.addTrackToQueue(track!!, displayArtist)
                        showBottomSheet = false
                    },
                    onAddToPlaylist = { playlistId ->
                        viewModel.addTrackToPlaylist(playlistId, track!!.id)
                        showBottomSheet = false
                    }
                )
            }
        }
    }
}

// ==========================================
// КОМПОНЕНТИ ІНТЕРФЕЙСУ
// ==========================================

@Composable
private fun PlayerArtwork(track: TrackDto, embeddedBitmap: androidx.compose.ui.graphics.ImageBitmap?, isPlaying: Boolean, viewModel: PlayerViewModel) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = dragOffsetX, label = "offsetX")
    val artScale by animateFloatAsState(targetValue = if (isPlaying) 1f else 0.85f, animationSpec = tween(durationMillis = 500), label = "artScale")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f) // Зберігає квадратну форму
            .scale(artScale)
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .clip(RoundedCornerShape(24.dp))
            .background(Color.DarkGray.copy(alpha = 0.5f))
            .pointerInput(track.id) {
                detectDragGestures(onDragEnd = {
                    if (dragOffsetX < -150f) viewModel.skipToNext() else if (dragOffsetX > 150f) viewModel.skipToPrevious()
                    dragOffsetX = 0f
                }) { change, dragAmount ->
                    change.consume()
                    dragOffsetX += dragAmount.x
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (track.coverUrl != null) AsyncImage(model = track.coverUrl, contentDescription = "Cover", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else if (embeddedBitmap != null) Image(bitmap = embeddedBitmap, contentDescription = "Cover", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        else Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(100.dp), tint = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
private fun TrackInfoRow(title: String, artist: String, uniqueListenersCount: Int, isLiked: Boolean, onLikeClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = artist, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = "$uniqueListenersCount унікальних слухачів",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onLikeClick) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun PlayerSlider(progress: Float, duration: Float, onSeek: (Float) -> Unit) {
    val sliderRange = 0f..(duration.takeIf { it > 0f } ?: 100f)
    Slider(
        value = progress,
        onValueChange = onSeek,
        valueRange = sliderRange,
        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(alpha = 0.3f))
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(formatTime(progress.toInt()), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
        Text(formatTime(duration.toInt()), color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun PlayerControls(isPlaying: Boolean, shuffleMode: Boolean, repeatMode: Int, viewModel: PlayerViewModel) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { viewModel.toggleShuffle() }) { Icon(Icons.Default.Shuffle, "Shuffle", tint = if (shuffleMode) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f)) }
        IconButton(onClick = { viewModel.skipToPrevious() }) { Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(40.dp)) }
        FilledIconButton(onClick = { viewModel.togglePlayPause() }, modifier = Modifier.size(72.dp), shape = CircleShape, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play", modifier = Modifier.size(40.dp), tint = Color.White)
        }
        IconButton(onClick = { viewModel.skipToNext() }) { Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(40.dp)) }
        IconButton(onClick = { viewModel.toggleRepeat() }) {
            val repeatTint = if (repeatMode == Player.REPEAT_MODE_OFF) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
            Icon(Icons.Default.Repeat, "Repeat", tint = repeatTint)
        }
    }
}

@Composable
private fun QueueHeader(onHide: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 12.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.width(40.dp).height(4.dp)) {
                drawRoundRect(color = Color.White.copy(alpha = 0.2f), size = Size(40.dp.toPx(), 4.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Наступні треки", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onHide, modifier = Modifier.offset(x = 12.dp)) { Icon(Icons.Default.Close, null, tint = Color.White) }
        }
    }
}

@Composable
private fun QueueTrackItem(track: TrackDto, bandName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)) {
            if (track.coverUrl != null) AsyncImage(model = track.coverUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Icon(Icons.Default.MusicNote, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.Center))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = bandName, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Default.DragHandle, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
private fun PlayerTrackOptionsSheet(
    track: TrackDto,
    playlists: List<PlaylistDto>,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: (String) -> Unit
) {
    var isSelectingPlaylist by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        if (!isSelectingPlaylist) {
            Text(text = track.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            PlayerSheetOptionRow(Icons.AutoMirrored.Filled.QueueMusic, "Додати в чергу") {
                onAddToQueue()
            }
            PlayerSheetOptionRow(Icons.AutoMirrored.Filled.PlaylistAdd, "Додати в плейліст") {
                isSelectingPlaylist = true
            }
        } else {
            Text(text = "Оберіть плейліст", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            if (playlists.isEmpty()) {
                Text("У вас ще немає плейлістів.", color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(playlists) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddToPlaylist(playlist.id) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(playlist.name, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            TextButton(onClick = { isSelectingPlaylist = false }) {
                Text("Назад", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun PlayerSheetOptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}

fun formatTime(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "$min:${sec.toString().padStart(2, '0')}"
}
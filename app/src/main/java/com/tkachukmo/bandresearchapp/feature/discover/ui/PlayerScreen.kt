package com.tkachukmo.bandresearchapp.feature.discover.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil3.compose.AsyncImage
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

    LaunchedEffect(trackId) {
        viewModel.loadTrack(trackId)
        viewModel.checkIfTrackIsLiked(trackId)
    }

    if (track == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    val embeddedArtworkBitmap = remember(extractedArtwork) {
        extractedArtwork?.let { bytes ->
            BitmapFactory.decodeByteArray(
                bytes,
                0,
                bytes.size
            )?.asImageBitmap()
        }
    }

    val displayTitle = extractedTitle ?: track!!.title
    val displayArtist = bandName

    // Стан видимості черги
    var isQueueVisible by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // --- ШАР 1: ОСНОВНИЙ ПЛЕЄР ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = isQueueVisible) {
                    isQueueVisible = false
                }
                .graphicsLayer {
                    scaleX = if (isQueueVisible) 0.92f else 1f
                    scaleY = if (isQueueVisible) 0.92f else 1f
                    clip = true
                    shape = RoundedCornerShape(
                        if (isQueueVisible) 24.dp.toPx() else 0f
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()

                        if (dragAmount < -20f && !isQueueVisible) {
                            isQueueVisible = true
                        }

                        if (dragAmount > 20f && !isQueueVisible) {
                            onNavigateBack()
                        }
                    }
                }
        ) {

            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                // Background
                if (track!!.coverUrl != null) {

                    AsyncImage(
                        model = track!!.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(radius = 80.dp)
                    )

                } else if (embeddedArtworkBitmap != null) {

                    Image(
                        bitmap = embeddedArtworkBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(radius = 80.dp)
                    )

                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF2A2A35),
                                        Color(0xFF121218)
                                    )
                                )
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.5f)
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .systemBarsPadding(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    // Top bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = onNavigateBack
                        ) {

                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Згорнути",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "Зараз грає",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        IconButton(
                            onClick = {
                                isQueueVisible = true
                            }
                        ) {

                            Icon(
                                Icons.Default.QueueMusic,
                                contentDescription = "Черга",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    PlayerArtwork(
                        track = track!!,
                        embeddedBitmap = embeddedArtworkBitmap,
                        isPlaying = isPlaying,
                        viewModel = viewModel
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    TrackInfoRow(
                        title = displayTitle,
                        artist = displayArtist,
                        isLiked = isLiked,
                        onLikeClick = {
                            viewModel.toggleLikeTrack(track!!.id)
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    PlayerSlider(
                        progress = progress,
                        duration = duration,
                        onSeek = {
                            viewModel.seekTo(it)
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    PlayerControls(
                        isPlaying = isPlaying,
                        shuffleMode = shuffleMode,
                        repeatMode = repeatMode,
                        viewModel = viewModel
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // --- ШАР 2: ТЕМНИЙ ОВЕРЛЕЙ ---
        AnimatedVisibility(
            visible = isQueueVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.6f)
                    )
            )
        }

        // --- ШАР 3: КАСТОМНА ШТОРКА ---
        AnimatedVisibility(
            visible = isQueueVisible,
            enter = slideInVertically(
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                targetOffsetY = { it }
            ),
            modifier = Modifier.align(
                Alignment.BottomCenter
            )
        ) {

            var dragOffset by remember {
                mutableFloatStateOf(0f)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .offset {
                        IntOffset(
                            0,
                            dragOffset.roundToInt()
                        )
                    }
                    .clip(
                        RoundedCornerShape(
                            topStart = 32.dp,
                            topEnd = 32.dp
                        )
                    )
                    .background(Color(0xFF18181C))
                    .pointerInput(Unit) {

                        detectVerticalDragGestures(

                            onDragEnd = {

                                if (dragOffset > 200f) {
                                    isQueueVisible = false
                                }

                                dragOffset = 0f
                            }

                        ) { change, dragAmount ->

                            change.consume()

                            if (dragAmount > 0) {
                                dragOffset += dragAmount
                            }
                        }
                    }
            ) {

                QueueHeader(
                    onHide = {
                        isQueueVisible = false
                    }
                )

                if (upcomingTracks.isEmpty()) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "Черга порожня",
                            color = Color.Gray,
                            modifier = Modifier.padding(24.dp)
                        )
                    }

                } else {

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        items(upcomingTracks) { nextTrack ->

                            QueueTrackItem(
                                track = nextTrack,
                                bandName = bandName
                            )
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// COMPONENTS
// =====================================================

@Composable
fun PlayerArtwork(
    track: TrackDto,
    embeddedBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    isPlaying: Boolean,
    viewModel: PlayerViewModel
) {

    var dragOffsetX by remember {
        mutableFloatStateOf(0f)
    }

    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        label = "offsetX"
    )

    val artScale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.85f,
        animationSpec = tween(durationMillis = 500),
        label = "artScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(artScale)
            .offset {
                IntOffset(
                    animatedOffsetX.roundToInt(),
                    0
                )
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                Color.DarkGray.copy(alpha = 0.5f)
            )
            .pointerInput(track.id) {

                detectDragGestures(

                    onDragEnd = {

                        if (dragOffsetX < -150f) {
                            viewModel.skipToNext()
                        } else if (dragOffsetX > 150f) {
                            viewModel.skipToPrevious()
                        }

                        dragOffsetX = 0f
                    }

                ) { change, dragAmount ->

                    change.consume()

                    dragOffsetX += dragAmount.x
                }
            },
        contentAlignment = Alignment.Center
    ) {

        if (track.coverUrl != null) {

            AsyncImage(
                model = track.coverUrl,
                contentDescription = "Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

        } else if (embeddedBitmap != null) {

            Image(
                bitmap = embeddedBitmap,
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
}

@Composable
fun TrackInfoRow(
    title: String,
    artist: String,
    isLiked: Boolean,
    onLikeClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = artist,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        IconButton(
            onClick = onLikeClick
        ) {

            Icon(
                imageVector = if (isLiked) {
                    Icons.Default.Favorite
                } else {
                    Icons.Default.FavoriteBorder
                },
                contentDescription = "Like",
                tint = if (isLiked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.White
                },
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun PlayerSlider(
    progress: Float,
    duration: Float,
    onSeek: (Float) -> Unit
) {

    val sliderRange =
        0f..(
                duration.takeIf { it > 0f } ?: 100f
                )

    Slider(
        value = progress,
        onValueChange = onSeek,
        valueRange = sliderRange,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = Color.White,
            inactiveTrackColor =
                Color.White.copy(alpha = 0.3f)
        )
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = formatTime(progress.toInt()),
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium
        )

        Text(
            text = formatTime(duration.toInt()),
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    shuffleMode: Boolean,
    repeatMode: Int,
    viewModel: PlayerViewModel
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceEvenly,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        IconButton(
            onClick = {
                viewModel.toggleShuffle()
            }
        ) {

            Icon(
                Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleMode) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.White.copy(alpha = 0.5f)
                }
            )
        }

        IconButton(
            onClick = {
                viewModel.skipToPrevious()
            }
        ) {

            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        FilledIconButton(
            onClick = {
                viewModel.togglePlayPause()
            },
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            colors = IconButtonDefaults
                .filledIconButtonColors(
                    containerColor =
                        MaterialTheme.colorScheme.primary
                )
        ) {

            Icon(
                imageVector = if (isPlaying) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = "Play",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }

        IconButton(
            onClick = {
                viewModel.skipToNext()
            }
        ) {

            Icon(
                Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        IconButton(
            onClick = {
                viewModel.toggleRepeat()
            }
        ) {

            val repeatTint =
                if (repeatMode == Player.REPEAT_MODE_OFF) {
                    Color.White.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.primary
                }

            Icon(
                Icons.Default.Repeat,
                contentDescription = "Repeat",
                tint = repeatTint
            )
        }
    }
}

@Composable
fun QueueHeader(
    onHide: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 12.dp,
                start = 24.dp,
                end = 24.dp,
                bottom = 12.dp
            )
    ) {

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Canvas(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
            ) {

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.2f),
                    size = Size(
                        40.dp.toPx(),
                        4.dp.toPx()
                    ),
                    cornerRadius = CornerRadius(
                        2.dp.toPx()
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = "Наступні треки",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onHide,
                modifier = Modifier.offset(x = 12.dp)
            ) {

                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun QueueTrackItem(
    track: TrackDto,
    bandName: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Color.White.copy(alpha = 0.05f)
            )
            .padding(12.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.DarkGray)
        ) {

            if (track.coverUrl != null) {

                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

            } else {

                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(
            modifier = Modifier.width(16.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = track.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = bandName,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            Icons.Default.DragHandle,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

fun formatTime(seconds: Int): String {

    val min = seconds / 60
    val sec = seconds % 60

    return "$min:${sec.toString().padStart(2, '0')}"
}
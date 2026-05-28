package com.tkachukmo.bandresearchapp.feature.discover.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.feature.discover.viewmodel.MiniPlayerViewModel
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(
    onNavigateToPlayer: (String) -> Unit,
    viewModel: MiniPlayerViewModel = hiltViewModel()
) {

    val currentTrack by viewModel.audioController.currentTrack.collectAsState()
    val isPlaying by viewModel.audioController.isPlaying.collectAsState()
    val extractedTitle by viewModel.audioController.extractedTitle.collectAsState()

    var isDismissed by remember { mutableStateOf(false) }

    // Р—Р°РїР°Рј'СЏС‚РѕРІСѓС”РјРѕ РѕСЃС‚Р°РЅРЅС–Р№ С‚СЂРµРє,
    // С‰РѕР± СѓРЅРёРєРЅСѓС‚Рё РєСЂР°С€Сѓ РїС–Рґ С‡Р°СЃ Р°РЅС–РјР°С†С–С— Р·РЅРёРєРЅРµРЅРЅСЏ
    var rememberedTrack by remember {
        mutableStateOf<TrackDto?>(null)
    }

    LaunchedEffect(currentTrack) {
        if (currentTrack != null) {
            rememberedTrack = currentTrack
            isDismissed = false
        }
    }

    AnimatedVisibility(
        visible = currentTrack != null && !isDismissed,
        enter = slideInVertically(
            initialOffsetY = { it }
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it }
        ) + fadeOut()
    ) {

        // Р‘РµР·РїРµС‡РЅРёР№ С‚СЂРµРє
        val track = rememberedTrack ?: return@AnimatedVisibility

        val displayTitle = extractedTitle ?: track.title

        var dragOffsetX by remember {
            mutableFloatStateOf(0f)
        }

        var dragOffsetY by remember {
            mutableFloatStateOf(0f)
        }

        val animatedOffsetX by animateFloatAsState(
            targetValue = dragOffsetX,
            label = "offsetX"
        )

        val animatedOffsetY by animateFloatAsState(
            targetValue = dragOffsetY,
            label = "offsetY"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
                .height(64.dp)
                .offset {
                    IntOffset(
                        animatedOffsetX.roundToInt(),
                        animatedOffsetY.roundToInt()
                    )
                }
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(track.id) {

                    detectDragGestures(

                        onDragEnd = {

                            val absX = kotlin.math.abs(dragOffsetX)
                            val absY = kotlin.math.abs(dragOffsetY)

                            if (absX > absY) {

                                // РЎРІР°Р№РїРё РІР»С–РІРѕ / РІРїСЂР°РІРѕ
                                if (dragOffsetX < -100f) {
                                    viewModel.audioController.skipToPrevious()
                                } else if (dragOffsetX > 100f) {
                                    viewModel.audioController.skipToNext()
                                }

                            } else {

                                // РЎРІР°Р№РїРё РІРІРµСЂС… / РІРЅРёР·
                                if (dragOffsetY < -50f) {

                                    // Р’С–РґРєСЂРёС‚Рё РїРѕРІРЅРёР№ РїР»РµС”СЂ
                                    onNavigateToPlayer(track.id)

                                } else if (dragOffsetY > 100f) {

                                    // РџСЂРёС…РѕРІР°С‚Рё mini player
                                    isDismissed = true

                                    // Р”Р°С”РјРѕ Р°РЅС–РјР°С†С–С— Р·Р°РІРµСЂС€РёС‚РёСЃСЊ
                                    // РїРµСЂРµРґ РѕС‡РёС‰РµРЅРЅСЏРј MediaItems
                                    viewModel.audioController.stopAndClear()
                                }
                            }

                            // РџРѕРІРµСЂС‚Р°С”РјРѕ РЅР°Р·Р°Рґ
                            dragOffsetX = 0f
                            dragOffsetY = 0f
                        }

                    ) { change, dragAmount ->

                        change.consume()

                        dragOffsetX += dragAmount.x
                        dragOffsetY += dragAmount.y
                    }
                }
                .clickable {
                    onNavigateToPlayer(track.id)
                }
        ) {

            // Blur background
            if (track.coverUrl != null) {

                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = "MiniPlayer Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(24.dp)
                )

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                )
            }

            // Dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.5f)
                    )
            )

            // Content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Cover
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {

                    if (track.coverUrl != null) {

                        AsyncImage(
                            model = track.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                    } else {

                        Text(
                            text = "♪",
                            color = Color.White
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                // Title
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Previous
                IconButton(
                    onClick = {
                        viewModel.audioController.skipToPrevious()
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Попередній",
                        tint = Color.White
                    )
                }

                // Play / Pause
                IconButton(
                    onClick = {
                        viewModel.audioController.playPause()
                    }
                ) {

                    Icon(
                        imageVector = if (isPlaying) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }

                // Next
                IconButton(
                    onClick = {
                        viewModel.audioController.skipToNext()
                    }
                ) {

                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Наступний",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

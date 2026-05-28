package com.tkachukmo.bandresearchapp.feature.discover.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import kotlin.math.abs
import com.tkachukmo.bandresearchapp.feature.discover.viewmodel.DiscoverViewModel

data class BandCard(
    val id: String,
    val name: String,
    val genre: String,
    val country: String,
    val emoji: String,
    val color: Color,
    val tags: List<String>
)
fun BandDto.toBandCard(): BandCard {
    val defaultColors = listOf(
        Color(0xFF6750A4), Color(0xFF006064), Color(0xFF880E4F), Color(0xFFE65100), Color(0xFF1B5E20)
    )
    val colorIndex = this.id.hashCode().let { if (it == Int.MIN_VALUE) 0 else abs(it) } % defaultColors.size

    return BandCard(
        id = this.id,
        name = this.name,
        genre = this.genres.firstOrNull() ?: "Різне",
        country = this.country ?: "Невідомо",
        emoji = "🎸", // Поки залишаємо статичне емоджі для карток свайпу
        color = defaultColors[colorIndex],
        tags = this.genres
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    modifier: Modifier = Modifier,
    onBandClick: (String) -> Unit = {},
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val bandsDto by viewModel.recommendedBands.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val cards = remember(bandsDto) { bandsDto.map { it.toBandCard() } }

    // ДОДАНО: Стан для Snackbar (повідомлення про інтернет)
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

    // ДОДАНО: Обернули все в Scaffold для правильного відображення Snackbar
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->

        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (cards.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎸", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нових рекомендацій поки немає",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val currentBand = cards.first()
                var offsetX by remember { mutableFloatStateOf(0f) }
                val rotate by animateFloatAsState(targetValue = offsetX / 20f, animationSpec = tween(150), label = "card_rotation")

                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = offsetX.dp)
                            .rotate(rotate)
                            .pointerInput(currentBand.id) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (offsetX > 150) viewModel.onBandSwiped(currentBand.id)
                                        else if (offsetX < -150) viewModel.onBandSwiped(currentBand.id)
                                        offsetX = 0f
                                    }
                                ) { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount
                                }
                            }
                            .clickable { onBandClick(currentBand.id) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = currentBand.color)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(currentBand.emoji, fontSize = 120.sp, modifier = Modifier.align(Alignment.Center))

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(24.dp)
                            ) {
                                Text(currentBand.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${currentBand.genre} · ${currentBand.country}", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    currentBand.tags.take(3).forEach { tag ->
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = tag, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            if (offsetX > 60) {
                                Text("ТАК", color = Color.Green, fontSize = 40.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopStart).padding(32.dp).rotate(-15f))
                            } else if (offsetX < -60) {
                                Text("НІ", color = Color.Red, fontSize = 40.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.TopEnd).padding(32.dp).rotate(15f))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { viewModel.onBandSwiped(currentBand.id) },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Close, "Пропустити", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                    }

                    FilledIconButton(
                        onClick = { viewModel.onBandSwiped(currentBand.id) },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Favorite, "Подобається", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}
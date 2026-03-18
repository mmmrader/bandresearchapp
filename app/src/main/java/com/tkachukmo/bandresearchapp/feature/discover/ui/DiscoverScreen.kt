package com.tkachukmo.bandresearchapp.feature.discover.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class BandCard(
    val id: String,
    val name: String,
    val genre: String,
    val country: String,
    val emoji: String,
    val color: Color,
    val tags: List<String>
)

val sampleBands = listOf(
    BandCard("1", "Океан Ельзи", "Рок · Поп-рок", "Україна", "🎸",
        Color(0xFF4A0080), listOf("Рок", "Поп-рок", "Альтернатива")),
    BandCard("2", "Kazka", "Поп", "Україна", "🎹",
        Color(0xFF006064), listOf("Поп", "Електронна")),
    BandCard("3", "DakhaBrakha", "Авант-фолк", "Україна", "🥁",
        Color(0xFF880E4F), listOf("Фолк", "Авант-гард")),
    BandCard("4", "Haydamaky", "Рок-фолк", "Україна", "🎺",
        Color(0xFFE65100), listOf("Рок", "Фолк")),
    BandCard("5", "Brutto", "Рок", "Україна", "🎸",
        Color(0xFF1B5E20), listOf("Рок", "Панк")),
    BandCard("6", "Neon Waves", "Синтпоп", "Польща", "🎹",
        Color(0xFF880E4F), listOf("Синтпоп", "Електронна")),
)

@Composable
fun DiscoverScreen(
    modifier: Modifier = Modifier,
    onBandClick: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var offsetX by remember { mutableFloatStateOf(0f) }

    val rotation by animateFloatAsState(
        targetValue = offsetX * 0.05f,
        animationSpec = tween(100),
        label = "rotation"
    )

    val likeAlpha = if (offsetX > 40f) ((offsetX - 40f) / 60f).coerceIn(0f, 1f) else 0f
    val nopeAlpha = if (offsetX < -40f) ((-offsetX - 40f) / 60f).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Swipe Card Stack
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Картка позаду
            if (currentIndex + 1 < sampleBands.size) {
                val backBand = sampleBands[(currentIndex + 1) % sampleBands.size]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backBand.color
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {}
            }

            // Верхня картка (активна)
            if (currentIndex < sampleBands.size) {
                val band = sampleBands[currentIndex % sampleBands.size]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .rotate(rotation)
                        .pointerInput(currentIndex) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    when {
                                        offsetX > 120f -> {
                                            currentIndex =
                                                (currentIndex + 1) % sampleBands.size
                                            offsetX = 0f
                                        }
                                        offsetX < -120f -> {
                                            currentIndex =
                                                (currentIndex + 1) % sampleBands.size
                                            offsetX = 0f
                                        }
                                        else -> offsetX = 0f
                                    }
                                },
                                onDragCancel = { offsetX = 0f },
                                onHorizontalDrag = { _, dragAmount ->
                                    offsetX += dragAmount
                                }
                            )
                        },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = band.color
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = band.emoji,
                                fontSize = 96.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Column {
                                Text(
                                    text = band.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${band.genre} · ${band.country}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    band.tags.forEach { tag ->
                                        androidx.compose.material3.SuggestionChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    text = tag,
                                                    color = Color.White,
                                                    fontSize = 12.sp
                                                )
                                            },
                                            colors = androidx.compose.material3
                                                .SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = Color.White.copy(alpha = 0.2f)
                                                ),
                                            border = null
                                        )
                                    }
                                }
                            }
                        }

                        // LIKE індикатор
                        Text(
                            text = "💜 ТАК",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(24.dp)
                                .alpha(likeAlpha)
                        )

                        // NOPE індикатор
                        Text(
                            text = "✖ НІ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(24.dp)
                                .alpha(nopeAlpha)
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "🎵", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ти переглянув всі гурти!",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Повертайся пізніше",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки дій
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = {
                    currentIndex = (currentIndex + 1) % sampleBands.size
                    offsetX = 0f
                },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Пропустити",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }

            FilledIconButton(
                onClick = {},
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Супер лайк",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }

            FilledIconButton(
                onClick = {
                    currentIndex = (currentIndex + 1) % sampleBands.size
                    offsetX = 0f
                },
                modifier = Modifier.size(64.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Подобається",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
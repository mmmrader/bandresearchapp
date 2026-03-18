package com.tkachukmo.bandresearchapp.feature.catalog.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tkachukmo.bandresearchapp.feature.discover.ui.BandCard
import com.tkachukmo.bandresearchapp.feature.discover.ui.sampleBands

// Тестові дані треків
data class TrackItem(
    val id: String,
    val number: Int,
    val title: String,
    val duration: String,
    val isPlaying: Boolean = false
)

data class ReleaseItem(
    val id: String,
    val title: String,
    val year: String,
    val type: String,
    val tracksCount: Int,
    val emoji: String
)

data class VideoItem(
    val id: String,
    val title: String,
    val views: String,
    val duration: String,
    val emoji: String
)

val sampleTracks = listOf(
    TrackItem("1", 1, "Така як ти", "4:12"),
    TrackItem("2", 2, "Вона", "3:58"),
    TrackItem("3", 3, "Без бою", "4:35"),
    TrackItem("4", 4, "Друг", "3:47"),
    TrackItem("5", 5, "Мить", "5:02"),
    TrackItem("6", 6, "Холодно", "4:18"),
    TrackItem("7", 7, "Серце", "3:55"),
    TrackItem("8", 8, "Рідна", "4:28"),
)

val sampleReleases = listOf(
    ReleaseItem("1", "Земля", "2023", "Альбом", 12, "🎸"),
    ReleaseItem("2", "Така як ти", "2022", "Сингл", 1, "🎵"),
    ReleaseItem("3", "Без бою", "2021", "EP", 5, "🎸"),
    ReleaseItem("4", "Dolce Vita", "2019", "Альбом", 14, "🎸"),
    ReleaseItem("5", "Кохання в кожному місті", "2017", "Альбом", 11, "🎸"),
)

val sampleVideos = listOf(
    VideoItem("1", "Така як ти — Official Video", "12M переглядів", "4:12", "🎬"),
    VideoItem("2", "Без бою — Live Concert", "5.4M переглядів", "5:35", "🎥"),
    VideoItem("3", "Вона — Acoustic Version", "3.2M переглядів", "4:02", "🎵"),
    VideoItem("4", "Друг — Official Video", "8.1M переглядів", "3:47", "🎬"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandDetailScreen(
    bandId: String = "1",
    onNavigateBack: () -> Unit = {},
    onPlayTrack: (TrackItem) -> Unit = {}
) {
    val band = sampleBands.find { it.id == bandId } ?: sampleBands.first()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isFollowing by remember { mutableStateOf(false) }

    val tabs = listOf("Треки", "Відео", "Дискографія", "Тексти")

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        // Hero секція
        item {
            BandHeroSection(
                band = band,
                isFollowing = isFollowing,
                onNavigateBack = onNavigateBack,
                onFollowClick = { isFollowing = !isFollowing }
            )
        }

        // Статистика
        item {
            BandStatsSection(band = band)
        }

        // Кнопки дій
        item {
            BandActionsSection(
                isFollowing = isFollowing,
                onFollowClick = { isFollowing = !isFollowing }
            )
        }

        // Вкладки
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
        }

        // Контент вкладок
        when (selectedTab) {
            0 -> {
                // Треки
                items(sampleTracks) { track ->
                    TrackRow(
                        track = track,
                        onClick = { onPlayTrack(track) }
                    )
                }
            }
            1 -> {
                // Відео
                items(sampleVideos) { video ->
                    VideoRow(video = video)
                }
            }
            2 -> {
                // Дискографія
                items(sampleReleases) { release ->
                    ReleaseRow(release = release)
                }
            }
            3 -> {
                // Тексти
                item {
                    LyricsSection()
                }
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun BandHeroSection(
    band: BandCard,
    isFollowing: Boolean,
    onNavigateBack: () -> Unit,
    onFollowClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // Фон
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(band.color)
        ) {
            Text(
                text = band.emoji,
                fontSize = 120.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Градієнт знизу
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        // Кнопка назад
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White
            )
        }

        // Кнопки зверху справа
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Поділитись",
                    tint = Color.White
                )
            }
        }

        // Назва гурту знизу
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = band.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = band.genre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BandStatsSection(band: BandCard) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        BandStatItem(value = "2.4M", label = "Підписників")
        BandStatItem(value = "142M", label = "Прослухань")
        BandStatItem(value = "${band.tags.size}", label = "Жанри")
        BandStatItem(value = "1994", label = "З року")
    }
}

@Composable
fun BandStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BandActionsSection(
    isFollowing: Boolean,
    onFollowClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Кнопка підписки
        Button(
            onClick = onFollowClick,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFollowing)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                if (isFollowing) Icons.Default.Favorite
                else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isFollowing)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isFollowing) "Стежите" else "Стежити",
                color = if (isFollowing)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onPrimary
            )
        }

        // Кнопка відтворити
        OutlinedButton(
            onClick = {},
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Слухати")
        }

        // Більше
        OutlinedButton(onClick = {}) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Більше",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun TrackRow(
    track: TrackItem,
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
        // Номер або іконка
        Box(
            modifier = Modifier.width(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${track.number}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Обкладинка
        Card(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Назва і артист
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Тривалість
        Text(
            text = track.duration,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Більше
        IconButton(
            onClick = {},
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Більше",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VideoRow(video: VideoItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable {},
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Прев'ю відео
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = video.emoji, fontSize = 32.sp)
                // Кнопка play
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Тривалість
                Text(
                    text = video.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Інфо
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.views,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Більше",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReleaseRow(release: ReleaseItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Обкладинка
        Card(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = release.emoji, fontSize = 26.sp)
            }
        }

        // Інфо
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = release.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${release.year} · ${release.type} · ${release.tracksCount} треків",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            Icons.Default.MoreVert,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun LyricsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Така як ти",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Океан Ельзи · 2022",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        val lyrics = """
Ти така як ти
Неповторна і єдина
Ти така як ти
І мені без тебе погано

Я шукав тебе скрізь
По далеких дорогах
Я шукав тебе скрізь
І знайшов тебе вдома

Ти моя зоря
Ти моя весна
Ти моє все
Ти така як ти
        """.trimIndent()

        Text(
            text = lyrics,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )
    }
}
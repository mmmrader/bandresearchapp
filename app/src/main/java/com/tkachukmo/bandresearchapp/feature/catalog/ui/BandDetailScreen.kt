package com.tkachukmo.bandresearchapp.feature.catalog.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.data.remote.dto.VideoDto
import com.tkachukmo.bandresearchapp.feature.catalog.viewmodel.BandDetailViewModel
import com.tkachukmo.bandresearchapp.feature.discover.ui.MiniPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandDetailScreen(
    bandId: String,
    onNavigateBack: () -> Unit,
    onPlayTrack: (TrackDto) -> Unit,
    onNavigateToTab: (Int) -> Unit = {},
    viewModel: BandDetailViewModel = hiltViewModel()
) {
    val band by viewModel.band.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedTrackForOptions by remember { mutableStateOf<TrackDto?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(bandId) { viewModel.loadBandDetails(bandId) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                MiniPlayer(onNavigateToPlayer = { trackId ->
                    val track = tracks.find { it.id == trackId }
                    if (track != null) onPlayTrack(track)
                })

                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = { onNavigateToTab(0) },
                        icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Каталог") },
                        label = { Text("Каталог") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onNavigateToTab(1) },
                        icon = { Icon(Icons.Outlined.Search, contentDescription = "Пошук") },
                        label = { Text("Пошук") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onNavigateToTab(2) },
                        icon = { Icon(Icons.Outlined.Event, contentDescription = "Події") },
                        label = { Text("Події") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { onNavigateToTab(3) },
                        icon = { Icon(Icons.Outlined.Person, contentDescription = "Профіль") },
                        label = { Text("Профіль") }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (band != null) {
            val currentBand = band!!

            Box(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {

                BandDetailContent(
                    band = currentBand,
                    tracks = tracks,
                    videos = videos,
                    isFollowing = isFollowing, // ДОДАНО
                    onToggleFollow = { viewModel.toggleFollow() }, // ДОДАНО
                    onPlayTrack = onPlayTrack,
                    onOptionsClick = { track ->
                        selectedTrackForOptions = track
                        showBottomSheet = true
                    }
                )

                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp)
                        .statusBarsPadding()
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                }

                if (showBottomSheet && selectedTrackForOptions != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        TrackOptionsSheet(
                            track = selectedTrackForOptions!!,
                            fallbackUrl = currentBand.avatarUrl ?: currentBand.coverUrl,
                            bandName = currentBand.name,
                            onClose = { showBottomSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BandDetailContent(
    band: BandDto,
    tracks: List<TrackDto>,
    videos: List<VideoDto>,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    onPlayTrack: (TrackDto) -> Unit,
    onOptionsClick: (TrackDto) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Треки", "Релізи", "Відео", "Про гурт")
    val context = LocalContext.current

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                if (band.coverUrl != null) {
                    AsyncImage(model = band.coverUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF6750A4), Color(0xFF21005D)))))
                }
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))))

                Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text(text = band.name, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text(text = "${band.followersCount} підписників", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                // ВАЖЛИВО: ОНОВЛЕНА КНОПКА
                if (isFollowing) {
                    Button(onClick = onToggleFollow, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                        Text("Відписатись", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(onClick = onToggleFollow) {
                        Text("Підписатись", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                FloatingActionButton(onClick = { if (tracks.isNotEmpty()) onPlayTrack(tracks.first()) }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }
        }

        item {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp, containerColor = Color.Transparent, divider = {}) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
        }

        when (selectedTab) {
            0 -> items(tracks) { track ->
                TrackItemRow(track, band.avatarUrl ?: band.coverUrl, { onPlayTrack(track) }, { onOptionsClick(track) }, band.name)
            }

            1 -> item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Розділ у розробці", color = MaterialTheme.colorScheme.onSurfaceVariant) } }

            2 -> {
                if (videos.isEmpty()) {
                    item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("Гурт ще не додав відео", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                } else {
                    items(videos) { video ->
                        PublicVideoItem(video = video, onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${video.youtubeId}"))
                            context.startActivity(intent)
                        })
                    }
                }
            }

            3 -> item {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("Про виконавця", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!band.description.isNullOrBlank()) {
                        Text(text = band.description, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                    } else {
                        Text("Інформація відсутня.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    band.genres.takeIf { it.isNotEmpty() }?.let {
                        Text("Жанри: ${it.joinToString(", ")}", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    band.country?.let {
                        Text("Країна: $it", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun PublicVideoItem(video: VideoDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(120.dp).height(68.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black), contentAlignment = Alignment.Center) {
                if (video.thumbnailUrl != null) {
                    AsyncImage(model = video.thumbnailUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Icon(Icons.Default.PlayCircle, contentDescription = "Грати", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(32.dp))
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(text = video.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(text = "YouTube", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun TrackItemRow(track: TrackDto, fallbackUrl: String?, onPlay: () -> Unit, onOptionsClick: () -> Unit, bandName: String) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onPlay() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            AsyncImage(model = track.coverUrl ?: fallbackUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
            Text(text = track.title, fontWeight = FontWeight.Medium)
            Text(text = "$bandName • ${track.playsCount} прослуховувань", style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onOptionsClick) { Icon(Icons.Default.MoreVert, null) }
    }
}

@Composable
fun TrackOptionsSheet(track: TrackDto, fallbackUrl: String?, bandName: String, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = track.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        SheetOptionRow(Icons.Default.PlaylistPlay, "Відтворити наступним", onClose)
        SheetOptionRow(Icons.Default.QueueMusic, "Додати в чергу", onClose)
        SheetOptionRow(Icons.Default.Share, "Поділитися", onClose)
    }
}

@Composable
fun SheetOptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
    }
}
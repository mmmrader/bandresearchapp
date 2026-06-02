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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.tkachukmo.bandresearchapp.data.remote.dto.ApplicationDto
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
import com.tkachukmo.bandresearchapp.data.remote.dto.BandEventDto
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ReleaseDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.data.remote.dto.VacancyDto
import com.tkachukmo.bandresearchapp.data.remote.dto.VideoDto
import com.tkachukmo.bandresearchapp.feature.catalog.viewmodel.BandDetailViewModel
import com.tkachukmo.bandresearchapp.feature.discover.ui.MiniPlayer
import androidx.core.net.toUri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandDetailScreen(
    bandId: String,
    onNavigateBack: () -> Unit,
    onPlayTrack: (TrackDto) -> Unit,
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToChat: (partnerId: String, chatName: String) -> Unit = { _, _ -> },
    viewModel: BandDetailViewModel = hiltViewModel()
) {
    val band by viewModel.band.collectAsState()
    val userApplications by viewModel.userApplications.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val releases by viewModel.releases.collectAsState()
    val events by viewModel.events.collectAsState()
    val vacancies by viewModel.vacancies.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var showBottomSheet by remember {
        mutableStateOf(false)
    }

    var selectedTrackForOptions by remember {
        mutableStateOf<TrackDto?>(null)
    }

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    LaunchedEffect(bandId) {
        viewModel.loadBandDetails(bandId)
    }

    LaunchedEffect(errorMessage) {

        if (errorMessage != null) {

            snackbarHostState.showSnackbar(
                errorMessage!!,
                duration = SnackbarDuration.Short
            )

            viewModel.clearMessages()
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            snackbarHostState.showSnackbar(
                successMessage!!,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },

        bottomBar = {

            Column {

                MiniPlayer(
                    onNavigateToPlayer = onNavigateToPlayer
                )

                NavigationBar {

                    NavigationBarItem(
                        selected = true,
                        onClick = {
                            onNavigateToTab(0)
                        },
                        icon = {
                            Icon(
                                Icons.Filled.LibraryMusic,
                                contentDescription = "Каталог"
                            )
                        },
                        label = {
                            Text("Каталог")
                        }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            onNavigateToTab(1)
                        },
                        icon = {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = "Пошук"
                            )
                        },
                        label = {
                            Text("Пошук")
                        }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            onNavigateToTab(2)
                        },
                        icon = {
                            Icon(
                                Icons.Outlined.Event,
                                contentDescription = "Події"
                            )
                        },
                        label = {
                            Text("Події")
                        }
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            onNavigateToTab(3)
                        },
                        icon = {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Профіль"
                            )
                        },
                        label = {
                            Text("Профіль")
                        }
                    )
                }
            }
        }
    ) { paddingValues ->

        if (isLoading) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),

                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else if (band != null) {

            val currentBand = band!!

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom =
                            paddingValues.calculateBottomPadding()
                    )
            ) {

                BandDetailContent(
                    band = currentBand,
                    tracks = tracks,
                    releases = releases,
                    videos = videos,
                    events = events,
                    vacancies = vacancies,
                    userApplications = userApplications,
                    isFollowing = isFollowing,
                    onToggleFollow = { viewModel.toggleFollow() },
                    onPlayTrack = onPlayTrack,
                    onOptionsClick = { track ->
                        selectedTrackForOptions = track
                        viewModel.loadUserPlaylists()
                        showBottomSheet = true
                    },
                    onApplyForVacancy = { vacancy, message ->
                        viewModel.applyForVacancy(vacancy.id, message)
                    },
                    onNavigateToChat = { partnerId, chatName ->
                        onNavigateToChat(partnerId, chatName)
                    }
                )

                IconButton(
                    onClick = onNavigateBack,

                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp)
                        .statusBarsPadding()
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }

                if (
                    showBottomSheet &&
                    selectedTrackForOptions != null
                ) {

                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                        },

                        sheetState = sheetState,

                        containerColor =
                            MaterialTheme.colorScheme.surface
                    ) {

                        TrackOptionsSheet(
                            track =
                                selectedTrackForOptions!!,

                            playlists = playlists,

                            bandName =
                                currentBand.name,

                            onClose = {
                                showBottomSheet = false
                            },

                            onAddToQueue = {

                                viewModel.audioController
                                    .addTrackToQueue(
                                        selectedTrackForOptions!!,
                                        currentBand.name
                                    )

                                viewModel.clearMessages()
                            },

                            onAddToPlaylist = { playlistId ->

                                viewModel.addTrackToPlaylist(
                                    playlistId,
                                    selectedTrackForOptions!!.id
                                )
                            }
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
    releases: List<ReleaseDto>,
    videos: List<VideoDto>,
    events: List<BandEventDto>,
    vacancies: List<VacancyDto>,
    userApplications: List<ApplicationDto>,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    onPlayTrack: (TrackDto) -> Unit,
    onOptionsClick: (TrackDto) -> Unit,
    onApplyForVacancy: (VacancyDto, String) -> Unit,
    onNavigateToChat: (partnerId: String, chatName: String) -> Unit = { _, _ -> },
) {

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    val tabs = listOf(
        "Треки",
        "Релізи",
        "Відео",
        "Події",
        "Вакансії",
        "Про гурт"
    )

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            ) {

                if (band.coverUrl != null) {

                    AsyncImage(
                        model = band.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF6750A4),
                                        Color(0xFF21005D)
                                    )
                                )
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {

                    Text(
                        text = band.name,
                        style =
                            MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    val totalPlays = tracks.sumOf { it.playsCount }
                    Text(
                        text =
                            "${band.followersCount} підписників",

                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                if (isFollowing) {

                    Button(
                        onClick = onToggleFollow,

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    MaterialTheme.colorScheme.secondaryContainer,

                                contentColor =
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                    ) {

                        Text(
                            "Відписатися",
                            fontWeight = FontWeight.Bold
                        )
                    }

                } else {

                    OutlinedButton(
                        onClick = onToggleFollow
                    ) {

                        Text(
                            "Підписатися",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                FloatingActionButton(
                    onClick = {

                        if (tracks.isNotEmpty()) {
                            onPlayTrack(tracks.first())
                        }
                    }
                ) {

                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                }
            }
        }

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
                        onClick = {
                            selectedTab = index
                        },
                        text = {
                            Text(title)
                        }
                    )
                }
            }
        }

        when (selectedTab) {

            0 -> {

                items(tracks) { track ->

                    TrackItemRow(
                        track = track,
                        fallbackUrl =
                            band.avatarUrl ?: band.coverUrl,

                        onPlay = {
                            onPlayTrack(track)
                        },

                        onOptionsClick = {
                            onOptionsClick(track)
                        },

                        bandName = band.name
                    )
                }
            }

            1 -> {
                if (releases.isEmpty()) {
                    item {
                        EmptyBandSection("Гурт ще не додав альбомів, EP або синглів")
                    }
                } else {
                    items(releases) { release ->
                        PublicReleaseItem(release)
                    }
                }
            }

            2 -> {

                if (videos.isEmpty()) {

                    item {

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),

                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                "Гурт ще не додав відео",

                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                } else {

                    items(videos) { video ->

                        PublicVideoItem(
                            video = video,

                            onClick = {

                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "https://www.youtube.com/watch?v=${video.youtubeId}".toUri()
                                    )

                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            3 -> {
                if (events.isEmpty()) {
                    item {
                        EmptyBandSection("Гурт ще не публікував подій")
                    }
                } else {
                    items(events) { event ->
                        PublicBandEventCard(event)
                    }
                }
            }

            4 -> {
                if (vacancies.isEmpty()) {
                    item {
                        EmptyBandSection("Зараз відкритих вакансій немає")
                    }
                } else {
                    items(vacancies) { vacancy ->
                        val userApp = userApplications.find { it.vacancyId == vacancy.id }
                        PublicVacancyCard(
                            vacancy = vacancy,
                            userApplication = userApp,
                            bandManagerId = band.managerId,
                            bandName = band.name,
                            onNavigateToChat = onNavigateToChat,
                            onApply = { message -> onApplyForVacancy(vacancy, message) }
                        )
                    }
                }
            }
                5 -> {

                    item {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {

                            Text(
                                "Про виконавця",

                                style =
                                    MaterialTheme.typography.titleLarge,

                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            if (!band.description.isNullOrBlank()) {

                                Text(
                                    text = band.description,

                                    style =
                                        MaterialTheme.typography.bodyLarge,

                                    lineHeight = 24.sp
                                )

                            } else {

                                Text(
                                    "Інформація відсутня.",

                                    color =
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(24.dp)
                            )

                            band.genres
                                .takeIf { it.isNotEmpty() }
                                ?.let {

                                    Text(
                                        "Жанри: ${it.joinToString(", ")}",

                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(8.dp)
                                    )
                                }

                            band.country?.let {

                                Text(
                                    "Країна: $it",

                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }
        }
    }

    @Composable
    fun EmptyBandSection(text: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    @Composable
    fun PublicReleaseItem(release: ReleaseDto) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (release.coverUrl != null) {
                        AsyncImage(
                            model = release.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Album, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(start = 14.dp)
                        .weight(1f)
                ) {
                    Text(release.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        text = "${release.releaseType.uppercase()} • ${release.releaseYear ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    fun PublicBandEventCard(event: BandEventDto) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (event.type) {
                            "release" -> Icons.Default.LibraryMusic
                            "video" -> Icons.Default.PlayCircle
                            "concert", "tour" -> Icons.Default.Event
                            else -> Icons.Default.Campaign
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(event.title, fontWeight = FontWeight.Bold)
                        Text(
                            when (event.type) {
                                "release" -> "Реліз"
                                "video" -> "Кліп"
                                "concert" -> "Концерт"
                                "tour" -> "Тур"
                                else -> "Новина"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                event.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
                if (event.eventDate != null || event.venue != null || event.city != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        listOfNotNull(event.eventDate, event.venue, event.city).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    fun PublicVacancyCard(
        vacancy: VacancyDto,
        userApplication: ApplicationDto?,
        bandManagerId: String?,
        bandName: String = "гурт",
        onNavigateToChat: (partnerId: String, chatName: String) -> Unit = { _, _ -> },
        onApply: (String) -> Unit
    ) {
        var showApplyDialog by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf("") }

        // Діалог для подачі заявки
        if (showApplyDialog) {
            AlertDialog(
                onDismissRequest = { showApplyDialog = false },
                title = { Text("Відгук на вакансію") },
                text = {
                    OutlinedTextField(
                        message, { message = it },
                        label = { Text("Повідомлення гурту (необов'язково)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = { onApply(message); message = ""; showApplyDialog = false }) {
                        Text("Надіслати")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showApplyDialog = false }) { Text("Скасувати") }
                }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(vacancy.instrument, fontWeight = FontWeight.Bold)
                        Text(
                            vacancy.city ?: "Локація не вказана",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                vacancy.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    userApplication == null -> {
                        Button(onClick = { showApplyDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Відгукнутися")
                        }
                    }
                    userApplication.status == "pending" -> {
                        OutlinedButton(onClick = { }, enabled = false, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ви вже надіслали заявку")
                        }
                    }
                    userApplication.status == "accepted" -> {
                        // Заявку прийнято — кнопка відкриває повноцінний ChatDetailScreen
                        Button(
                            onClick = {
                                if (bandManagerId != null) {
                                    onNavigateToChat(bandManagerId, bandName)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Чат з гуртом")
                        }
                    }
                    userApplication.status == "rejected" -> {
                        OutlinedButton(onClick = { }, enabled = false, modifier = Modifier.fillMaxWidth()) {
                            Text("Заявку відхилено", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PublicVideoItem(
        video: VideoDto,
        onClick: () -> Unit
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
                .clickable {
                    onClick()
                },

            shape = RoundedCornerShape(12.dp)
        ) {

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(68.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),

                    contentAlignment = Alignment.Center
                ) {

                    if (video.thumbnailUrl != null) {
                        AsyncImage(
                            model = video.thumbnailUrl,
                            contentDescription = "Грати",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "Грати",

                        tint =
                            Color.White.copy(alpha = 0.8f),

                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {

                    Text(
                        text = video.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "YouTube",

                        style =
                            MaterialTheme.typography.bodySmall,

                        color =
                            MaterialTheme.colorScheme.primary,

                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun TrackItemRow(
        track: TrackDto,
        fallbackUrl: String?,
        onPlay: () -> Unit,
        onOptionsClick: () -> Unit,
        bandName: String
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onPlay()
                }
                .padding(16.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),

                contentAlignment = Alignment.Center
            ) {

                AsyncImage(
                    model = track.coverUrl ?: fallbackUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {

                Text(
                    text = track.title,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text =
                        "$bandName • ${track.playsCount} слухачів",

                    style =
                        MaterialTheme.typography.bodySmall
                )
            }

            IconButton(
                onClick = onOptionsClick
            ) {

                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null
                )
            }
        }
    }
    @Composable
    fun TrackOptionsSheet(
        track: TrackDto,
        playlists: List<PlaylistDto>,
        bandName: String,
        onClose: () -> Unit,
        onAddToQueue: () -> Unit,
        onAddToPlaylist: (String) -> Unit
    ) {

        var isSelectingPlaylist by remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            if (!isSelectingPlaylist) {

                Text(
                    text = track.title,

                    style =
                        MaterialTheme.typography.titleLarge,

                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                SheetOptionRow(
                    icon = Icons.Default.QueueMusic,
                    title = "Додати в чергу"
                ) {

                    onAddToQueue()
                    onClose()
                }

                SheetOptionRow(
                    icon = Icons.Default.PlaylistAdd,
                    title = "Додати в плейліст"
                ) {

                    isSelectingPlaylist = true
                }

            } else {

                Text(
                    text = "Оберіть плейліст",

                    style =
                        MaterialTheme.typography.titleLarge,

                    fontWeight = FontWeight.Bold,

                    color =
                        MaterialTheme.colorScheme.primary
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                if (playlists.isEmpty()) {

                    Text(
                        "У вас ще немає плейлістів.",

                        color = Color.Gray,

                        modifier = Modifier.padding(
                            bottom = 16.dp
                        )
                    )

                } else {

                    LazyColumn(
                        modifier = Modifier.heightIn(
                            max = 300.dp
                        )
                    ) {

                        items(playlists) { playlist ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        onAddToPlaylist(
                                            playlist.id
                                        )

                                        onClose()
                                    }
                                    .padding(
                                        vertical = 12.dp,
                                        horizontal = 8.dp
                                    ),

                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Icon(
                                    Icons.Default.LibraryMusic,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )

                                Spacer(
                                    modifier = Modifier.width(16.dp)
                                )

                                Text(
                                    playlist.name,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = {
                        isSelectingPlaylist = false
                    }
                ) {

                    Text(
                        "Назад",
                        color = Color.Gray
                    )
                }
            }
        }
    }

    @Composable
    fun SheetOptionRow(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        title: String,
        onClick: () -> Unit
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(
                    vertical = 12.dp,
                    horizontal = 8.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                icon,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Text(
                text = title,

                style =
                    MaterialTheme.typography.bodyLarge
            )
        }
    }
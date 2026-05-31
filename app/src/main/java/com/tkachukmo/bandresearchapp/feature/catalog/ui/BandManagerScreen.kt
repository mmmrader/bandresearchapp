package com.tkachukmo.bandresearchapp.feature.catalog.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.tkachukmo.bandresearchapp.data.remote.dto.ChatMessageDto
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ApplicationDto
import com.tkachukmo.bandresearchapp.data.remote.dto.BandEventDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ReleaseDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.data.remote.dto.VacancyDto
import com.tkachukmo.bandresearchapp.data.remote.dto.VideoDto
import com.tkachukmo.bandresearchapp.feature.catalog.viewmodel.BandManagerViewModel
import com.tkachukmo.bandresearchapp.feature.discover.ui.MiniPlayer
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandManagerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToChat: (partnerId: String, chatName: String) -> Unit = { _, _ -> },
    viewModel: BandManagerViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val currentBand by viewModel.currentBand.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val releases by viewModel.releases.collectAsState()
    val events by viewModel.events.collectAsState()
    val vacancies by viewModel.vacancies.collectAsState()
    val applications by viewModel.applications.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isCreatingBand by rememberSaveable { mutableStateOf(false) }
    var isUploadingTrack by rememberSaveable { mutableStateOf(false) }
    var isAddingVideo by rememberSaveable { mutableStateOf(false) }
    var isCreatingRelease by rememberSaveable { mutableStateOf(false) }
    var isEditingProfile by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        val msg = viewModel.errorMessage.value
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                MiniPlayer(onNavigateToPlayer = { /* TODO */ })
                NavigationBar {
                    NavigationBarItem(selected = false, onClick = { onNavigateToTab(0) }, icon = { Icon(Icons.Outlined.LibraryMusic, "Каталог") }, label = { Text("Каталог") })
                    NavigationBarItem(selected = false, onClick = { onNavigateToTab(1) }, icon = { Icon(Icons.Outlined.Search, "Пошук") }, label = { Text("Пошук") })
                    NavigationBarItem(selected = false, onClick = { onNavigateToTab(2) }, icon = { Icon(Icons.Outlined.Event, "Події") }, label = { Text("Події") })
                    NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Filled.Person, "Профіль") }, label = { Text("Профіль") })
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                isCreatingRelease -> AddReleaseForm(viewModel, { isCreatingRelease = false }, { isCreatingRelease = false })
                isUploadingTrack -> UploadTrackForm(viewModel, releases, { viewModel.clearUploadForm(); isUploadingTrack = false }, { viewModel.uploadTrack(context) { isUploadingTrack = false } })
                isAddingVideo -> AddVideoForm(viewModel, { isAddingVideo = false }, { isAddingVideo = false })
                isEditingProfile && currentBand != null -> EditBandProfile(currentBand!!, viewModel) { isEditingProfile = false }
                currentBand != null -> BandDashboard(currentBand!!, tracks, videos, releases, events, vacancies, applications, viewModel, { isEditingProfile = true }, { isUploadingTrack = true }, { isAddingVideo = true }, { isCreatingRelease = true }, onNavigateToChat = onNavigateToChat)
                isCreatingBand -> CreateBandForm(viewModel) { isCreatingBand = false }
                else -> EmptyBandState { isCreatingBand = true }
            }

            IconButton(
                onClick = {
                    when {
                        isUploadingTrack -> { viewModel.clearUploadForm(); isUploadingTrack = false }
                        isCreatingRelease -> isCreatingRelease = false
                        isAddingVideo -> isAddingVideo = false
                        isEditingProfile -> isEditingProfile = false
                        isCreatingBand -> isCreatingBand = false
                        else -> onNavigateBack()
                    }
                },
                modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp).statusBarsPadding().background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
        }
    }
}

@Composable
fun BandDashboard(
    band: BandDto,
    tracks: List<TrackDto>,
    videos: List<VideoDto>,
    releases: List<ReleaseDto>,
    events: List<BandEventDto>,
    vacancies: List<VacancyDto>,
    applications: List<ApplicationDto>,
    viewModel: BandManagerViewModel,
    onEditClick: () -> Unit,
    onAddTrackClick: () -> Unit,
    onAddVideoClick: () -> Unit,
    onAddReleaseClick: () -> Unit,
    onNavigateToChat: (String, String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { viewModel.uploadBandImage(context, it, false) } }
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { viewModel.uploadBandImage(context, it, true) } }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            Box(modifier = Modifier.fillMaxSize().clickable { coverPicker.launch("image/*") }) {
                if (band.coverUrl != null) AsyncImage(
                    model = band.coverUrl,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                else Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF6750A4),
                                Color(0xFF21005D)
                            )
                        )
                    )
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = (50).dp).size(100.dp)
                    .clip(CircleShape).background(MaterialTheme.colorScheme.surface)
                    .clickable { avatarPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (band.avatarUrl != null) AsyncImage(
                    model = band.avatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                else Text(
                    band.name.take(2).uppercase(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = band.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            band.genres.takeIf { it.isNotEmpty() }?.let {
                Text(
                    text = it.joinToString(", "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!band.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = band.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardStatCard(
                    Modifier.weight(1f),
                    Icons.Default.People,
                    band.followersCount.toString(),
                    "Підписників",
                    MaterialTheme.colorScheme.secondaryContainer
                )
                DashboardStatCard(
                    Modifier.weight(1f),
                    Icons.Default.PlayArrow,
                    band.playsCount.toString(),
                    "Прослуховувань",
                    MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Альбоми / EP / Сингли",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onAddReleaseClick) {
                Icon(
                    Icons.Default.LibraryAdd,
                    contentDescription = "Створити альбом",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (releases.isEmpty()) Text(
            "Створіть свій перший реліз перед завантаженням треків",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else {
            releases.forEach { release ->
                ReleaseItem(release)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Окремі треки",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onAddTrackClick) {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = "Додати трек",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (tracks.isEmpty()) Text(
            "Немає треків",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else tracks.forEach { track ->
            TrackItem(
                track,
                { viewModel.playTrack(track, tracks) },
                { viewModel.deleteTrack(track.id) })
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ваші відео",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onAddVideoClick) {
                Icon(
                    Icons.Default.VideoCall,
                    contentDescription = "Додати відео",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (videos.isEmpty()) Text(
            "Немає доданих відео",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        else videos.forEach { video ->
            VideoItem(video) { viewModel.deleteVideo(video.id) }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        BandEventsManagerSection(
            events = events,
            onCreateEvent = { title, desc, type, date, venue, city, smart, spotify, apple, youtube ->
                viewModel.createManualEvent(
                    title,
                    desc,
                    type,
                    date,
                    venue,
                    city,
                    smart,
                    spotify,
                    apple,
                    youtube
                ) {}
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        VacanciesManagerSection(
            vacancies = vacancies,
            applications = applications,
            onNavigateToChat = onNavigateToChat,
            viewModel = viewModel,
            onCreateVacancy = viewModel::createVacancy,
            onDeleteVacancy = viewModel::deleteVacancy,
            onAccept = { app -> viewModel.updateApplicationStatus(app, "accepted") }, // <--- ДОДАТИ ЦЕ
            onReject = { app -> viewModel.updateApplicationStatus(app, "rejected") }, // <--- ДОДАТИ ЦЕ
            onMessage = { app, msg -> viewModel.sendMessageToCandidate(app, msg) }    // <--- ДОДАТИ ЦЕ
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun BandEventsManagerSection(
    events: List<BandEventDto>,
    onCreateEvent: (String, String, String, String, String, String, String, String, String, String) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Події", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showForm = !showForm }) {
                Icon(Icons.Default.PostAdd, contentDescription = "Створити подію", tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (showForm) {
            EventEditorCard(
                onSave = { title, desc, type, date, venue, city, smart, spotify, apple, youtube ->
                    onCreateEvent(title, desc, type, date, venue, city, smart, spotify, apple, youtube)
                    showForm = false
                },
                onCancel = { showForm = false }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (events.isEmpty()) {
            Text("Створіть першу новину, концерт або релізну публікацію", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            events.take(5).forEach { event ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            when (event.type) {
                                "release" -> Icons.Default.LibraryMusic
                                "video" -> Icons.Default.VideoLibrary
                                "concert", "tour" -> Icons.Default.Event
                                else -> Icons.Default.Campaign
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(event.title, fontWeight = FontWeight.SemiBold)
                            Text(event.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventEditorCard(
    onSave: (String, String, String, String, String, String, String, String, String, String) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("news") }
    var date by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var smart by remember { mutableStateOf("") }
    var spotify by remember { mutableStateOf("") }
    var apple by remember { mutableStateOf("") }
    var youtube by remember { mutableStateOf("") }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Назва") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, { description = it }, label = { Text("Опис") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("news", "release", "video", "concert", "tour").forEach { option ->
                    FilterChip(selected = type == option, onClick = { type = option }, label = { Text(option) })
                }
            }
            OutlinedTextField(date, { date = it }, label = { Text("Дата: 2026-06-12") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(venue, { venue = it }, label = { Text("Місце") }, modifier = Modifier.weight(1f))
                OutlinedTextField(city, { city = it }, label = { Text("Місто") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(smart, { smart = it }, label = { Text("Smart-link") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(spotify, { spotify = it }, label = { Text("Spotify") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(apple, { apple = it }, label = { Text("Apple Music") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(youtube, { youtube = it }, label = { Text("YouTube Music") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Скасувати") }
                Button(onClick = { onSave(title, description, type, date, venue, city, smart, spotify, apple, youtube) }, enabled = title.isNotBlank(), modifier = Modifier.weight(1f)) { Text("Опублікувати") }
            }
        }
    }
}

@Composable
fun VacanciesManagerSection(
    vacancies: List<VacancyDto>,
    applications: List<ApplicationDto>,
    onCreateVacancy: (String, String, String) -> Unit,
    onDeleteVacancy: (String) -> Unit,
    viewModel: BandManagerViewModel,
    onAccept: (ApplicationDto) -> Unit,
    onReject: (ApplicationDto) -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    onMessage: (ApplicationDto, String) -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var instrument by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Вакансії та заявки", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showForm = !showForm }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Створити вакансію", tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (showForm) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(instrument, { instrument = it }, label = { Text("Інструмент") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(description, { description = it }, label = { Text("Опис") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(city, { city = it }, label = { Text("Місто") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            onCreateVacancy(instrument, description, city)
                            instrument = ""
                            description = ""
                            city = ""
                            showForm = false
                        },
                        enabled = instrument.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Опублікувати вакансію") }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (vacancies.isEmpty()) {
            Text("Відкритих вакансій немає", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            vacancies.forEach { vacancy ->
                VacancyManagerItem(vacancy, onDeleteVacancy)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Відгуки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (applications.isEmpty()) {
            Text("Нових кандидатів поки немає", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        } else {
            applications.forEach { app ->
                ApplicationManagerItem(
                    application = app,
                    onNavigateToChat = onNavigateToChat,
                    vacancy = vacancies.find { it.id == app.vacancyId },
                    viewModel = viewModel, // <--- ПЕРЕДАЄМО VIEWMODEL
                    onAccept = { viewModel.updateApplicationStatus(app, "accepted") },
                    onReject = { viewModel.updateApplicationStatus(app, "rejected") }
                )
            }
        }
    }
}

@Composable
fun VacancyManagerItem(vacancy: VacancyDto, onDelete: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.PersonSearch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(vacancy.instrument, fontWeight = FontWeight.SemiBold)
                Text(vacancy.city ?: "Місто не вказане", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onDelete(vacancy.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Закрити вакансію", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ApplicationManagerItem(
    application: ApplicationDto,
    vacancy: VacancyDto?,
    viewModel: BandManagerViewModel,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onNavigateToChat: (partnerId: String, chatName: String) -> Unit = { _, _ -> }
) {
    var showProfile by remember { mutableStateOf(false) }
    val candidateProfile by viewModel.candidateProfile.collectAsState()

    // Вікно профілю кандидата
    if (showProfile && candidateProfile != null) {
        AlertDialog(
            onDismissRequest = { showProfile = false },
            title = { Text("Профіль кандидата") },
            text = {
                Column {
                    Text("Ім'я: ${candidateProfile?.displayName ?: "Не вказано"}", fontWeight = FontWeight.Bold)
                    Text("Інструмент: ${candidateProfile?.instrument ?: "Не вказано"}")
                    Text("Досвід: ${candidateProfile?.experience ?: "Не вказано"}")
                    Text("Місто: ${candidateProfile?.location ?: "Не вказано"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Про себе: ${candidateProfile?.bio ?: "Немає інформації"}", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { showProfile = false }) { Text("Закрити") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Кандидат: ${application.userId.take(8)}", fontWeight = FontWeight.Bold)
            Text(
                "Вакансія: ${vacancy?.instrument ?: application.vacancyId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            application.message?.takeIf { it.isNotBlank() }?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(it)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (application.status == "accepted") {
                    // Прийнята заявка — показуємо тільки "Написати"
                    Button(
                        onClick = {
                            // Просто передаємо звичайний текст, NavGraph сам його закодує
                            onNavigateToChat(application.userId, "Кандидат: ${application.userId.take(8)}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Написати")
                    }
                } else {
                    // Pending — Профіль / Відхилити / Прийняти
                    OutlinedButton(onClick = {
                        viewModel.loadCandidateProfile(application.userId)
                        showProfile = true
                    }) { Text("Профіль") }

                    OutlinedButton(onClick = onReject) { Text("Відхилити") }
                    Button(onClick = onAccept) { Text("Прийняти") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReleaseForm(viewModel: BandManagerViewModel, onCancel: () -> Unit, onSave: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    var selectedType by remember { mutableStateOf("single") }
    var expanded by remember { mutableStateOf(false) }
    var coverUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val types = mapOf("single" to "Сингл", "ep" to "EP", "album" to "Альбом")
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> coverUri = uri }

    Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Створити Реліз", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.size(160.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { imagePicker.launch("image/*") }, contentAlignment = Alignment.Center) {
            if (coverUri != null) AsyncImage(model = coverUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(48.dp))
                Text("Обкладинка", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Назва") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Рік випуску") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = types[selectedType] ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Тип релізу") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                types.forEach { (key, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { selectedType = key; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(50.dp)) { Text("Скасувати") }
            Button(
                onClick = { viewModel.createRelease(context, title, selectedType, year.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR), coverUri, onSave) },
                modifier = Modifier.weight(1f).height(50.dp),
                enabled = title.isNotBlank()
            ) { Text("Створити") }
        }
    }
}

@Composable
fun ReleaseItem(release: ReleaseDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                if (release.coverUrl != null) AsyncImage(model = release.coverUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Icon(Icons.Default.Album, null)
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(text = release.title, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = "${release.releaseType.uppercase()} • ${release.releaseYear ?: ""}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadTrackForm(viewModel: BandManagerViewModel, releases: List<ReleaseDto>, onCancel: () -> Unit, onSave: () -> Unit) {
    val context = LocalContext.current
    val selectedUri by viewModel.selectedFileUri.collectAsState()
    val uploadTitle by viewModel.uploadTitle.collectAsState()
    val uploadArtwork by viewModel.uploadArtwork.collectAsState()
    val selectedReleaseId by viewModel.selectedReleaseId.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { viewModel.analyzeAudioFile(context, it) } }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { viewModel.updateUploadArtwork(context, it) } }

    Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Новий трек", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (selectedUri == null) {
            Card(modifier = Modifier.fillMaxWidth().height(200.dp), onClick = { audioPicker.launch("audio/*") }) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(64.dp))
                    Text("Оберіть .mp3 файл")
                }
            }
        } else {
            Box(modifier = Modifier.size(160.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable { imagePicker.launch("image/*") }, contentAlignment = Alignment.Center) {
                // ВАЖЛИВО: Використовуємо AsyncImage замість BitmapFactory, щоб не крашити додаток
                if (uploadArtwork != null) {
                    AsyncImage(
                        model = uploadArtwork,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(48.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = uploadTitle, onValueChange = { viewModel.updateUploadTitle(it) }, label = { Text("Назва треку") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            if (releases.isNotEmpty()) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    val currentSelectionName = releases.find { it.id == selectedReleaseId }?.title ?: "Оберіть альбом"
                    OutlinedTextField(
                        value = currentSelectionName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Додати в реліз") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        releases.forEach { release ->
                            DropdownMenuItem(text = { Text(release.title) }, onClick = { viewModel.updateSelectedRelease(release.id); expanded = false })
                        }
                    }
                }
            } else {
                Text("Увага: ви не створили жодного релізу. Трек буде додано без альбому.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(50.dp)) { Text("Скасувати") }
                Button(onClick = onSave, modifier = Modifier.weight(1f).height(50.dp), enabled = uploadTitle.isNotBlank()) { Text("Завантажити") }
            }
        }
    }
}

@Composable
fun AddVideoForm(viewModel: BandManagerViewModel, onCancel: () -> Unit, onSave: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)) {
        Text("Додати YouTube Відео", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Назва відео") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Посилання на YouTube") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(50.dp)) { Text("Скасувати") }
            Button(onClick = { viewModel.addYouTubeVideo(title, url, onSave) }, modifier = Modifier.weight(1f).height(50.dp), enabled = title.isNotBlank() && url.isNotBlank()) { Text("Додати") }
        }
    }
}

@Composable
fun VideoItem(video: VideoDto, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Видалення") },
            text = { Text("Видалити відео ${video.title}?") },
            confirmButton = { TextButton(onClick = { showDialog = false; onDelete() }) { Text("Видалити", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Скасувати") } }
        )
    }
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(100.dp).height(56.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black), contentAlignment = Alignment.Center) {
                if (video.thumbnailUrl != null) AsyncImage(model = video.thumbnailUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Icon(Icons.Default.PlayCircle, null, tint = Color.White)
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(text = video.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { showDialog = true }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun EditBandProfile(band: BandDto, viewModel: BandManagerViewModel, onDone: () -> Unit) {
    var name by remember { mutableStateOf(band.name) }
    var description by remember { mutableStateOf(band.description ?: "") }
    Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)) {
        Text("Редагування профілю", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Назва гурту") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Опис гурту") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 5)
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { viewModel.updateBandInfo(name, description) { onDone() } }, modifier = Modifier.fillMaxWidth().height(56.dp), enabled = name.isNotBlank()) { Text("Зберегти зміни") }
    }
}

@Composable
fun DashboardStatCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, modifier = Modifier.size(28.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun CreateBandForm(viewModel: BandManagerViewModel, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var genres by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)) {
        Text("Створення гурту", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Назва гурту") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = slug, onValueChange = { slug = it }, label = { Text("Унікальне посилання (slug)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = genres, onValueChange = { genres = it }, label = { Text("Жанри (через кому)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { viewModel.createBand(name, slug, genres) { onDone() } }, modifier = Modifier.fillMaxWidth().height(56.dp), enabled = name.isNotBlank()) { Text("Створити") }
    }
}

@Composable
fun EmptyBandState(onCreateClick: () -> Unit) {
    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Text("У тебе ще немає гурту", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreateClick, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Створити гурт") }
    }
}

@Composable
fun TrackItem(track: TrackDto, onPlay: () -> Unit, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Видалення") },
            text = { Text("Видалити трек ${track.title}?") },
            confirmButton = { TextButton(onClick = { showDialog = false; onDelete() }) { Text("Видалити", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Скасувати") } }
        )
    }
    Card(modifier = Modifier.fillMaxWidth().clickable { onPlay() }.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                if (track.coverUrl != null) AsyncImage(model = track.coverUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                else Icon(Icons.Default.MusicNote, null)
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(text = track.title, fontWeight = FontWeight.Bold)
                Text(text = "${track.playsCount} прослуховувань", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { showDialog = true }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}
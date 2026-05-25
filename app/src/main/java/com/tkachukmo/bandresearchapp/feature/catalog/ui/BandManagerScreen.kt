package com.tkachukmo.bandresearchapp.feature.catalog.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.feature.catalog.viewmodel.BandManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BandManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: BandManagerViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val currentBand by viewModel.currentBand.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var isCreatingBand by remember { mutableStateOf(false) }
    var isUploadingTrack by remember { mutableStateOf(false) }

    var bandName by remember { mutableStateOf("") }
    var bandSlug by remember { mutableStateOf("") }
    var bandGenres by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кабінет гурту", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isUploadingTrack) {
                            viewModel.clearUploadForm()
                            isUploadingTrack = false
                        } else if (isCreatingBand) {
                            isCreatingBand = false
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentBand != null && !isCreatingBand && !isUploadingTrack) {
                ExtendedFloatingActionButton(
                    onClick = { isUploadingTrack = true },
                    icon = { Icon(Icons.Default.Add, "Додати") },
                    text = { Text("Новий реліз") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()

                isUploadingTrack -> {
                    UploadTrackForm(
                        viewModel = viewModel,
                        onCancel = {
                            viewModel.clearUploadForm()
                            isUploadingTrack = false
                        },
                        onSave = {
                            viewModel.uploadTrack(context) {
                                isUploadingTrack = false
                            }
                        }
                    )
                }

                currentBand != null -> {
                    BandDashboard(
                        band = currentBand!!,
                        tracks = tracks,
                        viewModel = viewModel
                    )
                }

                isCreatingBand -> {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Створення профілю гурту", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(value = bandName, onValueChange = { bandName = it }, label = { Text("Назва гурту") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = bandSlug, onValueChange = { bandSlug = it }, label = { Text("Унікальне посилання (slug)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = bandGenres, onValueChange = { bandGenres = it }, label = { Text("Жанри (через кому)") }, modifier = Modifier.fillMaxWidth())

                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { viewModel.createBand(bandName, bandSlug, bandGenres, onSuccess = { isCreatingBand = false }) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = bandName.isNotBlank() && bandSlug.isNotBlank() && bandGenres.isNotBlank()
                        ) { Text("Зберегти та продовжити") }
                    }
                }

                else -> {
                    EmptyBandState(onCreateClick = { isCreatingBand = true })
                }
            }
        }
    }
}

@Composable
fun UploadTrackForm(
    viewModel: BandManagerViewModel,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val selectedUri by viewModel.selectedFileUri.collectAsState()
    val uploadTitle by viewModel.uploadTitle.collectAsState()
    val uploadDuration by viewModel.uploadDuration.collectAsState()
    val uploadArtwork by viewModel.uploadArtwork.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val audioPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.analyzeAudioFile(context, uri)
    }

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                viewModel.updateUploadArtwork(context, uri)
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val cropOptions = CropImageContractOptions(uri, CropImageOptions(
                fixAspectRatio = true,
                aspectRatioX = 1,
                aspectRatioY = 1,
                activityBackgroundColor = android.graphics.Color.BLACK,
                toolbarColor = android.graphics.Color.parseColor("#121218"),
                toolbarTitleColor = android.graphics.Color.WHITE,
                activityMenuIconColor = android.graphics.Color.WHITE
            ))
            imageCropLauncher.launch(cropOptions)
        }
    }

    val artworkBitmap = remember(uploadArtwork) {
        uploadArtwork?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Новий трек", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (selectedUri == null) {
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                onClick = { audioPickerLauncher.launch("audio/*") }
            ) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(64.dp))
                    Text("Оберіть .mp3 файл", fontWeight = FontWeight.Medium)
                }
            }
        } else {
            Box(
                modifier = Modifier.size(160.dp).clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (artworkBitmap != null) {
                    Image(bitmap = artworkBitmap, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Text("Змінити фото", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = uploadTitle, onValueChange = { viewModel.updateUploadTitle(it) }, label = { Text("Назва треку") }, modifier = Modifier.fillMaxWidth())
            Text(text = "Тривалість: ${uploadDuration / 60}:${(uploadDuration % 60).toString().padStart(2, '0')}", modifier = Modifier.align(Alignment.Start).padding(top = 8.dp))

            Spacer(modifier = Modifier.weight(1f))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(50.dp)) { Text("Скасувати") }
                Button(onClick = onSave, modifier = Modifier.weight(1f).height(50.dp), enabled = uploadTitle.isNotBlank()) { Text("Опублікувати") }
            }
        }
    }
}

@Composable
fun BandDashboard(
    band: BandDto,
    tracks: List<TrackDto>,
    viewModel: BandManagerViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Text(text = band.name.take(2).uppercase(), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = band.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        band.genres?.let { Text(text = it.joinToString(", "), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // ТУТ ВИПРАВЛЕНО: followersCount та playsCount
            DashboardStatCard(Modifier.weight(1f), Icons.Default.People, band.followersCount.toString(), "Підписників", MaterialTheme.colorScheme.secondaryContainer)
            DashboardStatCard(Modifier.weight(1f), Icons.Default.PlayArrow, band.playsCount.toString(), "Прослуховувань", MaterialTheme.colorScheme.tertiaryContainer)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Ваші релізи", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = { }) { Icon(Icons.Default.Edit, contentDescription = "Редагувати") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (tracks.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.BarChart, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("У вас ще немає завантажених релізів", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            tracks.forEach { track ->
                TrackItem(
                    track = track,
                    onPlay = { viewModel.playTrack(track, tracks) },
                    onDelete = { viewModel.deleteTrack(track.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TrackItem(
    track: TrackDto,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Видалення треку") },
            text = { Text("Ви впевнені, що хочете видалити «${track.title}»? Цю дію неможливо скасувати.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Видалити", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Скасувати") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // ТУТ ВИПРАВЛЕНО: coverUrl
                if (track.coverUrl != null) {
                    AsyncImage(
                        model = track.coverUrl,
                        contentDescription = "Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = track.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                // ТУТ ВИПРАВЛЕНО: playsCount та durationSec
                Text(
                    text = "${track.playsCount} прослуховувань • ${track.durationSec / 60}:${(track.durationSec % 60).toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Видалити", tint = MaterialTheme.colorScheme.error)
            }
        }
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
fun EmptyBandState(onCreateClick: () -> Unit) {
    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Text("У тебе ще немає гурту", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Створи профіль для свого гурту, щоб завантажувати треки.", textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 16.dp))
        Button(onClick = onCreateClick, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Створити гурт") }
    }
}
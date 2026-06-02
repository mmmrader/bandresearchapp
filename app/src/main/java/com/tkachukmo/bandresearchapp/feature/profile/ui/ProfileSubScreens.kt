package com.tkachukmo.bandresearchapp.feature.profile.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.feature.profile.viewmodel.PasswordChangeState
import com.tkachukmo.bandresearchapp.feature.profile.viewmodel.PlaylistDetailTrack
import com.tkachukmo.bandresearchapp.feature.profile.viewmodel.ProfileViewModel
import com.tkachukmo.bandresearchapp.feature.discover.viewmodel.PlayerViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.GroupAdd

// ==========================================
// КОЛЬОРИ
// ==========================================

private val SubBg = Color(0xFF141414)
private val SubCardBg = Color(0xFF1E1E1E)
private val SubCardStroke = Color(0xFF2C2C2C)
private val SubPrimary = Color(0xFFB288FF)
private val SubTextMuted = Color(0xFFA0A0A0)
private val SuccessGreen = Color(0xFF00E676)
private val ErrorRed = Color(0xFFFF5252)

private val availableMusicGenres = listOf(
    "Рок", "Поп", "Метал", "Джаз",
    "Фолк", "Електроніка", "Хіп-хоп", "Класика"
)

// ==========================================
// 1. РЕДАГУВАТИ ПРОФІЛЬ
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()

    var name by remember(profile) { mutableStateOf(profile?.displayName ?: "") }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "") }
    var showSocialDialog by remember { mutableStateOf(false) }
    var showGenresDialog by remember { mutableStateOf(false) }
    var socialLink by remember(profile) { mutableStateOf(profile?.socialLink ?: "") }
    var instrument by remember(profile) { mutableStateOf(profile?.instrument ?: "") }
    var experience by remember(profile) { mutableStateOf(profile?.experience ?: "") }
    var location by remember(profile) { mutableStateOf(profile?.location ?: "") }
    var youtubeLink by remember(profile) { mutableStateOf(profile?.youtubeLink ?: "") }
    var audioLink by remember(profile) { mutableStateOf(profile?.audioLink ?: "") }
    var showMusicianDialog by remember { mutableStateOf(false) }
    val selectedGenres = remember(profile) {
        mutableStateListOf<String>().apply {
            addAll(profile?.musicGenres ?: emptyList())
        }
    }

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) result.uriContent?.let { viewModel.uploadAvatar(context, it) }
    }
    val standardPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { sourceUri ->
            imageCropLauncher.launch(
                CropImageContractOptions(
                    uri = sourceUri,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeGallery = true,
                        imageSourceIncludeCamera = false,
                        guidelines = CropImageView.Guidelines.ON,
                        aspectRatioX = 1, aspectRatioY = 1, fixAspectRatio = true,
                        activityBackgroundColor = android.graphics.Color.BLACK,
                        backgroundColor = android.graphics.Color.argb(170, 0, 0, 0)
                    )
                )
            )
        }
    }

    Scaffold(containerColor = SubBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                    }
                    Text(
                        "Редагувати профіль",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(2.dp, SubPrimary.copy(alpha = 0.5f), CircleShape)
                        .clip(CircleShape)
                        .background(SubCardBg)
                        .clickable { standardPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profile?.avatarUrl != null) {
                        AsyncImage(
                            model = profile!!.avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person, null,
                            tint = SubTextMuted, modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = 8.dp)
                        .size(40.dp)
                        .background(SubPrimary, CircleShape)
                        .border(4.dp, SubBg, CircleShape)
                        .clickable { standardPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PhotoCamera, null,
                        tint = SubBg, modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text("Нікнейм", color = SubPrimary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(profile?.displayName ?: "Введіть ваш нікнейм", color = SubTextMuted) },
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
                trailingIcon = {
                    Text(
                        "@", color = Color.White, fontSize = 20.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                    unfocusedContainerColor = SubCardBg, focusedContainerColor = SubCardBg
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Про себе (Біографія)", color = SubPrimary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                placeholder = { Text(profile?.bio ?: "Розкажіть трохи про себе...", color = SubTextMuted) },
                textStyle = LocalTextStyle.current.copy(
                    color = Color.White, fontSize = 16.sp, lineHeight = 24.sp
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                    unfocusedContainerColor = SubCardBg, focusedContainerColor = SubCardBg
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EditProfileMiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Link,
                    title = "Соціальні мережі",
                    subtitle = socialLink.ifBlank { "Не додано" },
                    onClick = { showSocialDialog = true }
                )
                EditProfileMiniCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.LibraryMusic,
                    title = "Жанри",
                    subtitle = if (selectedGenres.isEmpty()) "Не обрано" else selectedGenres.joinToString(", "),
                    iconTint = SubPrimary,
                    onClick = { showGenresDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Секція музиканта
            Text("Профіль музиканта", color = SubPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Інструмент", color = SubTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = instrument,
                        onValueChange = { instrument = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Гітара, бас, барабани...", color = SubTextMuted, fontSize = 12.sp) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                        leadingIcon = { Icon(Icons.Default.Piano, null, tint = SubTextMuted, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                            unfocusedContainerColor = SubCardBg, focusedContainerColor = SubCardBg
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Досвід / стаж", color = SubTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = experience,
                        onValueChange = { experience = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("3 роки, початківець...", color = SubTextMuted, fontSize = 12.sp) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                        leadingIcon = { Icon(Icons.Default.Work, null, tint = SubTextMuted, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                            unfocusedContainerColor = SubCardBg, focusedContainerColor = SubCardBg
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Місто", color = SubTextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Київ, Харків, Одеса...", color = SubTextMuted) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = SubTextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                    unfocusedContainerColor = SubCardBg, focusedContainerColor = SubCardBg
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("YouTube (відео-портфоліо)", color = SubTextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = youtubeLink,
                onValueChange = { youtubeLink = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("https://youtube.com/...", color = SubTextMuted) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                leadingIcon = { Icon(Icons.Default.PlayCircle, null, tint = SubTextMuted) },
                isError = youtubeLink.isNotBlank() && !youtubeLink.contains("youtube") && !youtubeLink.contains("youtu.be"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                    unfocusedContainerColor = SubCardBg, focusedContainerColor = SubCardBg
                )
            )
            if (youtubeLink.isNotBlank() && !youtubeLink.contains("youtube") && !youtubeLink.contains("youtu.be")) {
                Text("Введіть коректне посилання на YouTube", color = ErrorRed, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("SoundCloud / аудіо-демо", color = SubTextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = audioLink,
                onValueChange = { audioLink = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("https://soundcloud.com/...", color = SubTextMuted) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                leadingIcon = { Icon(Icons.Default.Audiotrack, null, tint = SubTextMuted) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                    unfocusedContainerColor = SubCardBg, focusedContainerColor = SubCardBg
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.updateProfileInfo(
                        newName = name,
                        newBio = bio,
                        newSocialLink = socialLink,
                        newGenres = selectedGenres.toList(),
                        newInstrument = instrument,
                        newExperience = experience,
                        newLocation = location,
                        newYoutubeLink = youtubeLink,
                        newAudioLink = audioLink
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SubPrimary, contentColor = SubBg),
                enabled = name.isNotBlank()
            ) {
                Text("Зберегти зміни", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircleOutline, null, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSocialDialog) {
        AlertDialog(
            onDismissRequest = { showSocialDialog = false },
            containerColor = SubCardBg,
            title = { Text("Соціальні мережі", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Вкажіть посилання на ваш профіль (наприклад, Telegram або Instagram):",
                        color = SubTextMuted, fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = socialLink,
                        onValueChange = { socialLink = it },
                        placeholder = { Text("t.me/username", color = SubTextMuted) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SubCardStroke,
                            focusedBorderColor = SubPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSocialDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SubPrimary, contentColor = SubBg)
                ) { Text("Готово", fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (showGenresDialog) {
        AlertDialog(
            onDismissRequest = { showGenresDialog = false },
            containerColor = SubCardBg,
            title = { Text("Оберіть жанри", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.height(260.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableMusicGenres) { genre ->
                            val isSelected = selectedGenres.contains(genre)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) SubPrimary else SubBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) SubPrimary else SubCardStroke,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (isSelected) selectedGenres.remove(genre)
                                        else selectedGenres.add(genre)
                                    }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    genre,
                                    color = if (isSelected) SubBg else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showGenresDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SubPrimary, contentColor = SubBg)
                ) { Text("Зберегти", fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
fun EditProfileMiniCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconTint: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .defaultMinSize(minHeight = 95.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SubCardBg)
            .border(1.dp, SubCardStroke, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle, color = SubTextMuted, fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==========================================
// 2. ПЛЕЙЛИСТИ — СПИСОК
// ==========================================

@Composable
fun PlaylistsScreen(
    onNavigateBack: () -> Unit,
    onOpenPlaylist: (PlaylistDto) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var newPlaylistName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    // Діалог підтвердження видалення
    var playlistToDelete by remember { mutableStateOf<PlaylistDto?>(null) }

    // Діалог перейменування
    var playlistToRename by remember { mutableStateOf<PlaylistDto?>(null) }
    var renameText by remember { mutableStateOf("") }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = SubBg,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SubCardBg,
                    contentColor = Color.White,
                    actionColor = SubPrimary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White) }
                    Text(
                        "Мої плейлисти",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Керуйте своєю музичною колекцією та створюйте ідеальні мікси для будь-якої події.",
                    color = SubTextMuted, fontSize = 14.sp, lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Поле введення нового плейлиста
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = { Text("Назва нового плейлисту", color = SubTextMuted) },
                        modifier = Modifier.weight(1f).focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                            unfocusedContainerColor = SubCardBg, focusedContainerColor = SubCardBg
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                        },
                        enabled = newPlaylistName.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SubPrimary, contentColor = SubBg,
                            disabledContainerColor = SubCardStroke, disabledContentColor = SubTextMuted
                        )
                    ) { Text("Додати", fontWeight = FontWeight.Bold) }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Картка-заглушка «Створити плейлист»
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SubBg)
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRoundRect(
                            color = SubCardStroke,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            ),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(56.dp).background(SubPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = SubBg, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Створити новий плейлист",
                            color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                        Text("Натисніть, щоб ввести назву", color = SubTextMuted, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "Ваші плейлисти",
                        color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    Text("${playlists.size} елементів", color = SubTextMuted, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (playlists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SubCardBg)
                            .border(1.dp, SubCardStroke, RoundedCornerShape(24.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(80.dp).background(SubCardStroke, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LibraryMusic, null,
                                    tint = SubPrimary, modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Тут поки що порожньо",
                                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Створіть свій перший мікс, щоб зберегти улюблені ритми.",
                                color = SubTextMuted, fontSize = 14.sp, textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistListItem(
                        playlist = playlist,
                        onClick = { onOpenPlaylist(playlist) },
                        onRename = {
                            renameText = playlist.name
                            playlistToRename = playlist
                        },
                        onDelete = { playlistToDelete = playlist }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // Діалог видалення
    playlistToDelete?.let { pl ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            containerColor = SubCardBg,
            title = { Text("Видалити плейлист?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "«${pl.name}» буде видалено разом з усіма треками. Цю дію не можна скасувати.",
                    color = SubTextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePlaylist(pl.id)
                        playlistToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Видалити", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Скасувати", color = SubTextMuted)
                }
            }
        )
    }

    // Діалог перейменування
    playlistToRename?.let { pl ->
        AlertDialog(
            onDismissRequest = { playlistToRename = null },
            containerColor = SubCardBg,
            title = { Text("Перейменувати", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renamePlaylist(pl.id, renameText)
                        playlistToRename = null
                    },
                    enabled = renameText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SubPrimary, contentColor = SubBg)
                ) { Text("Зберегти", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { playlistToRename = null }) {
                    Text("Скасувати", color = SubTextMuted)
                }
            }
        )
    }
}

@Composable
private fun PlaylistListItem(
    playlist: PlaylistDto,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SubCardBg)
            .border(1.dp, SubCardStroke, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SubPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LibraryMusic, null,
                    tint = SubPrimary, modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    playlist.name,
                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium
                )
                Text(
                    if (playlist.isPublic) "Публічний" else "Приватний",
                    color = SubTextMuted, fontSize = 12.sp
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = SubTextMuted)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = SubCardBg
                ) {
                    DropdownMenuItem(
                        text = { Text("Відкрити", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, null, tint = SubPrimary) },
                        onClick = { menuExpanded = false; onClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Перейменувати", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = SubPrimary) },
                        onClick = { menuExpanded = false; onRename() }
                    )
                    HorizontalDivider(color = SubCardStroke)
                    DropdownMenuItem(
                        text = { Text("Видалити", color = ErrorRed) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = ErrorRed) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. ДЕТАЛІ ПЛЕЙЛИСТА — ТРЕКИ
// ==========================================

@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistDto,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val tracks by viewModel.playlistDetailTracks.collectAsState()
    val isLoading by viewModel.isPlaylistLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val availableTracks by viewModel.availableTracks.collectAsState()

    var showAddTrackSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var trackToDelete by remember { mutableStateOf<PlaylistDetailTrack?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(playlist.id) {
        viewModel.loadPlaylistDetails(playlist.id)
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = SubBg,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SubCardBg,
                    contentColor = Color.White,
                    actionColor = SubPrimary
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.loadAvailableTracks()
                    showAddTrackSheet = true
                },
                containerColor = SubPrimary,
                contentColor = SubBg
            ) {
                Icon(Icons.Default.Add, "Додати трек")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Шапка
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        playlist.name,
                        color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${tracks.size} треків",
                        color = SubTextMuted, fontSize = 12.sp
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SubPrimary)
                }
            } else if (tracks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MusicOff, null,
                            tint = SubTextMuted, modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Плейлист порожній",
                            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Натисніть «+» щоб додати треки",
                            color = SubTextMuted, fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 8.dp, bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(tracks, key = { _, t -> t.trackId }) { index, track ->
                        PlaylistTrackItem(
                            track = track,
                            index = index + 1,
                            onPlay = {
                                // Завантажуємо ВЕСЬ плейліст як чергу,
                                // а не лише треки одного гурту
                                playerViewModel.playPlaylistQueue(
                                    playlistName = playlist.name,
                                    tracks = tracks,
                                    startIndex = index
                                )
                                onNavigateToPlayer(track.trackId)
                            },
                            onRemove = { trackToDelete = track }
                        )
                    }
                }
            }
        }
    }

    // Діалог підтвердження видалення треку
    trackToDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { trackToDelete = null },
            containerColor = SubCardBg,
            title = { Text("Видалити трек?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text("«${t.title}» буде видалено з плейлиста.", color = SubTextMuted)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeTrackFromPlaylist(t.playlistId, t.trackId)
                        trackToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Видалити", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { trackToDelete = null }) {
                    Text("Скасувати", color = SubTextMuted)
                }
            }
        )
    }

    // Боковий аркуш для додавання треків
    if (showAddTrackSheet) {
        AddTrackBottomSheet(
            availableTracks = availableTracks,
            searchQuery = searchQuery,
            onSearchQueryChange = { q ->
                searchQuery = q
                viewModel.loadAvailableTracks(q)
            },
            onAddTrack = { trackId ->
                viewModel.addTrackToPlaylist(playlist.id, trackId)
            },
            onDismiss = {
                showAddTrackSheet = false
                searchQuery = ""
            }
        )
    }
}

@Composable
private fun PlaylistTrackItem(
    track: PlaylistDetailTrack,
    index: Int,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SubCardBg)
            .border(1.dp, SubCardStroke, RoundedCornerShape(12.dp))
            .clickable { onPlay() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index",
            color = SubTextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SubCardStroke),
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
                Icon(Icons.Default.MusicNote, null, tint = SubPrimary)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(track.bandName, fontSize = 12.sp, color = SubTextMuted)
        }

        val minutes = track.durationSec / 60
        val seconds = track.durationSec % 60
        Text(
            text = "%d:%02d".format(minutes, seconds),
            fontSize = 12.sp, color = SubTextMuted,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.RemoveCircleOutline, null,
                tint = ErrorRed, modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTrackBottomSheet(
    availableTracks: List<TrackDto>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddTrack: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SubCardBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(SubCardStroke, CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Додати трек",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Пошук треків...", color = SubTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = SubTextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, null, tint = SubTextMuted)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                    unfocusedContainerColor = SubBg, focusedContainerColor = SubBg
                )
            )

            Spacer(Modifier.height(16.dp))

            if (availableTracks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff, null,
                            tint = SubTextMuted, modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Нічого не знайдено", color = SubTextMuted)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableTracks, key = { it.id }) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SubBg)
                                .border(1.dp, SubCardStroke, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SubCardStroke),
                                contentAlignment = Alignment.Center
                            ) {
                                if (track.coverUrl != null) {
                                    AsyncImage(
                                        model = track.coverUrl, contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.MusicNote, null, tint = SubPrimary)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    track.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val min = track.durationSec / 60
                                val sec = track.durationSec % 60
                                Text(
                                    "%d:%02d".format(min, sec),
                                    fontSize = 12.sp, color = SubTextMuted
                                )
                            }
                            IconButton(
                                onClick = {
                                    onAddTrack(track.id)
                                    onDismiss()
                                }
                            ) {
                                Icon(
                                    Icons.Default.AddCircleOutline, null,
                                    tint = SubPrimary, modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. ІСТОРІЯ
// ==========================================

@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onPlayTrack: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val history by viewModel.listeningHistory.collectAsState()

    Scaffold(containerColor = SubBg) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.offset(x = (-12).dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                        }
                    }
                    Text(
                        "Історія прослуховувань",
                        color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Тут з'являтимуться треки, які ти слухав.",
                        color = SubTextMuted, fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SubCardBg)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.MusicOff, null,
                                    tint = SubTextMuted, modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Ваша історія порожня",
                                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Ти ще не прослухав жодного треку.",
                                    color = SubTextMuted, fontSize = 14.sp, textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            if (history.isNotEmpty()) {
                val groupedHistory = history.groupBy { it.listenedAt.take(10) }
                groupedHistory.forEach { (date, items) ->
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                text = date,
                                fontWeight = FontWeight.Bold,
                                color = SubPrimary,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                color = SubCardStroke
                            )
                        }
                    }
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayTrack(item.historyId) }
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SubCardBg)
                                    .border(1.dp, SubCardStroke, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.coverUrl != null)
                                    AsyncImage(
                                        model = item.coverUrl, contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                else Icon(Icons.Default.MusicNote, null, tint = SubPrimary)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.trackTitle, color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Text(item.bandName, fontSize = 12.sp, color = SubTextMuted)
                            }
                            Text(item.listenedAt.takeLast(5), fontSize = 12.sp, color = SubTextMuted)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. БЕЗПЕКА — ЗМІНА ПАРОЛЯ
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val passwordChangeState by viewModel.passwordChangeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Валідація нового пароля
    val hasMinLength = newPassword.length >= 8
    val hasDigit = newPassword.any { it.isDigit() }
    val hasSpecial = newPassword.any { !it.isLetterOrDigit() }
    val passwordsMatch = newPassword == confirmPassword && confirmPassword.isNotEmpty()

    var score = 0
    if (hasMinLength) score++
    if (hasDigit) score++
    if (hasSpecial) score++

    val strengthProgress by animateFloatAsState(targetValue = score / 3f, label = "progress")
    val strengthColor = when (score) {
        1 -> ErrorRed
        2 -> Color(0xFFFFC107)
        3 -> SuccessGreen
        else -> SubCardStroke
    }
    val strengthText = when (score) {
        0 -> if (newPassword.isEmpty()) "—" else "Дуже слабкий"
        1 -> "Слабкий"
        2 -> "Середній"
        3 -> "Надійний"
        else -> "—"
    }

    val isFormValid = score == 3 && passwordsMatch && oldPassword.isNotBlank()

    // Обробка станів
    LaunchedEffect(passwordChangeState) {
        when (val state = passwordChangeState) {
            is PasswordChangeState.Success -> {
                snackbarHostState.showSnackbar("Пароль успішно змінено!")
                oldPassword = ""
                newPassword = ""
                confirmPassword = ""
                viewModel.clearPasswordState()
            }
            is PasswordChangeState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearPasswordState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = SubBg,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SubCardBg,
                    contentColor = Color.White,
                    actionColor = SubPrimary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.offset(x = (-12).dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
                    }
                }
                Text(
                    "Налаштування безпеки",
                    color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Оновіть свій пароль, щоб забезпечити безпеку вашого облікового запису.",
                    color = SubTextMuted, fontSize = 14.sp, lineHeight = 20.sp
                )
                Spacer(Modifier.height(32.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SubCardBg)
                        .padding(20.dp)
                ) {
                    // Старий пароль
                    Text("Старий пароль", color = SubTextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        placeholder = { Text("Введіть поточний пароль", color = SubTextMuted) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = SubTextMuted) },
                        trailingIcon = {
                            IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                Icon(
                                    if (oldPasswordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = SubTextMuted
                                )
                            }
                        },
                        visualTransformation = if (oldPasswordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SubCardStroke, focusedBorderColor = SubPrimary,
                            unfocusedContainerColor = SubBg, focusedContainerColor = SubBg
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    // Новий пароль
                    Text("Новий пароль", color = SubTextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = { Text("Мінімум 8 символів", color = SubTextMuted) },
                        leadingIcon = { Icon(Icons.Default.SyncLock, null, tint = SubTextMuted) },
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    if (newPasswordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = SubTextMuted
                                )
                            }
                        },
                        visualTransformation = if (newPasswordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SubCardStroke,
                            focusedBorderColor = SubPrimary,
                            unfocusedContainerColor = SubBg,
                            focusedContainerColor = SubBg
                        )
                    )

                    // Лічильник надійності
                    if (newPassword.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Надійність: $strengthText", color = SubTextMuted, fontSize = 12.sp)
                            Text(
                                "${(score * 33.3f).toInt().coerceAtMost(100)}%",
                                color = strengthColor, fontSize = 12.sp, fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { strengthProgress },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                            color = strengthColor,
                            trackColor = SubBg
                        )
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SecurityCheckItem("Мінімум 8 символів", hasMinLength)
                            SecurityCheckItem("Хоча б одна цифра", hasDigit)
                            SecurityCheckItem("Спеціальний символ (!@#\$%^&*)", hasSpecial)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Підтвердження нового пароля
                    Text("Підтвердіть новий пароль", color = SubTextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Повторіть новий пароль", color = SubTextMuted) },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, null, tint = SubTextMuted) },
                        trailingIcon = {
                            if (confirmPassword.isNotEmpty()) {
                                Icon(
                                    if (passwordsMatch) Icons.Default.CheckCircle
                                    else Icons.Default.Cancel,
                                    null,
                                    tint = if (passwordsMatch) SuccessGreen else ErrorRed
                                )
                            } else {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        null, tint = SubTextMuted
                                    )
                                }
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible)
                            VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SubCardStroke,
                            focusedBorderColor = SubPrimary,
                            errorBorderColor = ErrorRed,
                            unfocusedContainerColor = SubBg,
                            focusedContainerColor = SubBg
                        )
                    )
                    if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        Spacer(Modifier.height(4.dp))
                        Text("Паролі не збігаються", color = ErrorRed, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.updatePassword(oldPassword, newPassword) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isFormValid && passwordChangeState !is PasswordChangeState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SubPrimary, contentColor = SubBg,
                            disabledContainerColor = SubCardStroke, disabledContentColor = SubTextMuted
                        )
                    ) {
                        if (passwordChangeState is PasswordChangeState.Loading) {
                            CircularProgressIndicator(
                                color = SubBg,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Оновити пароль", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SecurityCheckItem(text: String, isMet: Boolean) {
    val tint by androidx.compose.animation.animateColorAsState(
        targetValue = if (isMet) SuccessGreen else SubTextMuted,
        label = "color"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            null, tint = tint, modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, color = tint, fontSize = 12.sp)
    }
}

// ==========================================
// 6. ПІДТРИМКА
// ==========================================

@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(containerColor = SubBg) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.offset(x = (-12).dp)
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White) }
                Text(
                    "Підтримка",
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Розробник додатку", fontSize = 16.sp, color = SubTextMuted)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Михайло Ткачук",
                    fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SubPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Студент Олександрійського політехнічного фахового коледжу\n(Група ПЗ-221)",
                    fontSize = 14.sp, textAlign = TextAlign.Center, color = SubTextMuted
                )
                Spacer(Modifier.height(48.dp))
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, "https://t.me/underraged".toUri())
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0088CC), contentColor = Color.White
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Зв'язатись в Telegram", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
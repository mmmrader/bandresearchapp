package com.tkachukmo.bandresearchapp.feature.profile.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.tkachukmo.bandresearchapp.feature.profile.ui.EqController
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistDto
import com.tkachukmo.bandresearchapp.data.remote.dto.VacancyDto
import com.tkachukmo.bandresearchapp.feature.profile.viewmodel.ProfileViewModel

// ==========================================
// КОЛЬОРИ
// ==========================================

private val DarkBg = Color(0xFF121212)
val SurfaceDark = Color(0xFF1A1A1A)
val SurfaceVariantDark = Color(0xFF2A2A2A)
val NeonPurple = Color(0xFFB288FF)
private val TextGray = Color(0xFFA0A0A0)

data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val hasSwitch: Boolean = false,
    val hasArrow: Boolean = true,
    val tint: Color = NeonPurple
)

val musicGenres = listOf(
    "Рок", "Поп", "Метал", "Джаз",
    "Фолк", "Електроніка", "Хіп-хоп", "Класика"
)

// ==========================================
// PROFILE SCREEN
// ==========================================

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateToBandManager: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToEqualizer: () -> Unit = {},
    onNavigateToBandDetail: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val followedBands by viewModel.followedBands.collectAsState()
    val managedBand by viewModel.userBand.collectAsState()
    val matchingVacancies by viewModel.matchingVacancies.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val history by viewModel.listeningHistory.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val effectiveNotificationsEnabled = notificationsEnabled && canPostNotifications(context)

    val displayName = profile?.displayName ?: "Користувач"
    val initials = displayName.take(2).uppercase()
    val userGenres = profile?.musicGenres ?: emptyList()
    var showMatchingVacancies by remember { mutableStateOf(false) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationsEnabled(granted)
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
                        aspectRatioX = 1,
                        aspectRatioY = 1,
                        fixAspectRatio = true,
                        activityBackgroundColor = android.graphics.Color.BLACK,
                        backgroundColor = android.graphics.Color.argb(170, 0, 0, 0)
                    )
                )
            )
        }
    }

    LaunchedEffect(Unit) { viewModel.loadUserProfile() }

    LaunchedEffect(notificationsEnabled) {
        if (notificationsEnabled && !canPostNotifications(context)) {
            viewModel.setNotificationsEnabled(false)
        }
    }

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceDark,
                    contentColor = Color.White,
                    actionColor = NeonPurple
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            if (isLoading) {
                CircularProgressIndicator(color = NeonPurple, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // --- ШАПКА ПРОФІЛЮ ---
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp, bottom = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, NeonPurple, CircleShape)
                                    .background(SurfaceDark)
                                    .clickable { standardPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profile?.avatarUrl != null) {
                                    AsyncImage(
                                        model = profile!!.avatarUrl,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = initials,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonPurple,
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
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
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            Text(
                                text = userEmail ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray
                            )
                            if (!profile?.bio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = profile!!.bio!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ProfileStatItem(value = followedBands.size.toString(), label = "Підписки")
                                ProfileStatItem(value = history.size.toString(), label = "Прослухано треків")
                                ProfileStatItem(value = playlists.size.toString(), label = "Плейлисти")
                            }
                        }
                    }

                    // --- КАБІНЕТ ГУРТУ ---
                    item {
                        if (managedBand == null) {
                            NoBandActionsCard(
                                instrument = profile?.instrument,
                                onCreateBand = onNavigateToBandManager,
                                onNavigateToSearch = onNavigateToSearch // <--- Передаємо навігацію
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                                    .clickable { onNavigateToBandManager() }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(NeonPurple.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.MusicNote, null,
                                            tint = NeonPurple, modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Кабінет гурту",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = NeonPurple
                                        )
                                        Text(
                                            "Керуй своїм гуртом",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextGray
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, null, tint = NeonPurple)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // --- МУЗИЧНІ СТИЛІ ---
                    item { ProfileSectionHeader("Музичні стилі") }
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(musicGenres) { genre ->
                                val isSelected = userGenres.contains(genre)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) NeonPurple else SurfaceDark)
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonPurple else SurfaceVariantDark,
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable { viewModel.toggleGenre(genre) }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = genre,
                                        color = if (isSelected) DarkBg else Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // --- ВАШІ ПІДПИСКИ ---
                    item { ProfileSectionHeader("Ваші підписки") }
                    item {
                        if (followedBands.isEmpty()) {
                            Text(
                                "Ви ще не підписалися на жоден гурт",
                                color = TextGray,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(followedBands) { band ->
                                    FollowedBandItem(
                                        band = band,
                                        onClick = { onNavigateToBandDetail(band.id) }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // --- БІБЛІОТЕКА ---
                    item { ProfileSectionHeader("Бібліотека") }
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                        ) {
                            ProfileMenuItemRow(
                                ProfileMenuItem(
                                    Icons.Default.LibraryMusic,
                                    "Мої плейлисти",
                                    "${playlists.size} плейлістів"
                                ),
                                onClick = onNavigateToPlaylists
                            )
                            HorizontalDivider(color = SurfaceVariantDark)
                            ProfileMenuItemRow(
                                ProfileMenuItem(
                                    Icons.Default.MusicNote,
                                    "Історія прослуховувань",
                                    historyCountLabel(history.size)
                                ),
                                onClick = onNavigateToHistory
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // --- НАЛАШТУВАННЯ ---
                    item { ProfileSectionHeader("Налаштування") }
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                        ) {
                            ProfileMenuItemRow(
                                ProfileMenuItem(Icons.Default.Person, "Редагувати профіль"),
                                onClick = onNavigateToEditProfile
                            )
                            HorizontalDivider(color = SurfaceVariantDark)
                            ProfileMenuItemRow(
                                ProfileMenuItem(
                                    Icons.Default.Notifications,
                                    "Сповіщення",
                                    hasSwitch = true,
                                    hasArrow = false
                                ),
                                switchValue = effectiveNotificationsEnabled,
                                onSwitchChange = { enabled ->
                                    if (!enabled) {
                                        viewModel.setNotificationsEnabled(false)
                                    } else if (canPostNotifications(context)) {
                                        viewModel.setNotificationsEnabled(true)
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.setNotificationsEnabled(true)
                                    }
                                },
                                onClick = {}
                            )
                            HorizontalDivider(color = SurfaceVariantDark)
                            ProfileMenuItemRow(
                                ProfileMenuItem(Icons.Default.Security, "Безпека та пароль"),
                                onClick = onNavigateToSecurity
                            )
                            HorizontalDivider(color = SurfaceVariantDark)
                            ProfileMenuItemRow(
                                ProfileMenuItem(
                                    Icons.Default.GraphicEq,
                                    "Еквалайзер",
                                    if (EqController.isEqualizerEnabled()) "Увімкнено" else "Вимкнено"
                                ),
                                onClick = onNavigateToEqualizer
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // --- ІНШЕ ---
                    item { ProfileSectionHeader("Інше") }
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
                        ) {
                            ProfileMenuItemRow(
                                ProfileMenuItem(Icons.Default.Help, "Допомога та підтримка"),
                                onClick = onNavigateToHelp
                            )
                            HorizontalDivider(color = SurfaceVariantDark)
                            ProfileMenuItemRow(
                                ProfileMenuItem(
                                    Icons.Default.Logout,
                                    "Вийти",
                                    hasArrow = false,
                                    tint = Color(0xFFFF5252)
                                ),
                                onClick = { viewModel.logout(onLogout) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// КАСТОМНІ КОМПОНЕНТИ
// ==========================================

@Composable
fun NoBandActionsCard(
    instrument: String?,
    onCreateBand: () -> Unit,
    onNavigateToSearch: () -> Unit // <--- Новий параметр
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceVariantDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text("Ви ще не в гурті", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            text = instrument?.takeIf { it.isNotBlank() }?.let { "Ваш інструмент: $it" }
                ?: "Додайте інструмент у профілі, щоб гуртам було легше вас знайти",
            color = TextGray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onCreateBand,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = DarkBg)
            ) {
                Text("Створити гурт")
            }
            OutlinedButton(
                onClick = {
                    // Показуємо повідомлення користувачу
                    android.widget.Toast.makeText(
                        context,
                        "Знайдіть гурт через пошук та перейдіть на його сторінку для відгуку",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    // Перекидаємо на пошук
                    onNavigateToSearch()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Знайти гурт")
            }
        }
    }
}

@Composable
fun FollowedBandItem(band: BandDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(80.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceVariantDark)
                .border(1.dp, SurfaceVariantDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (band.avatarUrl != null || band.coverUrl != null)
                AsyncImage(
                    model = band.avatarUrl ?: band.coverUrl,
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            else Text(
                band.name.take(2).uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NeonPurple
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = band.name,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White
        )
    }
}

@Composable
fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = NeonPurple
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextGray)
    }
}

@Composable
fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
fun ProfileMenuItemRow(
    item: ProfileMenuItem,
    switchValue: Boolean = false,
    onSwitchChange: (Boolean) -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!item.hasSwitch) onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(item.tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, null, tint = item.tint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            item.subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = TextGray)
            }
        }
        if (item.hasSwitch) {
            Switch(
                checked = switchValue,
                onCheckedChange = onSwitchChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonPurple,
                    checkedTrackColor = NeonPurple.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextGray,
                    uncheckedTrackColor = SurfaceVariantDark
                )
            )
        } else if (item.hasArrow) {
            Icon(Icons.Default.ChevronRight, null, tint = TextGray, modifier = Modifier.size(24.dp))
        }
    }
}

private fun canPostNotifications(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}

// Правильний відмінок для лічильника треків в历史
private fun historyCountLabel(count: Int): String {
    return when {
        count == 0 -> "Ще не прослухано"
        count % 100 in 11..19 -> "$count треків"
        count % 10 == 1 -> "$count трек"
        count % 10 in 2..4 -> "$count треки"
        else -> "$count треків"
    }
}
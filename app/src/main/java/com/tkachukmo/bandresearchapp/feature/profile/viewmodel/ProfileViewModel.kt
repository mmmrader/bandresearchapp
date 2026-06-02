package com.tkachukmo.bandresearchapp.feature.profile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.FollowDto
import com.tkachukmo.bandresearchapp.data.remote.dto.HistoryDto
import com.tkachukmo.bandresearchapp.data.remote.dto.HistoryTrackUI
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistDto
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistTrackDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ProfileDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ApplicationInsertDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.data.remote.dto.VacancyDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.tkachukmo.bandresearchapp.data.remote.dto.HistoryInsertDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

// ==========================================
// СЕРІАЛІЗОВАНІ DTO ДЛЯ ЗАПИТІВ
// ==========================================

@Serializable
data class PlaylistInsertDto(
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("is_public") val isPublic: Boolean = false
)

@Serializable
data class PlaylistTrackInsertDto(
    @SerialName("playlist_id") val playlistId: String,
    @SerialName("track_id") val trackId: String,
    val position: Int
)

@Serializable
data class AvatarUpsertDto(
    val id: String,
    @SerialName("avatar_url") val avatarUrl: String
)

@Serializable
data class GenreUpsertDto(
    val id: String,
    @SerialName("music_genres") val musicGenres: List<String>
)

// ==========================================
// UI STATE ДЛЯ ДЕТАЛЕЙ ПЛЕЙЛИСТА
// ==========================================

data class PlaylistDetailTrack(
    val trackId: String,
    val playlistId: String,
    val position: Int,
    val title: String,
    val bandName: String,
    val coverUrl: String?,
    val durationSec: Int,
    val audioUrl: String?
)

// ==========================================
// VIEWMODEL
// ==========================================

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @ApplicationContext private val context: Context // <--- ДОДАНО ДЛЯ ПАМ'ЯТІ
) : ViewModel() {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    // ── Кеш профілю ─────────────────────────────────────────────
    private fun saveProfileCache(profile: ProfileDto) {
        runCatching { prefs.edit().putString("cached_profile", json.encodeToString(ProfileDto.serializer(), profile)).apply() }
    }

    private fun loadProfileCache(): ProfileDto? {
        val raw = prefs.getString("cached_profile", null) ?: return null
        return runCatching { json.decodeFromString(ProfileDto.serializer(), raw) }.getOrNull()
    }

    private fun saveEmailCache(email: String) {
        prefs.edit().putString("cached_email", email).apply()
    }

    private fun loadEmailCache(): String? = prefs.getString("cached_email", null)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>("")
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    // Відновлюємо збережений стан з пам'яті
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", false))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _profile = MutableStateFlow<ProfileDto?>(null)
    val profile: StateFlow<ProfileDto?> = _profile.asStateFlow()

    private val _followedBands = MutableStateFlow<List<BandDto>>(emptyList())
    val followedBands: StateFlow<List<BandDto>> = _followedBands.asStateFlow()

    private val _playlists = MutableStateFlow<List<PlaylistDto>>(emptyList())
    val playlists: StateFlow<List<PlaylistDto>> = _playlists.asStateFlow()

    private val _listeningHistory = MutableStateFlow<List<HistoryTrackUI>>(emptyList())
    val listeningHistory: StateFlow<List<HistoryTrackUI>> = _listeningHistory.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _playlistDetailTracks = MutableStateFlow<List<PlaylistDetailTrack>>(emptyList())
    val playlistDetailTracks: StateFlow<List<PlaylistDetailTrack>> = _playlistDetailTracks.asStateFlow()

    private val _isPlaylistLoading = MutableStateFlow(false)
    val isPlaylistLoading: StateFlow<Boolean> = _isPlaylistLoading.asStateFlow()

    private val _availableTracks = MutableStateFlow<List<TrackDto>>(emptyList())
    val availableTracks: StateFlow<List<TrackDto>> = _availableTracks.asStateFlow()

    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()

    private val _userBand = MutableStateFlow<BandDto?>(null)
    val userBand: StateFlow<BandDto?> = _userBand.asStateFlow()

    private val _matchingVacancies = MutableStateFlow<List<VacancyDto>>(emptyList())
    val matchingVacancies: StateFlow<List<VacancyDto>> = _matchingVacancies.asStateFlow()

    init {
        // Одразу показуємо кешовані дані (якщо є) — без очікування мережі
        _profile.value = loadProfileCache()
        _userEmail.value = loadEmailCache()
        loadUserProfile()
    }

    fun clearError() { _errorMessage.value = null }
    fun clearPasswordState() { _passwordChangeState.value = PasswordChangeState.Idle }

    private fun handleException(t: Throwable, defaultMessage: String) {
        t.printStackTrace()
        _errorMessage.value = "$defaultMessage: ${t.message ?: t.javaClass.simpleName}"
    }

    // ==========================================
    // ЗАВАНТАЖЕННЯ ПРОФІЛЮ
    // ==========================================

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                val userId = currentUser?.id ?: return@launch
                _userEmail.value = currentUser.email
                currentUser.email?.let { saveEmailCache(it) }

                val profile = supabaseClient.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<ProfileDto>()
                _profile.value = profile
                profile?.let { saveProfileCache(it) }

                val follows = supabaseClient.postgrest["follows"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<FollowDto>()

                if (follows.isNotEmpty()) {
                    val bandIds = follows.map { it.bandId }
                    _followedBands.value = supabaseClient.postgrest["bands"]
                        .select { filter { isIn("id", bandIds) } }
                        .decodeList<BandDto>()
                } else {
                    _followedBands.value = emptyList()
                }

                _userBand.value = try {
                    supabaseClient.postgrest["bands"]
                        .select { filter { eq("manager_id", userId) } }
                        .decodeSingleOrNull<BandDto>()
                } catch (_: Exception) { null }

                val instrument = _profile.value?.instrument
                if (_userBand.value == null && !instrument.isNullOrBlank()) {
                    loadMatchingVacancies(instrument)
                }

                loadPlaylists()
                loadHistoryData(userId)

            } catch (t: Throwable) {
                handleException(t, "Помилка отримання даних")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMatchingVacancies(instrument: String) {
        viewModelScope.launch {
            try {
                _matchingVacancies.value = supabaseClient.postgrest["vacancies"]
                    .select { filter { eq("is_active", true); ilike("instrument", "%$instrument%") } }
                    .decodeList<VacancyDto>()
            } catch (_: Exception) {
                _matchingVacancies.value = emptyList()
            }
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                _playlists.value = supabaseClient.postgrest["playlists"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<PlaylistDto>()
            } catch (e: Exception) {
                handleException(e, "Помилка завантаження плейлистів")
            }
        }
    }

    fun loadPlaylistDetails(playlistId: String) {
        viewModelScope.launch {
            _isPlaylistLoading.value = true
            try {
                val rawTracks = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", playlistId) } }
                    .decodeList<PlaylistTrackDto>()

                if (rawTracks.isEmpty()) { _playlistDetailTracks.value = emptyList(); return@launch }

                val trackIds = rawTracks.map { it.trackId }.distinct()
                val tracks = supabaseClient.postgrest["tracks"]
                    .select { filter { isIn("id", trackIds) } }
                    .decodeList<TrackDto>()
                val bandIds = tracks.map { it.bandId }.distinct()
                val bands = supabaseClient.postgrest["bands"]
                    .select { filter { isIn("id", bandIds) } }
                    .decodeList<BandDto>()

                _playlistDetailTracks.value = rawTracks.sortedBy { it.position }.mapNotNull { pt ->
                    val track = tracks.find { it.id == pt.trackId } ?: return@mapNotNull null
                    val band = bands.find { it.id == track.bandId }
                    PlaylistDetailTrack(
                        trackId = track.id, playlistId = pt.playlistId, position = pt.position,
                        title = track.title, bandName = band?.name ?: "Невідомий виконавець",
                        coverUrl = track.coverUrl, durationSec = track.durationSec, audioUrl = track.audioUrl
                    )
                }
            } catch (t: Throwable) {
                handleException(t, "Помилка завантаження треків плейлиста")
            } finally {
                _isPlaylistLoading.value = false
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["playlist_tracks"].delete {
                    filter { eq("playlist_id", playlistId); eq("track_id", trackId) }
                }
                loadPlaylistDetails(playlistId)
                _errorMessage.value = "Трек видалено з плейлиста"
            } catch (t: Throwable) { handleException(t, "Помилка видалення треку") }
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            try {
                val existing = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", playlistId); eq("track_id", trackId) } }
                    .decodeList<PlaylistTrackDto>()
                if (existing.isNotEmpty()) { _errorMessage.value = "Цей трек вже є в плейлисті"; return@launch }

                val currentTracks = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", playlistId) } }
                    .decodeList<PlaylistTrackDto>()
                val nextPosition = (currentTracks.maxOfOrNull { it.position } ?: 0) + 1

                supabaseClient.postgrest["playlist_tracks"].insert(
                    PlaylistTrackInsertDto(playlistId = playlistId, trackId = trackId, position = nextPosition)
                )
                loadPlaylistDetails(playlistId)
                _errorMessage.value = "Трек додано до плейлиста!"
            } catch (t: Throwable) { handleException(t, "Помилка додавання треку") }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["playlist_tracks"].delete { filter { eq("playlist_id", playlistId) } }
                supabaseClient.postgrest["playlists"].delete { filter { eq("id", playlistId) } }
                loadPlaylists()
                _errorMessage.value = "Плейлист видалено"
            } catch (t: Throwable) { handleException(t, "Помилка видалення плейлиста") }
        }
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        if (newName.isBlank()) { _errorMessage.value = "Назва не може бути порожньою"; return }
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["playlists"].update({ set("name", newName) }) {
                    filter { eq("id", playlistId) }
                }
                loadPlaylists()
                _errorMessage.value = "Плейлист перейменовано"
            } catch (t: Throwable) { handleException(t, "Помилка перейменування") }
        }
    }

    fun loadAvailableTracks(query: String = "") {
        viewModelScope.launch {
            try {
                val existingTrackIds = _playlistDetailTracks.value.map { it.trackId }.toSet()
                val allTracks = if (query.isBlank()) {
                    supabaseClient.postgrest["tracks"]
                        .select()
                        .decodeList<TrackDto>()
                        .take(100)
                } else {
                    supabaseClient.postgrest["tracks"]
                        .select { filter { ilike("title", "%$query%") } }
                        .decodeList<TrackDto>()
                }
                _availableTracks.value = allTracks.filter { it.id !in existingTrackIds }

            } catch (t: Throwable) {
                handleException(t, "Помилка пошуку треків")
            }
        }
    }

    fun recordListeningHistory(trackId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                val currentTime = java.time.Instant.now().toString()
                supabaseClient.postgrest["listen_history"].insert(
                    HistoryInsertDto(
                        userId = userId,
                        trackId = trackId,
                        listenedAt = currentTime
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reorderTracks(playlistId: String, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                val tracks = _playlistDetailTracks.value.toMutableList()
                if (fromIndex < 0 || fromIndex >= tracks.size || toIndex < 0 || toIndex >= tracks.size) return@launch
                val moved = tracks.removeAt(fromIndex)
                tracks.add(toIndex, moved)
                tracks.forEachIndexed { index, track ->
                    supabaseClient.postgrest["playlist_tracks"].update({ set("position", index + 1) }) {
                        filter { eq("playlist_id", playlistId); eq("track_id", track.trackId) }
                    }
                }
                _playlistDetailTracks.value = tracks.mapIndexed { index, t -> t.copy(position = index + 1) }
            } catch (t: Throwable) { handleException(t, "Помилка зміни порядку") }
        }
    }

    // ==========================================
    // СПОВІЩЕННЯ ТА ВАКАНСІЇ
    // ==========================================

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        // Зберігаємо вибір користувача у пам'ять
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun applyForVacancy(vacancyId: String) {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                val newApp = ApplicationInsertDto(
                    vacancyId = vacancyId,
                    userId = userId,
                    message = "Відгук з екрану профілю"
                )
                supabaseClient.postgrest["applications"].insert(newApp)
                _errorMessage.value = "Заявку успішно надіслано!"
            } catch (t: Throwable) {
                handleException(t, "Не вдалося відправити заявку")
            }
        }
    }

    // ==========================================
    // ІСТОРІЯ
    // ==========================================

    private suspend fun loadHistoryData(userId: String) {
        try {
            val rawHistory = supabaseClient.postgrest["listen_history"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<HistoryDto>()
                .sortedByDescending { it.listenedAt }

            if (rawHistory.isNotEmpty()) {
                val trackIds = rawHistory.map { it.trackId }.distinct()
                val tracks = supabaseClient.postgrest["tracks"]
                    .select { filter { isIn("id", trackIds) } }
                    .decodeList<TrackDto>()

                // ФІКС: Відфільтровуємо пусті bandId, щоб Supabase не крашився
                val bandIds = tracks.map { it.bandId }.filter { it.isNotBlank() }.distinct()

                val bands = if (bandIds.isNotEmpty()) {
                    supabaseClient.postgrest["bands"]
                        .select { filter { isIn("id", bandIds) } }
                        .decodeList<BandDto>()
                } else emptyList()

                _listeningHistory.value = rawHistory.mapNotNull { hist ->
                    val track = tracks.find { it.id == hist.trackId } ?: return@mapNotNull null
                    val band = bands.find { it.id == track.bandId }
                    HistoryTrackUI(
                        historyId = hist.id ?: UUID.randomUUID().toString(),
                        trackId = track.id,
                        trackTitle = track.title,
                        bandName = band?.name ?: "Невідомий виконавець",
                        coverUrl = track.coverUrl,
                        listenedAt = hist.listenedAt,
                        durationSec = track.durationSec
                    )
                }
            } else {
                _listeningHistory.value = emptyList()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ==========================================
    // ОНОВЛЕННЯ ПРОФІЛЮ (розширене)
    // ==========================================

    fun updateProfileInfo(
        newName: String,
        newBio: String,
        newSocialLink: String,
        newGenres: List<String>,
        newInstrument: String = "",
        newExperience: String = "",
        newLocation: String = "",
        newYoutubeLink: String = "",
        newAudioLink: String = ""
    ) {
        if (newName.isBlank()) { _errorMessage.value = "Будь ласка, введіть нікнейм"; return }
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["profiles"].update(
                    {
                        set("display_name", newName)
                        set("bio", newBio)
                        set("social_link", newSocialLink)
                        set("music_genres", newGenres)
                        set("instrument", newInstrument.takeIf { it.isNotBlank() })
                        set("experience", newExperience.takeIf { it.isNotBlank() })
                        set("location", newLocation.takeIf { it.isNotBlank() })
                        set("youtube_link", newYoutubeLink.takeIf { it.isNotBlank() })
                        set("audio_link", newAudioLink.takeIf { it.isNotBlank() })
                    }
                ) { filter { eq("id", userId) } }

                _profile.value = _profile.value?.copy(
                    displayName = newName, bio = newBio, socialLink = newSocialLink,
                    musicGenres = newGenres, instrument = newInstrument.takeIf { it.isNotBlank() },
                    experience = newExperience.takeIf { it.isNotBlank() },
                    location = newLocation.takeIf { it.isNotBlank() },
                    youtubeLink = newYoutubeLink.takeIf { it.isNotBlank() },
                    audioLink = newAudioLink.takeIf { it.isNotBlank() }
                )
                _errorMessage.value = "Профіль успішно оновлено!"
            } catch (e: Exception) {
                _errorMessage.value = "Помилка збереження: ${e.message}"
            }
        }
    }

    // ==========================================
    // СТВОРЕННЯ ПЛЕЙЛИСТА
    // ==========================================

    fun createPlaylist(name: String) {
        if (name.isBlank()) { _errorMessage.value = "Назва плейлісту не може бути порожньою"; return }
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["playlists"].insert(
                    PlaylistInsertDto(userId = userId, name = name, isPublic = false)
                )
                loadPlaylists()
                _errorMessage.value = "Плейліст успішно створено!"
            } catch (e: Exception) { handleException(e, "Помилка створення плейлиста") }
        }
    }

    // ==========================================
    // ЗМІНА ПАРОЛЯ З ПЕРЕВІРКОЮ СТАРОГО
    // ==========================================

    fun updatePassword(oldPassword: String, newPassword: String) {
        if (newPassword.length < 8) { _passwordChangeState.value = PasswordChangeState.Error("Пароль має бути не менше 8 символів"); return }
        if (!newPassword.any { it.isDigit() }) { _passwordChangeState.value = PasswordChangeState.Error("Пароль має містити хоча б одну цифру"); return }
        if (!newPassword.any { !it.isLetterOrDigit() }) { _passwordChangeState.value = PasswordChangeState.Error("Пароль має містити спеціальний символ"); return }

        viewModelScope.launch {
            _passwordChangeState.value = PasswordChangeState.Loading
            try {
                val email = supabaseClient.auth.currentUserOrNull()?.email
                    ?: run { _passwordChangeState.value = PasswordChangeState.Error("Не вдалося отримати дані користувача"); return@launch }
                try {
                    supabaseClient.auth.signInWith(Email) { this.email = email; this.password = oldPassword }
                } catch (e: Exception) {
                    _passwordChangeState.value = PasswordChangeState.Error("Старий пароль невірний")
                    return@launch
                }
                supabaseClient.auth.updateUser { password = newPassword }
                _passwordChangeState.value = PasswordChangeState.Success
            } catch (t: Throwable) {
                _passwordChangeState.value = PasswordChangeState.Error("Помилка зміни пароля: ${t.message ?: t.javaClass.simpleName}")
            }
        }
    }

    // ==========================================
    // АВАТАРКА
    // ==========================================

    fun uploadAvatar(context: Context, uri: Uri) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
                val fileName = "avatar_${userId}_${UUID.randomUUID()}.jpg"
                supabaseClient.storage["images"].upload(fileName, bytes)
                val imageUrl = supabaseClient.storage["images"].publicUrl(fileName)
                supabaseClient.postgrest["profiles"].upsert(AvatarUpsertDto(id = userId, avatarUrl = imageUrl))
                loadUserProfile()
            } catch (t: Throwable) { handleException(t, "Помилка завантаження фото") }
        }
    }

    // ==========================================
    // ЖАНРИ
    // ==========================================

    fun toggleGenre(genre: String) {
        val currentProfile = _profile.value ?: return
        val updatedGenres = (currentProfile.musicGenres ?: emptyList()).toMutableList().apply {
            if (contains(genre)) remove(genre) else add(genre)
        }
        _profile.value = currentProfile.copy(musicGenres = updatedGenres)
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["profiles"].upsert(GenreUpsertDto(id = currentProfile.id, musicGenres = updatedGenres))
            } catch (t: Throwable) { handleException(t, "Не вдалося зберегти стиль") }
        }
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try { supabaseClient.auth.signOut(); onSuccess() } catch (_: Exception) {}
        }
    }
}

// ==========================================
// СТАН ЗМІНИ ПАРОЛЯ
// ==========================================

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    object Success : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}
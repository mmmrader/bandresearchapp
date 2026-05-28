package com.tkachukmo.bandresearchapp.feature.profile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.core.player.AudioController
import com.tkachukmo.bandresearchapp.core.notifications.NotificationSettingsRepository
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.FollowDto
import com.tkachukmo.bandresearchapp.data.remote.dto.HistoryDto
import com.tkachukmo.bandresearchapp.data.remote.dto.HistoryTrackUI
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistDto
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistTrackDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ProfileDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.data.remote.dto.VacancyDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ApplicationDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ApplicationInsertDto
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.Serializable

// ==========================================
// DTO ДЛЯ ВСТАВКИ ДАНИХ (БЕЗ MAPOF)
// ==========================================

@Serializable
data class ProfileUpdateDto(
    val display_name: String,
    val bio: String
)

@Serializable
data class PlaylistInsertDto(
    val user_id: String,
    val name: String,
    val is_public: Boolean = false
)

@Serializable
data class PlaylistTrackInsertDto(
    val playlist_id: String,
    val track_id: String,
    val position: Int
)

// ==========================================
// UI STATE для деталей плейлиста
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
    private val audioController: AudioController,
    private val notificationSettingsRepository: NotificationSettingsRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>("")
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _profile = MutableStateFlow<ProfileDto?>(null)
    val profile: StateFlow<ProfileDto?> = _profile.asStateFlow()

    private val _followedBands = MutableStateFlow<List<BandDto>>(emptyList())
    val followedBands: StateFlow<List<BandDto>> = _followedBands.asStateFlow()

    private val _managedBand = MutableStateFlow<BandDto?>(null)
    val managedBand: StateFlow<BandDto?> = _managedBand.asStateFlow()

    private val _matchingVacancies = MutableStateFlow<List<VacancyDto>>(emptyList())
    val matchingVacancies: StateFlow<List<VacancyDto>> = _matchingVacancies.asStateFlow()

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

    val notificationsEnabled: StateFlow<Boolean> = notificationSettingsRepository.enabled

    init {
        loadUserProfile()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearPasswordState() {
        _passwordChangeState.value = PasswordChangeState.Idle
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationSettingsRepository.setEnabled(enabled)
    }

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

                _profile.value = supabaseClient.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<ProfileDto>()

                _managedBand.value = supabaseClient.postgrest["bands"]
                    .select { filter { eq("manager_id", userId) } }
                    .decodeSingleOrNull<BandDto>()

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

                loadPlaylists()
                loadHistoryData(userId)
                loadMatchingVacancies()

            } catch (t: Throwable) {
                handleException(t, "Помилка отримання даних")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==========================================
    // ПЛЕЙЛИСТИ — ЗАВАНТАЖЕННЯ
    // ==========================================

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

    // ==========================================
    // ПЛЕЙЛИСТИ — ДЕТАЛІ (треки з назвами виконавців)
    // ==========================================

    fun loadPlaylistDetails(playlistId: String) {
        viewModelScope.launch {
            _isPlaylistLoading.value = true
            try {
                val rawTracks = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", playlistId) } }
                    .decodeList<PlaylistTrackDto>()

                if (rawTracks.isEmpty()) {
                    _playlistDetailTracks.value = emptyList()
                    return@launch
                }

                val trackIds = rawTracks.map { it.trackId }.distinct()

                val tracks = supabaseClient.postgrest["tracks"]
                    .select { filter { isIn("id", trackIds) } }
                    .decodeList<TrackDto>()

                val bandIds = tracks.map { it.bandId }.distinct()

                val bands = supabaseClient.postgrest["bands"]
                    .select { filter { isIn("id", bandIds) } }
                    .decodeList<BandDto>()

                _playlistDetailTracks.value = rawTracks
                    .sortedBy { it.position }
                    .mapNotNull { pt ->
                        val track = tracks.find { it.id == pt.trackId } ?: return@mapNotNull null
                        val band = bands.find { it.id == track.bandId }
                        PlaylistDetailTrack(
                            trackId = track.id,
                            playlistId = pt.playlistId,
                            position = pt.position,
                            title = track.title,
                            bandName = band?.name ?: "Невідомий виконавець",
                            coverUrl = track.coverUrl,
                            durationSec = track.durationSec,
                            audioUrl = track.audioUrl
                        )
                    }

            } catch (t: Throwable) {
                handleException(t, "Помилка завантаження треків плейлиста")
            } finally {
                _isPlaylistLoading.value = false
            }
        }
    }

    // ==========================================
    // ПЛЕЙЛИСТИ — ВИДАЛЕННЯ ТРЕКУ
    // ==========================================

    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["playlist_tracks"].delete {
                    filter {
                        eq("playlist_id", playlistId)
                        eq("track_id", trackId)
                    }
                }
                loadPlaylistDetails(playlistId)
                _errorMessage.value = "Трек видалено з плейлиста"
            } catch (t: Throwable) {
                handleException(t, "Помилка видалення треку")
            }
        }
    }

    // ==========================================
    // ПЛЕЙЛИСТИ — ДОДАВАННЯ ТРЕКУ
    // ==========================================

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            try {
                val existing = supabaseClient.postgrest["playlist_tracks"]
                    .select {
                        filter {
                            eq("playlist_id", playlistId)
                            eq("track_id", trackId)
                        }
                    }
                    .decodeList<PlaylistTrackDto>()

                if (existing.isNotEmpty()) {
                    _errorMessage.value = "Цей трек вже є в плейлисті"
                    return@launch
                }

                val currentTracks = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", playlistId) } }
                    .decodeList<PlaylistTrackDto>()

                val nextPosition = (currentTracks.maxOfOrNull { it.position } ?: 0) + 1

                // Використовуємо DTO замість mapOf
                val newTrackDto = PlaylistTrackInsertDto(
                    playlist_id = playlistId,
                    track_id = trackId,
                    position = nextPosition
                )

                supabaseClient.postgrest["playlist_tracks"].insert(newTrackDto)

                loadPlaylistDetails(playlistId)
                _errorMessage.value = "Трек додано до плейлиста!"

            } catch (t: Throwable) {
                handleException(t, "Помилка додавання треку")
            }
        }
    }

    // ==========================================
    // ПЛЕЙЛИСТИ — ВИДАЛЕННЯ ПЛЕЙЛИСТА
    // ==========================================

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["playlist_tracks"].delete {
                    filter { eq("playlist_id", playlistId) }
                }
                supabaseClient.postgrest["playlists"].delete {
                    filter { eq("id", playlistId) }
                }
                loadPlaylists()
                _errorMessage.value = "Плейлист видалено"
            } catch (t: Throwable) {
                handleException(t, "Помилка видалення плейлиста")
            }
        }
    }

    // ==========================================
    // ПЛЕЙЛИСТИ — ПЕРЕЙМЕНУВАННЯ
    // ==========================================

    fun renamePlaylist(playlistId: String, newName: String) {
        if (newName.isBlank()) {
            _errorMessage.value = "Назва не може бути порожньою"
            return
        }
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["playlists"].update(
                    { set("name", newName) }
                ) {
                    filter { eq("id", playlistId) }
                }
                loadPlaylists()
                _errorMessage.value = "Плейлист перейменовано"
            } catch (t: Throwable) {
                handleException(t, "Помилка перейменування")
            }
        }
    }

    // ==========================================
    // ДОСТУПНІ ТРЕКИ ДЛЯ ПОШУКУ/ДОДАВАННЯ
    // ==========================================

    fun loadAvailableTracks(query: String = "") {
        viewModelScope.launch {
            try {
                _availableTracks.value = if (query.isBlank()) {
                    supabaseClient.postgrest["tracks"]
                        .select()
                        .decodeList<TrackDto>()
                        .take(50)
                } else {
                    supabaseClient.postgrest["tracks"]
                        .select {
                            filter { ilike("title", "%$query%") }
                        }
                        .decodeList<TrackDto>()
                }
            } catch (t: Throwable) {
                handleException(t, "Помилка пошуку треків")
            }
        }
    }

    // ==========================================
    // ЗАВАНТАЖЕННЯ ТІЛЬКИ ВПОДОБАНИХ ТА ВІДСУТНІХ ТРЕКІВ
    // ==========================================
    fun loadLikedTracksForPlaylist(playlistId: String, query: String = "") {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch

                // 1. Шукаємо плейлист "Улюблені"
                val favPlaylist = supabaseClient.postgrest["playlists"]
                    .select { filter { eq("user_id", userId); eq("name", "Улюблені") } }
                    .decodeSingleOrNull<PlaylistDto>()

                if (favPlaylist == null) {
                    _availableTracks.value = emptyList()
                    return@launch
                }

                // 2. Отримуємо ID всіх вподобаних треків
                val likedTracks = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", favPlaylist.id) } }
                    .decodeList<PlaylistTrackDto>()

                val likedIds = likedTracks.map { it.trackId }

                // 3. Отримуємо ID треків, які ВЖЕ Є в поточному плейлисті
                val currentTracks = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", playlistId) } }
                    .decodeList<PlaylistTrackDto>()

                val currentIds = currentTracks.map { it.trackId }

                // 4. Фільтруємо: залишаємо вподобані, яких ЩЕ НЕМАЄ в поточному
                val availableIds = likedIds.filterNot { it in currentIds }

                if (availableIds.isEmpty()) {
                    _availableTracks.value = emptyList()
                    return@launch
                }

                // 5. Завантажуємо самі треки з урахуванням пошукового запиту
                _availableTracks.value = supabaseClient.postgrest["tracks"]
                    .select {
                        filter {
                            isIn("id", availableIds)
                            if (query.isNotBlank()) {
                                ilike("title", "%$query%")
                            }
                        }
                    }
                    .decodeList<TrackDto>()

            } catch (t: Throwable) {
                handleException(t, "Помилка пошуку треків")
            }
        }
    }
    // ==========================================
    // ЗМІНА ПОРЯДКУ ТРЕКІВ У ПЛЕЙЛИСТІ
    // ==========================================

    fun reorderTracks(playlistId: String, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                val tracks = _playlistDetailTracks.value.toMutableList()
                if (fromIndex < 0 || fromIndex >= tracks.size || toIndex < 0 || toIndex >= tracks.size) return@launch

                val moved = tracks.removeAt(fromIndex)
                tracks.add(toIndex, moved)

                tracks.forEachIndexed { index, track ->
                    supabaseClient.postgrest["playlist_tracks"].update(
                        { set("position", index + 1) }
                    ) {
                        filter {
                            eq("playlist_id", playlistId)
                            eq("track_id", track.trackId)
                        }
                    }
                }

                _playlistDetailTracks.value = tracks.mapIndexed { index, t ->
                    t.copy(position = index + 1)
                }

            } catch (t: Throwable) {
                handleException(t, "Помилка зміни порядку")
            }
        }
    }

    // ==========================================
    // ІСТОРІЯ ПРОСЛУХОВУВАНЬ
    // ==========================================

    private suspend fun loadHistoryData(userId: String) {
        try {
            val rawHistory = supabaseClient.postgrest["history"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<HistoryDto>()
                .sortedByDescending { it.listenedAt }

            if (rawHistory.isNotEmpty()) {
                val trackIds = rawHistory.map { it.trackId }.distinct()
                val tracks = supabaseClient.postgrest["tracks"]
                    .select { filter { isIn("id", trackIds) } }
                    .decodeList<TrackDto>()
                val bandIds = tracks.map { it.bandId }.distinct()
                val bands = supabaseClient.postgrest["bands"]
                    .select { filter { isIn("id", bandIds) } }
                    .decodeList<BandDto>()

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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // ОНОВЛЕННЯ ПРОФІЛЮ
    // ==========================================

    fun updateProfileInfo(
        newName: String,
        newBio: String,
        newSocialLink: String,
        newGenres: List<String>,
        newInstrument: String, // ДОДАНО
        newExperience: String, // ДОДАНО
        newLocation: String,   // ДОДАНО
        newYoutubeLink: String,// ДОДАНО
        newAudioLink: String   // ДОДАНО
    ) {
        if (newName.isBlank()) {
            _errorMessage.value = "Будь ласка, введіть нікнейм"
            return
        }
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["profiles"]
                    .update(
                        {
                            set("display_name", newName)
                            set("bio", newBio)
                            set("social_link", newSocialLink)
                            set("music_genres", newGenres)
                            set("instrument", newInstrument)
                            set("experience", newExperience)
                            set("location", newLocation)
                            set("youtube_link", newYoutubeLink)
                            set("audio_link", newAudioLink)
                        }
                    ) {
                        filter { eq("id", userId) }
                    }

                val currentProfile = _profile.value
                if (currentProfile != null) {
                    _profile.value = currentProfile.copy(
                        displayName = newName,
                        bio = newBio,
                        socialLink = newSocialLink,
                        musicGenres = newGenres,
                        instrument = newInstrument,
                        experience = newExperience,
                        location = newLocation,
                        youtubeLink = newYoutubeLink,
                        audioLink = newAudioLink
                    )
                }

                _errorMessage.value = "Профіль успішно оновлено!"

            } catch (e: Exception) {
                _errorMessage.value = "Помилка збереження: ${e.message}"
            }
        }
    }

    fun removeHistoryItem(historyId: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["history"].delete {
                    filter { eq("id", historyId) }
                }
                _listeningHistory.value = _listeningHistory.value.filterNot { it.historyId == historyId }
                _errorMessage.value = "Запис видалено з історії"
            } catch (t: Throwable) {
                handleException(t, "Помилка видалення запису")
            }
        }
    }

    fun clearListeningHistory() {
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            try {
                supabaseClient.postgrest["history"].delete {
                    filter { eq("user_id", userId) }
                }
                _listeningHistory.value = emptyList()
                _errorMessage.value = "Історію очищено"
            } catch (t: Throwable) {
                handleException(t, "Помилка очищення історії")
            }
        }
    }

    fun loadMatchingVacancies() {
        viewModelScope.launch {
            try {
                val instrument = _profile.value?.instrument?.trim().orEmpty()
                _matchingVacancies.value = if (instrument.isBlank()) {
                    supabaseClient.postgrest["vacancies"]
                        .select { filter { eq("is_active", true) } }
                        .decodeList<VacancyDto>()
                } else {
                    supabaseClient.postgrest["vacancies"]
                        .select { filter { eq("is_active", true); ilike("instrument", "%$instrument%") } }
                        .decodeList<VacancyDto>()
                }
            } catch (_: Throwable) {
                _matchingVacancies.value = emptyList()
            }
        }
    }

    fun applyForVacancy(vacancyId: String, message: String = "") {
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            try {
                val existing = supabaseClient.postgrest["applications"]
                    .select { filter { eq("vacancy_id", vacancyId); eq("user_id", userId) } }
                    .decodeList<ApplicationDto>()
                if (existing.isNotEmpty()) {
                    _errorMessage.value = "Ви вже відгукнулися на цю вакансію"
                    return@launch
                }
                supabaseClient.postgrest["applications"].insert(
                    ApplicationInsertDto(
                        vacancyId = vacancyId,
                        userId = userId,
                        message = message.takeIf { it.isNotBlank() }
                    )
                )
                _errorMessage.value = "Заявку надіслано"
            } catch (t: Throwable) {
                handleException(t, "Помилка відправки заявки")
            }
        }
    }

    // ==========================================
    // СТВОРЕННЯ ПЛЕЙЛИСТА
    // ==========================================

    fun createPlaylist(name: String) {
        if (name.isBlank()) {
            _errorMessage.value = "Назва плейлісту не може бути порожньою"
            return
        }
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch

                // Використовуємо DTO замість mapOf
                val newPlaylistDto = PlaylistInsertDto(
                    user_id = userId,
                    name = name,
                    is_public = false
                )

                supabaseClient.postgrest["playlists"].insert(newPlaylistDto)
                loadPlaylists()
                _errorMessage.value = "Плейліст успішно створено!"

            } catch (e: Exception) {
                handleException(e, "Помилка створення плейлиста")
            }
        }
    }

    // ==========================================
    // ПІДГОТОВКА ПЛЕЙЛИСТА ДО ВІДТВОРЕННЯ
    // ==========================================
    fun preparePlaylistForPlayback(playlistName: String, tracks: List<PlaylistDetailTrack>, startIndex: Int) {
        // Перетворюємо UI-модель треків плейлиста у звичайні TrackDto
        val queue = tracks.map {
            TrackDto(
                id = it.trackId,
                title = it.title,
                audioUrl = it.audioUrl ?: "",
                coverUrl = it.coverUrl,
                durationSec = it.durationSec,
                bandId = "" // Якщо в TrackDto обов'язково треба bandId, залиш як є
            )
        }

        // Передаємо всю чергу в контролер!
        audioController.playQueue(queue, startIndex, playlistName)
    }

    // ==========================================
    // ЗМІНА ПАРОЛЮ
    // ==========================================

    fun updatePassword(oldPassword: String, newPassword: String) {
        if (newPassword.length < 8) {
            _passwordChangeState.value = PasswordChangeState.Error("Пароль має бути не менше 8 символів")
            return
        }
        if (!newPassword.any { it.isDigit() }) {
            _passwordChangeState.value = PasswordChangeState.Error("Пароль має містити хоча б одну цифру")
            return
        }
        if (!newPassword.any { !it.isLetterOrDigit() }) {
            _passwordChangeState.value = PasswordChangeState.Error("Пароль має містити спеціальний символ")
            return
        }

        viewModelScope.launch {
            _passwordChangeState.value = PasswordChangeState.Loading
            try {
                val email = supabaseClient.auth.currentUserOrNull()?.email
                    ?: run {
                        _passwordChangeState.value = PasswordChangeState.Error("Не вдалося отримати дані користувача")
                        return@launch
                    }

                try {
                    supabaseClient.auth.signInWith(Email) {
                        this.email = email
                        this.password = oldPassword
                    }
                } catch (e: Exception) {
                    _passwordChangeState.value = PasswordChangeState.Error("Старий пароль невірний")
                    return@launch
                }

                supabaseClient.auth.updateUser {
                    password = newPassword
                }

                _passwordChangeState.value = PasswordChangeState.Success

            } catch (t: Throwable) {
                _passwordChangeState.value = PasswordChangeState.Error(
                    "Помилка зміни пароля: ${t.message ?: t.javaClass.simpleName}"
                )
            }
        }
    }

    // ==========================================
    // ЗАВАНТАЖЕННЯ АВАТАРКИ
    // ==========================================

    fun uploadAvatar(context: Context, uri: Uri) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@launch

                val fileName = "avatar_${userId}_${UUID.randomUUID()}.jpg"

                supabaseClient.storage["images"].upload(fileName, bytes)

                val imageUrl = supabaseClient.storage["images"].publicUrl(fileName)

                // Використовуємо update DSL замість upsert(mapOf(...))
                supabaseClient.postgrest["profiles"].update(
                    { set("avatar_url", imageUrl) }
                ) {
                    filter { eq("id", userId) }
                }

                loadUserProfile()

            } catch (t: Throwable) {
                handleException(t, "Помилка завантаження фото")
            }
        }
    }

    // ==========================================
    // ЖАНРИ
    // ==========================================

    fun toggleGenre(genre: String) {
        val currentProfile = _profile.value ?: return
        val userId = currentProfile.id

        val updatedGenres = (currentProfile.musicGenres ?: emptyList())
            .toMutableList()
            .apply {
                if (contains(genre)) {
                    remove(genre)
                } else {
                    add(genre)
                }
            }

        _profile.value = currentProfile.copy(musicGenres = updatedGenres)

        viewModelScope.launch {
            try {
                // Використовуємо update DSL замість upsert(mapOf(...))
                supabaseClient.postgrest["profiles"].update(
                    { set("music_genres", updatedGenres) }
                ) {
                    filter { eq("id", userId) }
                }
            } catch (t: Throwable) {
                handleException(t, "Не вдалося зберегти стиль")
            }
        }
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
                onSuccess()
            } catch (_: Exception) {
            }
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

package com.tkachukmo.bandresearchapp.feature.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.core.player.AudioController
import com.tkachukmo.bandresearchapp.data.remote.dto.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BandDetailViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient,
    val audioController: AudioController
) : ViewModel() {

    private val _userApplications = MutableStateFlow<List<ApplicationDto>>(emptyList())
    val userApplications: StateFlow<List<ApplicationDto>> = _userApplications.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _band = MutableStateFlow<BandDto?>(null)
    val band: StateFlow<BandDto?> = _band.asStateFlow()

    private val _tracks = MutableStateFlow<List<TrackDto>>(emptyList())
    val tracks: StateFlow<List<TrackDto>> = _tracks.asStateFlow()

    private val _videos = MutableStateFlow<List<VideoDto>>(emptyList())
    val videos: StateFlow<List<VideoDto>> = _videos.asStateFlow()

    private val _releases = MutableStateFlow<List<ReleaseDto>>(emptyList())
    val releases: StateFlow<List<ReleaseDto>> = _releases.asStateFlow()

    private val _events = MutableStateFlow<List<BandEventDto>>(emptyList())
    val events: StateFlow<List<BandEventDto>> = _events.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _playlists = MutableStateFlow<List<PlaylistDto>>(emptyList())
    val playlists: StateFlow<List<PlaylistDto>> = _playlists.asStateFlow()

    private val _vacancies = MutableStateFlow<List<VacancyDto>>(emptyList())
    val vacancies: StateFlow<List<VacancyDto>> = _vacancies.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    // ==========================================
    // ЗАЯВКИ КОРИСТУВАЧА
    // ==========================================

    fun loadUserApplications() {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                _userApplications.value = supabaseClient.postgrest["applications"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<ApplicationDto>()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // ЗАВАНТАЖЕННЯ ДЕТАЛЕЙ ГУРТУ
    // ==========================================

    fun loadBandDetails(bandId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            clearMessages()

            try {
                val currentBand = supabaseClient.postgrest["bands"]
                    .select { filter { eq("id", bandId) } }
                    .decodeSingleOrNull<BandDto>()

                _band.value = currentBand

                if (currentBand != null) {

                    val bandTracks = supabaseClient.postgrest["tracks"]
                        .select { filter { eq("band_id", bandId) } }
                        .decodeList<TrackDto>()

                    _tracks.value = try {
                        bandTracks.withUniqueListenerCounts()
                    } catch (_: Throwable) {
                        bandTracks
                    }

                    _releases.value = try {
                        supabaseClient.postgrest["releases"]
                            .select { filter { eq("band_id", bandId) } }
                            .decodeList<ReleaseDto>()
                    } catch (_: Throwable) { emptyList() }

                    _videos.value = try {
                        supabaseClient.postgrest["videos"]
                            .select { filter { eq("band_id", bandId) } }
                            .decodeList<VideoDto>()
                    } catch (_: Throwable) { emptyList() }

                    _events.value = try {
                        supabaseClient.postgrest["band_events"]
                            .select { filter { eq("band_id", bandId) } }
                            .decodeList<BandEventDto>()
                            .sortedByDescending { it.createdAt ?: it.eventDate ?: "" }
                    } catch (_: Throwable) { emptyList() }

                    _vacancies.value = try {
                        supabaseClient.postgrest["vacancies"]
                            .select { filter { eq("band_id", bandId); eq("is_active", true) } }
                            .decodeList<VacancyDto>()
                    } catch (_: Throwable) { emptyList() }

                    loadUserApplications()
                    checkIfFollowing(bandId)
                }

            } catch (e: Exception) {
                _errorMessage.value = "Не вдалося завантажити сторінку гурту. Спробуйте ще раз."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==========================================
    // ПІДПИСКА
    // ==========================================

    private suspend fun checkIfFollowing(bandId: String) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        try {
            val follows = supabaseClient.postgrest["follows"]
                .select { filter { eq("user_id", userId); eq("band_id", bandId) } }
                .decodeList<FollowDto>()
            _isFollowing.value = follows.isNotEmpty()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun toggleFollow() {
        val bandId = _band.value?.id ?: return
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            if (userId == null) {
                _errorMessage.value = "Увійдіть в акаунт, щоб підписатися на гурт."
                return@launch
            }
            if (_band.value?.managerId == userId) {
                _errorMessage.value = "Ви створили цей гурт або є його учасником, тому не можете підписатися на нього."
                return@launch
            }
            try {
                if (_isFollowing.value) {
                    supabaseClient.postgrest["follows"].delete {
                        filter { eq("user_id", userId); eq("band_id", bandId) }
                    }
                    _isFollowing.value = false
                    _band.value = _band.value?.copy(
                        followersCount = maxOf(0, (_band.value?.followersCount ?: 0) - 1)
                    )
                } else {
                    supabaseClient.postgrest["follows"].insert(FollowDto(userId, bandId))
                    _isFollowing.value = true
                    _band.value = _band.value?.copy(
                        followersCount = (_band.value?.followersCount ?: 0) + 1
                    )
                }
                _band.value?.let { currentBand ->
                    supabaseClient.postgrest["bands"].update(
                        mapOf("followers_count" to currentBand.followersCount)
                    ) { filter { eq("id", currentBand.id) } }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Не вдалося змінити підписку. Перевірте інтернет і спробуйте ще раз."
            }
        }
    }

    // ==========================================
    // ПЛЕЙЛИСТИ
    // ==========================================

    fun loadUserPlaylists() {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                _playlists.value = supabaseClient.postgrest["playlists"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<PlaylistDto>()
            } catch (e: Exception) {
                _errorMessage.value = "Не вдалося завантажити ваші плейлісти."
            }
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            try {
                val existing = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", playlistId); eq("track_id", trackId) } }
                    .decodeList<PlaylistTrackDto>()

                if (existing.isNotEmpty()) {
                    _errorMessage.value = "Цей трек уже є в плейлісті."
                    return@launch
                }

                val currentTracks = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", playlistId) } }
                    .decodeList<PlaylistTrackDto>()

                val nextPosition = (currentTracks.maxOfOrNull { it.position } ?: 0) + 1

                supabaseClient.postgrest["playlist_tracks"].insert(
                    PlaylistTrackInsertDto(
                        playlistId = playlistId,
                        trackId = trackId,
                        position = nextPosition
                    )
                )
                _successMessage.value = "Трек додано до плейліста."
            } catch (e: Exception) {
                _errorMessage.value = "Не вдалося додати трек до плейліста."
            }
        }
    }

    // ==========================================
    // ЗАЯВКА НА ВАКАНСІЮ
    // ==========================================

    fun applyForVacancy(vacancyId: String, message: String) {
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            if (userId == null) {
                _errorMessage.value = "Увійдіть в акаунт, щоб відгукнутися на вакансію."
                return@launch
            }
            try {
                val existingApp = supabaseClient.postgrest["applications"]
                    .select { filter { eq("vacancy_id", vacancyId); eq("user_id", userId) } }
                    .decodeList<ApplicationDto>()

                if (existingApp.isNotEmpty()) {
                    _errorMessage.value = "Ви вже відгукнулися на цю вакансію."
                    return@launch
                }

                supabaseClient.postgrest["applications"].insert(
                    ApplicationInsertDto(vacancyId = vacancyId, userId = userId, message = message)
                )

                // Сповіщення адміну гурту
                _band.value?.managerId?.let { managerId ->
                    supabaseClient.postgrest["notifications"].insert(
                        NotificationInsertDto(
                            userId = managerId,
                            type   = "new_application",
                            title  = "Новий кандидат",
                            body   = "Є новий відгук на вакансію у ${_band.value?.name ?: "гурті"}"
                        )
                    )
                }

                _successMessage.value = "Вашу заявку надіслано."
            } catch (e: Exception) {
                _errorMessage.value = "Не вдалося надіслати заявку. Спробуйте ще раз."
            }
        }
    }

    // ==========================================
    // ДОПОМІЖНЕ: unique listeners з listen_history
    // ==========================================

    private suspend fun List<TrackDto>.withUniqueListenerCounts(): List<TrackDto> {
        if (isEmpty()) return emptyList()

        val trackIds = map { it.id }
        val history = supabaseClient.postgrest["listen_history"]
            .select { filter { isIn("track_id", trackIds) } }
            .decodeList<HistoryDto>()

        val listenersByTrack = history
            .groupBy { it.trackId }
            .mapValues { (_, listens) -> listens.map { it.userId }.distinct().size }

        return map { track ->
            track.copy(playsCount = listenersByTrack[track.id] ?: track.playsCount)
        }
    }
}
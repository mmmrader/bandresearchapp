package com.tkachukmo.bandresearchapp.feature.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.core.player.AudioController
import com.tkachukmo.bandresearchapp.data.remote.BandRepository
import com.tkachukmo.bandresearchapp.data.remote.dto.HistoryDto
import com.tkachukmo.bandresearchapp.data.remote.dto.HistoryInsertDto
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistDto
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistInsertDto
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistTrackDto
import com.tkachukmo.bandresearchapp.data.remote.dto.PlaylistTrackInsertDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.feature.profile.viewmodel.PlaylistDetailTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val bandRepository: BandRepository,
    private val audioController: AudioController,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    val track: StateFlow<TrackDto?> = audioController.currentTrack
    val isPlaying: StateFlow<Boolean> = audioController.isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _trackDuration = MutableStateFlow(0f)
    val trackDuration: StateFlow<Float> = _trackDuration.asStateFlow()

    val extractedTitle: StateFlow<String?> = audioController.extractedTitle
    val extractedArtwork: StateFlow<ByteArray?> = audioController.extractedArtwork
    val shuffleModeEnabled: StateFlow<Boolean> = audioController.shuffleModeEnabled
    val repeatMode: StateFlow<Int> = audioController.repeatMode

    private val _bandName = MutableStateFlow("Завантаження...")
    val bandName: StateFlow<String> = _bandName.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _uniqueListenersCount = MutableStateFlow(0)
    val uniqueListenersCount: StateFlow<Int> = _uniqueListenersCount.asStateFlow()

    private val _upcomingTracks = MutableStateFlow<List<TrackDto>>(emptyList())
    val upcomingTracks: StateFlow<List<TrackDto>> = _upcomingTracks.asStateFlow()

    private val _playlists = MutableStateFlow<List<PlaylistDto>>(emptyList())
    val playlists: StateFlow<List<PlaylistDto>> = _playlists.asStateFlow()

    private var lastRecordedTrackId: String? = null

    init {
        // Оновлення прогресу
        viewModelScope.launch {
            while (true) {
                if (isPlaying.value) {
                    _progress.value = audioController.getCurrentPositionSeconds()
                    _trackDuration.value = audioController.getDurationSeconds()
                }
                delay(500)
            }
        }

        // Слухаємо зміну треку
        viewModelScope.launch {
            track.collect { currentTrack ->
                if (currentTrack != null) {
                    _uniqueListenersCount.value = currentTrack.playsCount
                    try {
                        val band = bandRepository.getBandById(currentTrack.bandId)
                        if (currentTrack.bandId.isNotBlank()) {
                            _bandName.value = band?.name ?: "Невідомий виконавець"
                        }
                        checkIfTrackIsLiked(currentTrack.id)
                        recordListeningHistory(currentTrack.id)
                    } catch (e: Exception) {
                        recordListeningHistory(currentTrack.id)
                    }
                }
            }
        }

        // Оновлення черги
        viewModelScope.launch {
            combine(audioController.currentPlaylist, track) { playlist, current ->
                if (current == null || playlist.isEmpty()) return@combine emptyList<TrackDto>()
                val currentIndex = playlist.indexOfFirst { it.id == current.id }
                if (currentIndex == -1 || currentIndex == playlist.size - 1) emptyList()
                else playlist.subList(currentIndex + 1, playlist.size)
            }.collect { nextTracks ->
                _upcomingTracks.value = nextTracks
            }
        }
    }

    // ==========================================
    // ІСТОРІЯ ТА ЛІЧИЛЬНИК ПРОСЛУХОВУВАНЬ
    // ==========================================

    private fun recordListeningHistory(trackId: String) {
        if (lastRecordedTrackId == trackId) return // Захист від подвійного спрацювання
        lastRecordedTrackId = trackId

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                val currentTime = java.time.Instant.now().toString()

                // 1. Перевіряємо, чи слухав юзер цей трек раніше
                val existingHistory = supabaseClient.postgrest["listen_history"]
                    .select { filter { eq("user_id", userId); eq("track_id", trackId) } }
                    .decodeSingleOrNull<HistoryDto>()

                if (existingHistory != null) {
                    // Трек вже є в histórico -> оновлюємо час (дедупликація, без дубля)
                    supabaseClient.postgrest["listen_history"].update(
                        { set("listened_at", currentTime) }
                    ) { filter { eq("id", existingHistory.id!!) } }
                    // plays_count НЕ збільшуємо — той самий юзер повторно слухає
                } else {
                    // Нове прослуховування цього треку цим користувачем
                    supabaseClient.postgrest["listen_history"].insert(
                        HistoryInsertDto(userId = userId, trackId = trackId, listenedAt = currentTime)
                    )

                    // Ліміт 20 треків — прибираємо найстаріший якщо перевищено
                    val allUserHistory = supabaseClient.postgrest["listen_history"]
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<HistoryDto>()
                        .sortedByDescending { it.listenedAt }

                    if (allUserHistory.size > 20) {
                        val idsToDelete = allUserHistory.drop(20).mapNotNull { it.id }
                        if (idsToDelete.isNotEmpty()) {
                            supabaseClient.postgrest["listen_history"].delete {
                                filter { isIn("id", idsToDelete) }
                            }
                        }
                    }

                    // plays_count збільшуємо тільки при першому прослуховуванні треку цим юзером
                    val trackRow = supabaseClient.postgrest["tracks"]
                        .select { filter { eq("id", trackId) } }
                        .decodeSingleOrNull<TrackDto>()

                    if (trackRow != null) {
                        val newCount = trackRow.playsCount + 1
                        _uniqueListenersCount.value = newCount // Оновлюємо відразу в UI

                        supabaseClient.postgrest["tracks"].update(
                            { set("plays_count", newCount) }
                        ) { filter { eq("id", trackId) } }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // ЗАВАНТАЖЕННЯ ТРЕКУ
    // ==========================================

    fun loadTrack(trackId: String) {
        viewModelScope.launch {
            try {
                if (audioController.currentTrack.value?.id == trackId) return@launch

                val currentQueue = audioController.currentPlaylist.value
                val indexInQueue = currentQueue.indexOfFirst { it.id == trackId }

                if (indexInQueue != -1) {
                    audioController.skipToIndex(indexInQueue)
                    val band = bandRepository.getBandById(currentQueue[indexInQueue].bandId)
                    if (currentQueue[indexInQueue].bandId.isNotBlank()) {
                        _bandName.value = band?.name ?: "Невідомий виконавець"
                    }
                    checkIfTrackIsLiked(trackId)
                    return@launch
                }

                val loadedTrack = bandRepository.getTrackById(trackId)
                if (loadedTrack != null) {
                    val bandTracks = bandRepository.getTracksByBand(loadedTrack.bandId)
                    val band = bandRepository.getBandById(loadedTrack.bandId)
                    val artistName = band?.name ?: "Невідомий виконавець"
                    _bandName.value = artistName

                    val startIndex = bandTracks.indexOfFirst { it.id == trackId }.coerceAtLeast(0)
                    audioController.playQueue(bandTracks, startIndex, artistName)
                    checkIfTrackIsLiked(trackId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // ВІДТВОРЕННЯ З ПЛЕЙЛІСТА
    // ==========================================

    fun playPlaylistQueue(
        playlistName: String,
        tracks: List<PlaylistDetailTrack>,
        startIndex: Int
    ) {
        viewModelScope.launch {
            try {
                if (tracks.isEmpty()) return@launch

                val trackDtos = tracks.map { pt ->
                    TrackDto(
                        id          = pt.trackId,
                        bandId      = "",
                        title       = pt.title,
                        durationSec = pt.durationSec,
                        audioUrl    = pt.audioUrl,
                        coverUrl    = pt.coverUrl,
                        trackNumber = pt.position
                    )
                }

                val safeIndex = startIndex.coerceIn(0, trackDtos.lastIndex)
                audioController.playQueue(trackDtos, safeIndex, playlistName)
                _bandName.value = tracks[safeIndex].bandName
                checkIfTrackIsLiked(tracks[safeIndex].trackId)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // ЛАЙК
    // ==========================================

    fun checkIfTrackIsLiked(trackId: String) {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                val favoritesPlaylist = supabaseClient.postgrest["playlists"]
                    .select { filter { eq("user_id", userId); eq("name", "Улюблені") } }
                    .decodeSingleOrNull<PlaylistDto>()

                if (favoritesPlaylist != null) {
                    val existingTrack = supabaseClient.postgrest["playlist_tracks"]
                        .select { filter { eq("playlist_id", favoritesPlaylist.id); eq("track_id", trackId) } }
                        .decodeList<PlaylistTrackDto>()
                    _isLiked.value = existingTrack.isNotEmpty()
                } else {
                    _isLiked.value = false
                }
            } catch (e: Exception) {
                _isLiked.value = false
            }
        }
    }

    fun toggleLikeTrack(trackId: String) {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                var favoritesPlaylist = supabaseClient.postgrest["playlists"]
                    .select { filter { eq("user_id", userId); eq("name", "Улюблені") } }
                    .decodeSingleOrNull<PlaylistDto>()

                if (favoritesPlaylist == null) {
                    supabaseClient.postgrest["playlists"].insert(
                        PlaylistInsertDto(userId = userId, name = "Улюблені", isPublic = false)
                    )
                    favoritesPlaylist = supabaseClient.postgrest["playlists"]
                        .select { filter { eq("user_id", userId); eq("name", "Улюблені") } }
                        .decodeSingleOrNull<PlaylistDto>()
                }

                val playlistId = favoritesPlaylist?.id ?: return@launch

                if (_isLiked.value) {
                    supabaseClient.postgrest["playlist_tracks"].delete {
                        filter { eq("playlist_id", playlistId); eq("track_id", trackId) }
                    }
                    _isLiked.value = false
                } else {
                    supabaseClient.postgrest["playlist_tracks"].insert(
                        PlaylistTrackInsertDto(
                            playlistId = playlistId,
                            trackId    = trackId,
                            position   = System.currentTimeMillis().toInt()
                        )
                    )
                    _isLiked.value = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                e.printStackTrace()
            }
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            try {
                val existing = supabaseClient.postgrest["playlist_tracks"]
                    .select { filter { eq("playlist_id", playlistId); eq("track_id", trackId) } }
                    .decodeList<PlaylistTrackDto>()

                if (existing.isNotEmpty()) return@launch

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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // КЕРУВАННЯ
    // ==========================================

    fun togglePlayPause() = audioController.playPause()

    fun seekTo(position: Float) {
        _progress.value = position
        audioController.seekTo(position)
    }

    fun addTrackToQueue(track: TrackDto, bandName: String) {
        audioController.addTrackToQueue(track, bandName)
    }

    fun skipToNext() = audioController.skipToNext()
    fun skipToPrevious() = audioController.skipToPrevious()
    fun toggleShuffle() = audioController.toggleShuffle()
    fun toggleRepeat() = audioController.toggleRepeat()
}
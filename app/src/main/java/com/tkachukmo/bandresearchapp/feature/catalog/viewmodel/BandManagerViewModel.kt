package com.tkachukmo.bandresearchapp.feature.catalog.viewmodel

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.core.player.AudioController
import com.tkachukmo.bandresearchapp.data.remote.dto.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject

@Serializable
data class BandInsertDto(val name: String, val slug: String, val genres: List<String>, val manager_id: String)

@Serializable
data class TrackInsertDto(val band_id: String, val release_id: String? = null, val title: String, val duration_sec: Int, val audio_url: String, val cover_url: String? = null)

@Serializable
data class BandUpdateDto(val name: String? = null, val description: String? = null, val avatar_url: String? = null, val cover_url: String? = null)

@Serializable
data class VideoInsertDto(val band_id: String, val title: String, val youtube_id: String, val thumbnail_url: String)

@Serializable
data class ReleaseInsertDto(val band_id: String, val title: String, val release_type: String, val release_year: Int, val cover_url: String? = null)

@HiltViewModel
class BandManagerViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient,
    val audioController: AudioController
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentBand = MutableStateFlow<BandDto?>(null)
    val currentBand: StateFlow<BandDto?> = _currentBand.asStateFlow()

    private val _tracks = MutableStateFlow<List<TrackDto>>(emptyList())
    val tracks: StateFlow<List<TrackDto>> = _tracks.asStateFlow()

    private val _videos = MutableStateFlow<List<VideoDto>>(emptyList())
    val videos: StateFlow<List<VideoDto>> = _videos.asStateFlow()

    private val _releases = MutableStateFlow<List<ReleaseDto>>(emptyList())
    val releases: StateFlow<List<ReleaseDto>> = _releases.asStateFlow()

    private val _events = MutableStateFlow<List<BandEventDto>>(emptyList())
    val events: StateFlow<List<BandEventDto>> = _events.asStateFlow()

    private val _vacancies = MutableStateFlow<List<VacancyDto>>(emptyList())
    val vacancies: StateFlow<List<VacancyDto>> = _vacancies.asStateFlow()

    private val _applications = MutableStateFlow<List<ApplicationDto>>(emptyList())
    val applications: StateFlow<List<ApplicationDto>> = _applications.asStateFlow()
    private val _chatMessages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageDto>> = _chatMessages.asStateFlow()

    private val _candidateProfile = MutableStateFlow<ProfileDto?>(null)
    val candidateProfile: StateFlow<ProfileDto?> = _candidateProfile.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri: StateFlow<Uri?> = _selectedFileUri.asStateFlow()

    private val _uploadTitle = MutableStateFlow("")
    val uploadTitle: StateFlow<String> = _uploadTitle.asStateFlow()

    private val _uploadDuration = MutableStateFlow(0)
    val uploadDuration: StateFlow<Int> = _uploadDuration.asStateFlow()

    private val _uploadArtwork = MutableStateFlow<ByteArray?>(null)
    val uploadArtwork: StateFlow<ByteArray?> = _uploadArtwork.asStateFlow()

    private val _selectedReleaseId = MutableStateFlow<String?>(null)
    val selectedReleaseId: StateFlow<String?> = _selectedReleaseId.asStateFlow()

    init { checkUserBand() }

    private fun handleException(t: Throwable, defaultMessage: String) {
        t.printStackTrace()
        _errorMessage.value = "$defaultMessage: ${t.javaClass.simpleName}"
    }

    fun clearError() { _errorMessage.value = null }

    private fun checkUserBand() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val band = supabaseClient.postgrest["bands"]
                        .select { filter { eq("manager_id", userId) } }
                        .decodeSingleOrNull<BandDto>()
                    if (band != null) {
                        // Рахуємо сумарні прослуховування всіх треків гурту з listen_history
                        val totalPlays = runCatching {
                            @kotlinx.serialization.Serializable
                            data class CountDto(@kotlinx.serialization.SerialName("track_id") val trackId: String)

                            val trackIds = supabaseClient.postgrest["tracks"]
                                .select { filter { eq("band_id", band.id) } }
                                .decodeList<TrackDto>()
                                .map { it.id }

                            if (trackIds.isEmpty()) 0
                            else {
                                supabaseClient.postgrest["listen_history"]
                                    .select { filter { isIn("track_id", trackIds) } }
                                    .decodeList<CountDto>()
                                    .size
                            }
                        }.getOrElse { band.playsCount }

                        _currentBand.value = band.copy(playsCount = totalPlays)
                        loadReleases(band.id)
                        loadTracks(band.id)
                        loadVideos(band.id)
                        loadEvents(band.id)
                        loadVacancies(band.id)
                    }
                }
            } catch (t: Throwable) { handleException(t, "Помилка завантаження даних") }
            finally { _isLoading.value = false }
        }
    }

    private fun loadTracks(bandId: String) {
        viewModelScope.launch { try { _tracks.value = supabaseClient.postgrest["tracks"].select { filter { eq("band_id", bandId) } }.decodeList<TrackDto>() } catch (t: Throwable) {} }
    }

    private fun loadVideos(bandId: String) {
        viewModelScope.launch { try { _videos.value = supabaseClient.postgrest["videos"].select { filter { eq("band_id", bandId) } }.decodeList<VideoDto>() } catch (t: Throwable) {} }
    }

    private fun loadReleases(bandId: String) {
        viewModelScope.launch {
            try {
                val releaseList = supabaseClient.postgrest["releases"].select { filter { eq("band_id", bandId) } }.decodeList<ReleaseDto>()
                _releases.value = releaseList
                if (_selectedReleaseId.value == null && releaseList.isNotEmpty()) _selectedReleaseId.value = releaseList.first().id
            } catch (t: Throwable) {}
        }
    }

    private fun loadEvents(bandId: String) {
        viewModelScope.launch {
            try {
                _events.value = supabaseClient.postgrest["band_events"]
                    .select { filter { eq("band_id", bandId) } }
                    .decodeList<BandEventDto>()
                    .sortedByDescending { it.createdAt ?: it.eventDate ?: "" }
            } catch (_: Throwable) {
                _events.value = emptyList()
            }
        }
    }

    private fun loadVacancies(bandId: String) {
        viewModelScope.launch {
            try {
                _vacancies.value = supabaseClient.postgrest["vacancies"]
                    .select { filter { eq("band_id", bandId); eq("is_active", true) } }
                    .decodeList<VacancyDto>()
                loadApplications(bandId)
            } catch (_: Throwable) {
                _vacancies.value = emptyList()
            }
        }
    }

    private fun loadApplications(bandId: String) {
        viewModelScope.launch {
            try {
                val vacancyIds = _vacancies.value.map { it.id }
                _applications.value = if (vacancyIds.isEmpty()) {
                    emptyList()
                } else {
                    supabaseClient.postgrest["applications"]
                        .select { filter { isIn("vacancy_id", vacancyIds) } }
                        .decodeList<ApplicationDto>()
                        .sortedByDescending { it.createdAt ?: "" }
                }
            } catch (_: Throwable) {
                _applications.value = emptyList()
            }
        }
    }

    private suspend fun createAutomaticEvent(
        bandId: String,
        title: String,
        type: String,
        description: String? = null,
        smartLink: String? = null
    ) {
        try {
            supabaseClient.postgrest["band_events"].insert(
                BandEventInsertDto(
                    bandId = bandId,
                    title = title,
                    description = description,
                    type = type,
                    smartLink = smartLink
                )
            )
            loadEvents(bandId)
        } catch (_: Throwable) {
        }
    }

    fun createRelease(context: Context, title: String, type: String, year: Int, coverUri: Uri?, onSuccess: () -> Unit) {
        val bandId = _currentBand.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                var coverUrl: String? = null
                if (coverUri != null) {
                    val bytes = context.contentResolver.openInputStream(coverUri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val fileName = "release_${UUID.randomUUID()}.jpg"
                        supabaseClient.storage["images"].upload(fileName, bytes)
                        coverUrl = supabaseClient.storage["images"].publicUrl(fileName)
                    }
                }
                supabaseClient.postgrest["releases"].insert(ReleaseInsertDto(bandId, title, type, year, coverUrl))
                createAutomaticEvent(
                    bandId = bandId,
                    title = "Новий реліз: $title",
                    type = "release",
                    description = "Гурт опублікував ${type.uppercase()} $year року."
                )
                loadReleases(bandId)
                onSuccess()
            } catch (t: Throwable) { handleException(t, "Помилка створення релізу") }
            finally { _isLoading.value = false }
        }
    }

    fun deleteRelease(releaseId: String) {
        val bandId = _currentBand.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val releaseTracks = supabaseClient.postgrest["tracks"]
                    .select {
                        filter {
                            eq("band_id", bandId)
                            eq("release_id", releaseId)
                        }
                    }
                    .decodeList<TrackDto>()
                val releaseTrackIds = releaseTracks.map { it.id }

                if (releaseTrackIds.isNotEmpty()) {
                    supabaseClient.postgrest["playlist_tracks"].delete {
                        filter { isIn("track_id", releaseTrackIds) }
                    }
                }

                supabaseClient.postgrest["tracks"].delete {
                    filter {
                        eq("band_id", bandId)
                        eq("release_id", releaseId)
                    }
                }
                supabaseClient.postgrest["releases"].delete {
                    filter {
                        eq("id", releaseId)
                        eq("band_id", bandId)
                    }
                }
                if (_selectedReleaseId.value == releaseId) {
                    _selectedReleaseId.value = null
                }
                loadTracks(bandId)
                loadReleases(bandId)
                _errorMessage.value = "Реліз видалено"
            } catch (t: Throwable) {
                handleException(t, "Помилка видалення релізу")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun extractYouTubeId(url: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*"
        val compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) matcher.group() else null
    }

    fun addYouTubeVideo(title: String, url: String, onSuccess: () -> Unit) {
        val bandId = _currentBand.value?.id ?: return
        if (!url.contains("youtube.com") && !url.contains("youtu.be")) { _errorMessage.value = "Будь ласка, введіть коректне посилання на YouTube."; return }
        val youtubeId = extractYouTubeId(url)
        if (youtubeId.isNullOrBlank()) { _errorMessage.value = "Не вдалося розпізнати відео."; return }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                supabaseClient.postgrest["videos"].insert(VideoInsertDto(bandId, title, youtubeId, "https://img.youtube.com/vi/$youtubeId/hqdefault.jpg"))
                createAutomaticEvent(
                    bandId = bandId,
                    title = "Новий кліп: $title",
                    type = "video",
                    description = "Відео вже доступне на YouTube.",
                    smartLink = "https://www.youtube.com/watch?v=$youtubeId"
                )
                loadVideos(bandId)
                onSuccess()
            } catch (t: Throwable) { handleException(t, "Помилка додавання відео") }
            finally { _isLoading.value = false }
        }
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["videos"].delete { filter { eq("id", videoId) } }
                _currentBand.value?.id?.let { loadVideos(it) }
            } catch (t: Throwable) {
                handleException(t, "Помилка видалення відео")
            }
        }
    }

    fun updateBandInfo(newName: String, newDesc: String, onSuccess: () -> Unit) {
        val bandId = _currentBand.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                supabaseClient.postgrest["bands"].update(BandUpdateDto(name = newName, description = newDesc.takeIf { it.isNotBlank() })) { filter { eq("id", bandId) } }
                checkUserBand()
                onSuccess()
            } catch (t: Throwable) { handleException(t, "Помилка оновлення профілю") }
            finally { _isLoading.value = false }
        }
    }

    fun uploadBandImage(context: Context, uri: Uri, isCover: Boolean) {
        val bandId = _currentBand.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
                val fileName = "band_${bandId}_${if (isCover) "cover" else "avatar"}_${UUID.randomUUID()}.jpg"
                supabaseClient.storage["images"].upload(fileName, bytes)
                val imageUrl = supabaseClient.storage["images"].publicUrl(fileName)
                supabaseClient.postgrest["bands"].update(if (isCover) BandUpdateDto(cover_url = imageUrl) else BandUpdateDto(avatar_url = imageUrl)) { filter { eq("id", bandId) } }
                checkUserBand()
            } catch (t: Throwable) { handleException(t, "Помилка обробки зображення гурту") }
            finally { _isLoading.value = false }
        }
    }

    fun playTrack(trackDto: TrackDto, allTracks: List<TrackDto>) { audioController.playQueue(allTracks, allTracks.indexOfFirst { it.id == trackDto.id }.coerceAtLeast(0)) }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["tracks"].delete { filter { eq("id", trackId) } }
                _currentBand.value?.id?.let { loadTracks(it) }
            } catch (t: Throwable) {
                handleException(t, "Помилка видалення треку")
            }
        }
    }

    fun createBand(name: String, slug: String, genres: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                supabaseClient.auth.currentUserOrNull()?.id?.let { userId ->
                    supabaseClient.postgrest["bands"].insert(BandInsertDto(name, slug.lowercase().replace(" ", "-"), genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }, userId))
                    checkUserBand()
                    onSuccess()
                }
            } catch (t: Throwable) { handleException(t, "Помилка створення гурту") }
            finally { _isLoading.value = false }
        }
    }

    // ВАЖЛИВО: Безпечне читання файлу у фоні. Відсутність вильотів у Каталог!
    fun analyzeAudioFile(context: Context, uri: Uri) {
        _selectedFileUri.value = uri
        viewModelScope.launch(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                val picture = retriever.embeddedPicture

                _uploadTitle.value = title
                _uploadDuration.value = (durationMs / 1000).toInt()
                _uploadArtwork.value = picture
            } catch (t: Throwable) {
                _errorMessage.value = "Не вдалося відкрити файл. Спробуйте інший."
            } finally {
                try { retriever.release() } catch (e: Exception) {}
            }
        }
    }

    fun updateUploadArtwork(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) { try { _uploadArtwork.value = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } } catch (t: Throwable) {} }
    }

    fun updateUploadTitle(newTitle: String) { _uploadTitle.value = newTitle }
    fun updateSelectedRelease(releaseId: String) { _selectedReleaseId.value = releaseId }
    fun clearUploadForm() { _selectedFileUri.value = null; _uploadTitle.value = ""; _uploadDuration.value = 0; _uploadArtwork.value = null; _errorMessage.value = null }

    fun createManualEvent(
        title: String,
        description: String,
        type: String,
        eventDate: String,
        venue: String,
        city: String,
        smartLink: String,
        spotifyUrl: String,
        appleMusicUrl: String,
        youtubeMusicUrl: String,
        onSuccess: () -> Unit
    ) {
        val bandId = _currentBand.value?.id ?: return
        if (title.isBlank()) {
            _errorMessage.value = "Назва події не може бути порожньою"
            return
        }
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["band_events"].insert(
                    BandEventInsertDto(
                        bandId = bandId,
                        title = title,
                        description = description.takeIf { it.isNotBlank() },
                        type = type,
                        eventDate = eventDate.takeIf { it.isNotBlank() },
                        venue = venue.takeIf { it.isNotBlank() },
                        city = city.takeIf { it.isNotBlank() },
                        smartLink = smartLink.takeIf { it.isNotBlank() },
                        spotifyUrl = spotifyUrl.takeIf { it.isNotBlank() },
                        appleMusicUrl = appleMusicUrl.takeIf { it.isNotBlank() },
                        youtubeMusicUrl = youtubeMusicUrl.takeIf { it.isNotBlank() }
                    )
                )
                loadEvents(bandId)
                _errorMessage.value = "Подію опубліковано"
                onSuccess()
            } catch (t: Throwable) {
                handleException(t, "Помилка створення події")
            }
        }
    }

    fun createVacancy(instrument: String, description: String, city: String) {
        val bandId = _currentBand.value?.id ?: return
        if (instrument.isBlank()) {
            _errorMessage.value = "Вкажіть інструмент для вакансії"
            return
        }
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["vacancies"].insert(
                    VacancyInsertDto(
                        bandId = bandId,
                        instrument = instrument,
                        description = description.takeIf { it.isNotBlank() },
                        city = city.takeIf { it.isNotBlank() }
                    )
                )
                loadVacancies(bandId)
                _errorMessage.value = "Вакансію створено"
            } catch (t: Throwable) {
                handleException(t, "Помилка створення вакансії")
            }
        }
    }

    fun deleteVacancy(vacancyId: String) {
        val bandId = _currentBand.value?.id ?: return
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["vacancies"].update({ set("is_active", false) }) {
                    filter { eq("id", vacancyId) }
                }
                loadVacancies(bandId)
                _errorMessage.value = "Вакансію закрито"
            } catch (t: Throwable) {
                handleException(t, "Помилка видалення вакансії")
            }
        }
    }

    fun updateApplicationStatus(application: ApplicationDto, status: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["applications"].update({ set("status", status) }) {
                    filter { eq("id", application.id) }
                }
                supabaseClient.postgrest["notifications"].insert(
                    NotificationInsertDto(
                        userId = application.userId,
                        type = "application_status",
                        title = if (status == "accepted") "Заявку прийнято" else "Заявку відхилено",
                        body = if (status == "accepted") {
                            "Гурт прийняв вашу заявку. Очікуйте повідомлення від адміністратора."
                        } else {
                            "Гурт відхилив вашу заявку. Спробуйте інші вакансії."
                        }
                    )
                )
                _currentBand.value?.id?.let { loadApplications(it) }
                _errorMessage.value = "Статус заявки оновлено"
            } catch (t: Throwable) {
                handleException(t, "Помилка оновлення заявки")
            }
        }
    }

    fun sendMessageToCandidate(application: ApplicationDto, text: String) {
        if (text.isBlank()) return
        val senderId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["chat_messages"].insert(
                    ChatMessageInsertDto(
                        senderId = senderId,
                        recipientId = application.userId,
                        bandId = _currentBand.value?.id,
                        text = text
                    )
                )
                supabaseClient.postgrest["notifications"].insert(
                    NotificationInsertDto(
                        userId = application.userId,
                        type = "message",
                        title = "Повідомлення від гурту",
                        body = text
                    )
                )
                _errorMessage.value = "Повідомлення надіслано"
            } catch (t: Throwable) {
                handleException(t, "Помилка надсилання повідомлення")
            }
        }
    }
    fun loadCandidateProfile(userId: String) {
        viewModelScope.launch {
            try {
                _candidateProfile.value = supabaseClient.postgrest["profiles"]
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingleOrNull<ProfileDto>()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadChat(candidateId: String) {
        val myId = supabaseClient.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            try {
                val messages = supabaseClient.postgrest["chat_messages"]
                    .select()
                    .decodeList<ChatMessageDto>()
                    .filter {
                        (it.senderId == myId && it.recipientId == candidateId) ||
                                (it.senderId == candidateId && it.recipientId == myId)
                    }
                    .sortedBy { it.createdAt }

                _chatMessages.value = messages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun uploadTrack(context: Context, onSuccess: () -> Unit) {
        val bandId = _currentBand.value?.id ?: return
        val fileUri = _selectedFileUri.value ?: return
        val relId = _selectedReleaseId.value
        val title = _uploadTitle.value.trim()

        if (title.isBlank()) {
            _errorMessage.value = "Назва треку не може бути порожньою"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val existingTrack = supabaseClient.postgrest["tracks"]
                    .select {
                        filter {
                            eq("band_id", bandId)
                            ilike("title", title)
                        }
                    }
                    .decodeList<TrackDto>()

                if (existingTrack.isNotEmpty()) {
                    _errorMessage.value = "Трек з такою назвою вже є у цьому гурті"
                    return@launch
                }

                val trackUuid = UUID.randomUUID().toString()
                val audioBytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() } ?: return@launch
                val audioFileName = "audio_$trackUuid.mp3"
                supabaseClient.storage["tracks"].upload(audioFileName, audioBytes)
                val audioUrl = supabaseClient.storage["tracks"].publicUrl(audioFileName)

                var coverUrl: String? = null
                _uploadArtwork.value?.let { bytes ->
                    val coverFileName = "cover_$trackUuid.jpg"
                    supabaseClient.storage["images"].upload(coverFileName, bytes)
                    coverUrl = supabaseClient.storage["images"].publicUrl(coverFileName)
                }

                supabaseClient.postgrest["tracks"].insert(TrackInsertDto(band_id = bandId, release_id = relId, title = title, duration_sec = _uploadDuration.value, audio_url = audioUrl, cover_url = coverUrl))
                createAutomaticEvent(
                    bandId = bandId,
                    title = "Новий трек: $title",
                    type = "release",
                    description = "Трек додано до каталогу гурту.",
                    smartLink = audioUrl
                )
                clearUploadForm()
                loadTracks(bandId)
                onSuccess()
            } catch (t: Throwable) { handleException(t, "Не вдалося завантажити трек в хмару") }
            finally { _isLoading.value = false }
        }
    }
}

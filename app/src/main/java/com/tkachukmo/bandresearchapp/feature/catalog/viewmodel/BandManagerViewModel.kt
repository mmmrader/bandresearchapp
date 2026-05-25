package com.tkachukmo.bandresearchapp.feature.catalog.viewmodel

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.core.player.AudioController
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.UUID
import javax.inject.Inject

@Serializable
data class BandInsertDto(
    val name: String,
    val slug: String,
    val genres: List<String>,
    val manager_id: String
)

@Serializable
data class TrackInsertDto(
    val band_id: String,
    val title: String,
    val duration_sec: Int,
    val audio_url: String,
    val cover_url: String? = null
)

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

    init {
        checkUserBand()
    }

    // --- УНІВЕРСАЛЬНИЙ ОБРОБНИК ПОМИЛОК ІНТЕРНЕТУ ---
    private fun handleNetworkError(e: Exception, defaultMessage: String) {
        e.printStackTrace()
        val errorMsg = e.localizedMessage ?: ""
        if (e is UnknownHostException || e is ConnectException || errorMsg.contains("Unable to resolve host")) {
            _errorMessage.value = "Відсутнє підключення до інтернету. Перевірте з'єднання."
        } else {
            _errorMessage.value = "$defaultMessage: ${e.message}"
        }
    }

    fun checkUserBand() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val band = supabaseClient.postgrest["bands"]
                        .select { filter { eq("manager_id", userId) } }
                        .decodeSingleOrNull<BandDto>()
                    _currentBand.value = band

                    if (band != null) {
                        loadTracks(band.id)
                    }
                }
            } catch (e: Exception) {
                handleNetworkError(e, "Помилка завантаження даних")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTracks(bandId: String) {
        viewModelScope.launch {
            try {
                val trackList = supabaseClient.postgrest["tracks"]
                    .select { filter { eq("band_id", bandId) } }
                    .decodeList<TrackDto>()
                _tracks.value = trackList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playTrack(trackDto: TrackDto, allTracks: List<TrackDto>) {
        val startIndex = allTracks.indexOfFirst { it.id == trackDto.id }.coerceAtLeast(0)
        audioController.playQueue(allTracks, startIndex)
    }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["tracks"].delete { filter { eq("id", trackId) } }
                _currentBand.value?.id?.let { loadTracks(it) }
            } catch (e: Exception) {
                handleNetworkError(e, "Помилка видалення")
            }
        }
    }

    fun createBand(name: String, slug: String, genres: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val genresList = genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val newBand = BandInsertDto(name, slug.lowercase().replace(" ", "-"), genresList, userId)
                    supabaseClient.postgrest["bands"].insert(newBand)
                    checkUserBand()
                    onSuccess()
                }
            } catch (e: Exception) {
                handleNetworkError(e, "Помилка створення гурту")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzeAudioFile(context: Context, uri: Uri) {
        _selectedFileUri.value = uri
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            _uploadTitle.value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            _uploadDuration.value = (durationMs / 1000).toInt()
            _uploadArtwork.value = retriever.embeddedPicture
        } catch (e: Exception) {
            e.printStackTrace()
            _errorMessage.value = "Не вдалося прочитати файл"
        } finally {
            retriever.release()
        }
    }

    fun updateUploadArtwork(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                _uploadArtwork.value = bytes
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Не вдалося завантажити фото"
            }
        }
    }

    fun updateUploadTitle(newTitle: String) { _uploadTitle.value = newTitle }

    fun clearUploadForm() {
        _selectedFileUri.value = null
        _uploadTitle.value = ""
        _uploadDuration.value = 0
        _uploadArtwork.value = null
        _errorMessage.value = null
    }

    fun uploadTrack(context: Context, onSuccess: () -> Unit) {
        val bandId = _currentBand.value?.id ?: return
        val fileUri = _selectedFileUri.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val trackUuid = UUID.randomUUID().toString()

                val audioBytes = context.contentResolver.openInputStream(fileUri)?.use { it.readBytes() } ?: return@launch
                val audioFileName = "audio_$trackUuid.mp3"
                supabaseClient.storage["tracks"].upload(audioFileName, audioBytes)
                val audioUrl = supabaseClient.storage["tracks"].publicUrl(audioFileName)

                var coverUrl: String? = null
                _uploadArtwork.value?.let { bytes ->
                    val coverFileName = "cover_$trackUuid.jpg"
                    supabaseClient.storage["tracks"].upload(coverFileName, bytes)
                    coverUrl = supabaseClient.storage["tracks"].publicUrl(coverFileName)
                }

                val newTrack = TrackInsertDto(
                    band_id = bandId,
                    title = _uploadTitle.value,
                    duration_sec = _uploadDuration.value,
                    audio_url = audioUrl,
                    cover_url = coverUrl
                )
                supabaseClient.postgrest["tracks"].insert(newTrack)

                clearUploadForm()
                loadTracks(bandId)
                onSuccess()
            } catch (e: Exception) {
                handleNetworkError(e, "Помилка завантаження файлу")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
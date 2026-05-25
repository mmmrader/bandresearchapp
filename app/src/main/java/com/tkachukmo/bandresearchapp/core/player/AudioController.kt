package com.tkachukmo.bandresearchapp.core.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri // ДОДАНО ІМПОРТ ДЛЯ URI
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<TrackDto?>(null)
    val currentTrack: StateFlow<TrackDto?> = _currentTrack.asStateFlow()

    private val _extractedTitle = MutableStateFlow<String?>(null)
    val extractedTitle: StateFlow<String?> = _extractedTitle.asStateFlow()

    private val _extractedArtwork = MutableStateFlow<ByteArray?>(null)
    val extractedArtwork: StateFlow<ByteArray?> = _extractedArtwork.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private var currentPlaylist: List<TrackDto> = emptyList()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))

        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()

            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    _isPlaying.value = playing
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    val id = mediaItem?.mediaId
                    _currentTrack.value = currentPlaylist.find { it.id == id }
                }

                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    _extractedTitle.value = mediaMetadata.title?.toString()
                    _extractedArtwork.value = mediaMetadata.artworkData
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _shuffleModeEnabled.value = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode
                }
            })
        }, ContextCompat.getMainExecutor(context))
    }

    // ДОДАНО: Параметр bandName
    fun playQueue(tracks: List<TrackDto>, startIndex: Int = 0, bandName: String = "Невідомий виконавець") {
        currentPlaylist = tracks
        _currentTrack.value = tracks.getOrNull(startIndex)

        mediaController?.let { controller ->
            val mediaItems = tracks.map { track ->

                // 1. СТВОРЮЄМО КРАСИВІ МЕТАДАНІ ДЛЯ ШТОРКИ ТА ЕКРАНУ БЛОКУВАННЯ
                val metadata = MediaMetadata.Builder()
                    .setTitle(track.title)      // Назва пісні
                    .setArtist(bandName)        // Реальнa назва гурту з БД!
                    // Якщо в тебе DTO використовує cover_url замість coverUrl - просто зміни тут:
                    .setArtworkUri(track.coverUrl?.let { Uri.parse(it) })
                    .build()

                // 2. ПРИВ'ЯЗУЄМО ЇХ ДО ТРЕКУ
                MediaItem.Builder()
                    .setUri(track.audioUrl ?: "")
                    .setMediaId(track.id)
                    .setMediaMetadata(metadata) // Ось тут відбувається магія!
                    .build()
            }

            controller.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
            controller.prepare()
            controller.play()
        }
    }

    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun skipToNext() { mediaController?.seekToNext() }
    fun skipToPrevious() { mediaController?.seekToPrevious() }

    fun toggleShuffle() {
        mediaController?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun toggleRepeat() {
        mediaController?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun seekTo(positionSeconds: Float) {
        mediaController?.seekTo((positionSeconds * 1000).toLong())
    }

    fun getCurrentPositionSeconds(): Float {
        return (mediaController?.currentPosition?.toFloat() ?: 0f) / 1000f
    }

    fun getDurationSeconds(): Float {
        val duration = mediaController?.duration ?: 0L
        return if (duration == C.TIME_UNSET || duration < 0) 0f else duration / 1000f
    }
}
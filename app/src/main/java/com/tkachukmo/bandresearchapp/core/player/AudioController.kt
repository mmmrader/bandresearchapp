package com.tkachukmo.bandresearchapp.core.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
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

    private var mediaControllerFuture:
            ListenableFuture<MediaController>? = null

    private var mediaController:
            MediaController? = null

    // Playing state
    private val _isPlaying =
        MutableStateFlow(false)

    val isPlaying: StateFlow<Boolean> =
        _isPlaying.asStateFlow()

    // Current track
    private val _currentTrack =
        MutableStateFlow<TrackDto?>(null)

    val currentTrack: StateFlow<TrackDto?> =
        _currentTrack.asStateFlow()

    // Extracted metadata
    private val _extractedTitle =
        MutableStateFlow<String?>(null)

    val extractedTitle: StateFlow<String?> =
        _extractedTitle.asStateFlow()

    private val _extractedArtwork =
        MutableStateFlow<ByteArray?>(null)

    val extractedArtwork: StateFlow<ByteArray?> =
        _extractedArtwork.asStateFlow()

    // Shuffle
    private val _shuffleModeEnabled =
        MutableStateFlow(false)

    val shuffleModeEnabled: StateFlow<Boolean> =
        _shuffleModeEnabled.asStateFlow()

    // Repeat
    private val _repeatMode =
        MutableStateFlow(Player.REPEAT_MODE_OFF)

    val repeatMode: StateFlow<Int> =
        _repeatMode.asStateFlow()

    // Playlist
    private val _currentPlaylist =
        MutableStateFlow<List<TrackDto>>(emptyList())

    val currentPlaylist:
            StateFlow<List<TrackDto>> =
        _currentPlaylist.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {

        val sessionToken = SessionToken(
            context,
            ComponentName(
                context,
                PlaybackService::class.java
            )
        )

        mediaControllerFuture =
            MediaController.Builder(
                context,
                sessionToken
            ).buildAsync()

        mediaControllerFuture?.addListener({

            mediaController =
                mediaControllerFuture?.get()

            mediaController?.addListener(

                object : Player.Listener {

                    override fun onIsPlayingChanged(
                        playing: Boolean
                    ) {

                        _isPlaying.value = playing
                    }

                    override fun onMediaItemTransition(
                        mediaItem: MediaItem?,
                        reason: Int
                    ) {

                        val id = mediaItem?.mediaId

                        _currentTrack.value =
                            _currentPlaylist.value.find {
                                it.id == id
                            }
                    }

                    override fun onMediaMetadataChanged(
                        mediaMetadata: MediaMetadata
                    ) {

                        _extractedTitle.value =
                            mediaMetadata.title?.toString()

                        _extractedArtwork.value =
                            mediaMetadata.artworkData
                    }

                    override fun onShuffleModeEnabledChanged(
                        shuffleModeEnabled: Boolean
                    ) {

                        _shuffleModeEnabled.value =
                            shuffleModeEnabled
                    }

                    override fun onRepeatModeChanged(
                        repeatMode: Int
                    ) {

                        _repeatMode.value =
                            repeatMode
                    }
                }
            )

        }, ContextCompat.getMainExecutor(context))
    }

    // Play playlist
    fun playQueue(
        tracks: List<TrackDto>,
        startIndex: Int = 0,
        bandName: String = "Невідомий виконавець"
    ) {

        // Оновлюємо playlist StateFlow
        _currentPlaylist.value = tracks

        // Поточний трек
        _currentTrack.value =
            tracks.getOrNull(startIndex)

        mediaController?.let { controller ->

            val mediaItems = tracks.map { track ->

                // Metadata
                val metadata =
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(bandName)
                        .setArtworkUri(
                            track.coverUrl?.let {
                                Uri.parse(it)
                            }
                        )
                        .build()

                // Media item
                MediaItem.Builder()
                    .setUri(track.audioUrl ?: "")
                    .setMediaId(track.id)
                    .setMediaMetadata(metadata)
                    .build()
            }

            controller.setMediaItems(
                mediaItems,
                startIndex,
                C.TIME_UNSET
            )

            controller.prepare()
            controller.play()
        }
    }

    // ДОДАНО: Add track to queue
    fun addTrackToQueue(
        track: TrackDto,
        bandName: String
    ) {

        val currentQueue =
            _currentPlaylist.value.toMutableList()

        currentQueue.add(track)

        // Оновлюємо стан для UI
        _currentPlaylist.value = currentQueue

        mediaController?.let { controller ->

            val metadata =
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(bandName)
                    .setArtworkUri(
                        track.coverUrl?.let {
                            Uri.parse(it)
                        }
                    )
                    .build()

            val mediaItem =
                MediaItem.Builder()
                    .setUri(track.audioUrl ?: "")
                    .setMediaId(track.id)
                    .setMediaMetadata(metadata)
                    .build()

            // Додаємо трек у поточну чергу
            controller.addMediaItem(mediaItem)
        }
    }

    // Play / Pause
    fun playPause() {

        mediaController?.let {

            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    // Next
    fun skipToNext() {
        mediaController?.seekToNext()
    }

    // Previous
    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    // Shuffle
    fun toggleShuffle() {

        mediaController?.let {

            it.shuffleModeEnabled =
                !it.shuffleModeEnabled
        }
    }

    // Repeat
    fun toggleRepeat() {

        mediaController?.let {

            it.repeatMode =
                when (it.repeatMode) {

                    Player.REPEAT_MODE_OFF ->
                        Player.REPEAT_MODE_ALL

                    Player.REPEAT_MODE_ALL ->
                        Player.REPEAT_MODE_ONE

                    else ->
                        Player.REPEAT_MODE_OFF
                }
        }
    }

    // Seek
    fun seekTo(positionSeconds: Float) {

        mediaController?.seekTo(
            (positionSeconds * 1000).toLong()
        )
    }

    // Current position
    fun getCurrentPositionSeconds(): Float {

        return (
                mediaController?.currentPosition
                    ?.toFloat()
                    ?: 0f
                ) / 1000f
    }

    // Duration
    fun getDurationSeconds(): Float {

        val duration =
            mediaController?.duration ?: 0L

        return if (
            duration == C.TIME_UNSET ||
            duration < 0
        ) {
            0f
        } else {
            duration / 1000f
        }
    }

    // Stop + clear queue
    fun stopAndClear() {

        mediaController?.let {

            it.pause()
            it.stop()
            it.clearMediaItems()
        }

        _currentTrack.value = null
        _currentPlaylist.value = emptyList()
        _isPlaying.value = false
    }

    // Skip to specific index
    fun skipToIndex(index: Int) {

        mediaController?.let {

            it.seekToDefaultPosition(index)
            it.play()
        }
    }
}
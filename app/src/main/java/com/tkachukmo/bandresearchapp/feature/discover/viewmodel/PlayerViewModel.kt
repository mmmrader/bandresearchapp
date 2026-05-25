package com.tkachukmo.bandresearchapp.feature.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.core.player.AudioController
import com.tkachukmo.bandresearchapp.data.remote.BandRepository
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val bandRepository: BandRepository,
    private val audioController: AudioController
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

    // ДОДАНО: Стан для назви гурту
    private val _bandName = MutableStateFlow<String>("Завантаження...")
    val bandName: StateFlow<String> = _bandName.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                if (isPlaying.value) {
                    _progress.value = audioController.getCurrentPositionSeconds()
                    _trackDuration.value = audioController.getDurationSeconds()
                }
                delay(500)
            }
        }

        // ДОДАНО: Слухаємо зміну треку і завантажуємо назву гурту
        viewModelScope.launch {
            track.collect { currentTrack ->
                if (currentTrack != null) {
                    try {
                        val band = bandRepository.getBandById(currentTrack.bandId)
                        val artistName = band?.name ?: "Невідомий виконавець"
                        _bandName.value = artistName
                    } catch (e: Exception) {
                        _bandName.value = "Помилка завантаження"
                    }
                }
            }
        }
    }

    fun loadTrack(trackId: String) {
        if (audioController.currentTrack.value?.id == trackId) return

        viewModelScope.launch {
            try {
                val loadedTrack = bandRepository.getTrackById(trackId)
                _progress.value = 0f

                if (loadedTrack != null) {
                    val bandTracks = bandRepository.getTracksByBand(loadedTrack.bandId)

                    // ДОДАНО: Завантажуємо назву гурту для передачі в чергу
                    val band = bandRepository.getBandById(loadedTrack.bandId)
                    val artistName = band?.name ?: "Невідомий виконавець"
                    _bandName.value = artistName

                    val startIndex = bandTracks.indexOfFirst { it.id == trackId }.coerceAtLeast(0)

                    // Передаємо плейлист і НАЗВУ ГУРТУ в контролер
                    audioController.playQueue(bandTracks, startIndex, artistName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun togglePlayPause() { audioController.playPause() }
    fun seekTo(position: Float) { _progress.value = position; audioController.seekTo(position) }
    fun skipToNext() { audioController.skipToNext() }
    fun skipToPrevious() { audioController.skipToPrevious() }
    fun toggleShuffle() { audioController.toggleShuffle() }
    fun toggleRepeat() { audioController.toggleRepeat() }
}
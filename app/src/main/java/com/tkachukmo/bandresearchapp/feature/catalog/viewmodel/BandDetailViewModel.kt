package com.tkachukmo.bandresearchapp.feature.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.BandRepository
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BandDetailViewModel @Inject constructor(
    private val bandRepository: BandRepository
) : ViewModel() {

    private val _band = MutableStateFlow<BandDto?>(null)
    val band: StateFlow<BandDto?> = _band.asStateFlow()

    private val _tracks = MutableStateFlow<List<TrackDto>>(emptyList())
    val tracks: StateFlow<List<TrackDto>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadBandDetails(bandId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Паралельно завантажуємо інфо про гурт та його треки
                _band.value = bandRepository.getBandById(bandId)
                _tracks.value = bandRepository.getTracksByBand(bandId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
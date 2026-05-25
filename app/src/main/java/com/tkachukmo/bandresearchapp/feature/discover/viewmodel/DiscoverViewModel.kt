package com.tkachukmo.bandresearchapp.feature.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.BandRepository
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val bandRepository: BandRepository
) : ViewModel() {

    private val _recommendedBands = MutableStateFlow<List<BandDto>>(emptyList())
    val recommendedBands: StateFlow<List<BandDto>> = _recommendedBands.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadRecommendations()
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Отримуємо всі гурти і перемішуємо їх для ефекту "випадкових рекомендацій"
                val bands = bandRepository.getAllBands().shuffled()
                _recommendedBands.value = bands
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Метод для видалення гурту зі списку після того, як його свайпнули
    fun onBandSwiped(bandId: String) {
        val currentList = _recommendedBands.value.toMutableList()
        currentList.removeAll { it.id == bandId }
        _recommendedBands.value = currentList
    }
}
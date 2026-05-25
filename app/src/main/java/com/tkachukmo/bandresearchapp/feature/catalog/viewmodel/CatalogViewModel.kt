package com.tkachukmo.bandresearchapp.feature.catalog.viewmodel

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
class CatalogViewModel @Inject constructor(
    private val bandRepository: BandRepository
) : ViewModel() {

    // Стан списку гуртів
    private val _bands = MutableStateFlow<List<BandDto>>(emptyList())
    val bands: StateFlow<List<BandDto>> = _bands.asStateFlow()

    // Стан завантаження (щоб показувати крутилку в UI)
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadBands()
    }

    private fun loadBands() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Звертаємось до Supabase через репозиторій
                _bands.value = bandRepository.getAllBands()
            } catch (e: Exception) {
                // Тут пізніше додамо State для показу помилки (наприклад, немає інтернету)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
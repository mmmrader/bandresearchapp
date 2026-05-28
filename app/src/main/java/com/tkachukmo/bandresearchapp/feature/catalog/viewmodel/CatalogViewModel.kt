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
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val bandRepository: BandRepository
) : ViewModel() {

    private val _bands = MutableStateFlow<List<BandDto>>(emptyList())
    val bands: StateFlow<List<BandDto>> = _bands.asStateFlow()

    // ДОДАНО: Окремий стейт для топу гуртів
    private val _topBands = MutableStateFlow<List<BandDto>>(emptyList())
    val topBands: StateFlow<List<BandDto>> = _topBands.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadBandsAndTop()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun loadBandsAndTop() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Завантажуємо підписки для верхнього списку
                _bands.value = bandRepository.getFollowedBands()

                // Завантажуємо ВСІ гурти і сортуємо їх для топу
                val allBands = bandRepository.getAllBands()
                _topBands.value = allBands.sortedByDescending { it.followersCount }

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = e.localizedMessage ?: ""

                if (e is UnknownHostException || e is ConnectException || errorMsg.contains("Unable to resolve host")) {
                    _errorMessage.value = "Немає підключення до мережі."
                } else {
                    _errorMessage.value = "Помилка завантаження даних."
                }

                // Локальний фолбек
                _bands.value = bandRepository.getCachedFollowedBands()
                val cachedAll = bandRepository.getCachedBands()
                _topBands.value = cachedAll.sortedByDescending { it.followersCount }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
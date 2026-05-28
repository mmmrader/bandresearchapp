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
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val bandRepository: BandRepository
) : ViewModel() {

    private val _recommendedBands = MutableStateFlow<List<BandDto>>(emptyList())
    val recommendedBands: StateFlow<List<BandDto>> = _recommendedBands.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ДОДАНО: Стан для повідомлень про відсутність інтернету
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadRecommendations()
    }

    // ДОДАНО: Очищення повідомлення (щоб сховати Snackbar в UI)
    fun clearError() {
        _errorMessage.value = null
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Пробуємо стягнути нові дані з Supabase
                val bands = bandRepository.getAllBands().shuffled()
                _recommendedBands.value = bands
            } catch (e: Exception) {
                // ЯКЩО НЕМАЄ ІНТЕРНЕТУ
                e.printStackTrace()
                val errorMsg = e.localizedMessage ?: ""

                // Перевіряємо, чи це саме помилка мережі
                if (e is UnknownHostException || e is ConnectException || errorMsg.contains("Unable to resolve host")) {
                    _errorMessage.value = "Немає підключення до мережі. Показано збережені дані."
                } else {
                    _errorMessage.value = "Помилка завантаження даних."
                }

                // Витягуємо збережені дані з пам'яті телефону (кешу)
                val cachedBands = bandRepository.getCachedBands().shuffled()
                _recommendedBands.value = cachedBands
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onBandSwiped(bandId: String) {
        val currentList = _recommendedBands.value.toMutableList()
        currentList.removeAll { it.id == bandId }
        _recommendedBands.value = currentList
    }
}
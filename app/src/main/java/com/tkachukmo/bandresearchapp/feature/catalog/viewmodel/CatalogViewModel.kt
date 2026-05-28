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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ДОДАНО: Стан для повідомлень про відсутність інтернету
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadBands()
    }

    // ДОДАНО: Очищення повідомлення після показу
    fun clearError() {
        _errorMessage.value = null
    }

    private fun loadBands() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Пробуємо завантажити нові дані з Supabase
                _bands.value = bandRepository.getAllBands()
            } catch (e: Exception) {
                // ОБРОБКА ВІДСУТНОСТІ ІНТЕРНЕТУ
                e.printStackTrace()
                val errorMsg = e.localizedMessage ?: ""

                if (e is UnknownHostException || e is ConnectException || errorMsg.contains("Unable to resolve host")) {
                    _errorMessage.value = "Немає підключення до мережі."
                } else {
                    _errorMessage.value = "Помилка завантаження даних."
                }

                // Якщо впали з помилкою, дістаємо дані з локального кешу
                _bands.value = bandRepository.getCachedBands()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
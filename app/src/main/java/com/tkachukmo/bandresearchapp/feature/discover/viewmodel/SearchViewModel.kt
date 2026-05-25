package com.tkachukmo.bandresearchapp.feature.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.BandRepository
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bandRepository: BandRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<BandDto>>(emptyList())
    val searchResults: StateFlow<List<BandDto>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Історія пошуку (FR-22) - поки зберігаємо в пам'яті
    private val _searchHistory = MutableStateFlow<List<String>>(listOf("Океан Ельзи", "Рок"))
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel() // Скасовуємо попередній запит, якщо користувач продовжує друкувати

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Чекаємо півсекунди (Debounce) перед запитом до Supabase
            _isLoading.value = true
            try {
                _searchResults.value = bandRepository.searchBands(query)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToHistory(query: String) {
        if (query.isNotBlank() && !_searchHistory.value.contains(query)) {
            val current = _searchHistory.value.toMutableList()
            current.add(0, query) // Додаємо на початок
            if (current.size > 10) current.removeLast() // Зберігаємо тільки 10 останніх запитів
            _searchHistory.value = current
        }
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
    }
}
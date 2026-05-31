package com.tkachukmo.bandresearchapp.feature.discover.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.BandRepository
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bandRepository: BandRepository,
    @ApplicationContext private val context: Context // ДОДАНО: Контекст для доступу до пам'яті
) : ViewModel() {

    private val prefs = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<BandDto>>(emptyList())
    val searchResults: StateFlow<List<BandDto>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
    }

    private fun loadSearchHistory() {
        val savedHistory = prefs.getString("history", "") ?: ""
        if (savedHistory.isNotBlank()) {
            _searchHistory.value = savedHistory.split("||")
        }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _isLoading.value = true
            try {
                _searchResults.value = bandRepository.searchBands(query)
            } catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun addToHistory(query: String) {
        val q = query.trim()
        if (q.isNotBlank()) {
            val current = _searchHistory.value.toMutableList()
            current.remove(q) // Видаляємо, якщо такий вже був (щоб перемістити нагору)
            current.add(0, q)
            if (current.size > 10) current.removeLast() // Зберігаємо тільки 10 останніх

            _searchHistory.value = current
            // Зберігаємо в пам'ять телефону
            prefs.edit().putString("history", current.joinToString("||")).apply()
        }
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
        prefs.edit().remove("history").apply() // Видаляємо з пам'яті
    }
}
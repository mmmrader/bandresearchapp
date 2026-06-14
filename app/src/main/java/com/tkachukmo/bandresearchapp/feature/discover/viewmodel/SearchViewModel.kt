package com.tkachukmo.bandresearchapp.feature.discover.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.BandRepository
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bandRepository: BandRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
    private val historyJson = Json { ignoreUnknownKeys = true }

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
        val savedHistory = prefs.getString(HISTORY_KEY, "") ?: ""
        if (savedHistory.isBlank()) return

        _searchHistory.value = runCatching {
            historyJson.decodeFromString<List<String>>(savedHistory)
        }.getOrElse {
            savedHistory.split(LEGACY_HISTORY_SEPARATOR)
        }.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .take(MAX_HISTORY_ITEMS)
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()

        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            _searchResults.value = emptyList()
            _isLoading.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _isLoading.value = true
            try {
                _searchResults.value = bandRepository.searchBands(normalizedQuery)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = bandRepository.getCachedSearchResults(normalizedQuery)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToHistory(query: String) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return

        val updatedHistory = _searchHistory.value
            .filterNot { it.equals(normalizedQuery, ignoreCase = true) }
            .toMutableList()
            .apply { add(0, normalizedQuery) }
            .take(MAX_HISTORY_ITEMS)

        _searchHistory.value = updatedHistory
        prefs.edit().putString(HISTORY_KEY, historyJson.encodeToString(updatedHistory)).apply()
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
        prefs.edit().remove(HISTORY_KEY).apply()
    }

    private companion object {
        const val HISTORY_KEY = "history"
        const val LEGACY_HISTORY_SEPARATOR = "||"
        const val MAX_HISTORY_ITEMS = 10
        const val SEARCH_DEBOUNCE_MS = 500L
    }
}

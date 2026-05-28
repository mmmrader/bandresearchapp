package com.tkachukmo.bandresearchapp.feature.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.FollowDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import com.tkachukmo.bandresearchapp.data.remote.dto.VideoDto
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BandDetailViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _band = MutableStateFlow<BandDto?>(null)
    val band: StateFlow<BandDto?> = _band.asStateFlow()

    private val _tracks = MutableStateFlow<List<TrackDto>>(emptyList())
    val tracks: StateFlow<List<TrackDto>> = _tracks.asStateFlow()

    private val _videos = MutableStateFlow<List<VideoDto>>(emptyList())
    val videos: StateFlow<List<VideoDto>> = _videos.asStateFlow()

    // ДОДАНО: Стан підписки
    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }

    fun loadBandDetails(bandId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val currentBand = supabaseClient.postgrest["bands"].select { filter { eq("id", bandId) } }.decodeSingleOrNull<BandDto>()
                _band.value = currentBand

                if (currentBand != null) {
                    _tracks.value = supabaseClient.postgrest["tracks"].select { filter { eq("band_id", bandId) } }.decodeList<TrackDto>()
                    _videos.value = supabaseClient.postgrest["videos"].select { filter { eq("band_id", bandId) } }.decodeList<VideoDto>()

                    // ДОДАНО: Перевіряємо, чи підписаний користувач
                    checkIfFollowing(bandId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Помилка завантаження даних"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun checkIfFollowing(bandId: String) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        try {
            val follows = supabaseClient.postgrest["follows"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("band_id", bandId)
                    }
                }.decodeList<FollowDto>()
            _isFollowing.value = follows.isNotEmpty()
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun toggleFollow() {
        val bandId = _band.value?.id ?: return
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
            if (userId == null) {
                _errorMessage.value = "Увійдіть в акаунт, щоб підписатись"
                return@launch
            }

            try {
                if (_isFollowing.value) {
                    // Відписуємось
                    supabaseClient.postgrest["follows"].delete {
                        filter {
                            eq("user_id", userId)
                            eq("band_id", bandId)
                        }
                    }
                    _isFollowing.value = false
                    // Оптимістично зменшуємо лічильник у UI
                    _band.value = _band.value?.copy(followersCount = maxOf(0, (_band.value?.followersCount ?: 0) - 1))
                } else {
                    // Підписуємось
                    val newFollow = FollowDto(userId, bandId)
                    supabaseClient.postgrest["follows"].insert(newFollow)
                    _isFollowing.value = true
                    // Оптимістично збільшуємо лічильник у UI
                    _band.value = _band.value?.copy(followersCount = (_band.value?.followersCount ?: 0) + 1)
                }

                // ВАЖЛИВО: Оновлюємо лічильник у самій базі bands
                _band.value?.let { currentBand ->
                    supabaseClient.postgrest["bands"].update(
                        mapOf("followers_count" to currentBand.followersCount)
                    ) { filter { eq("id", currentBand.id) } }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Помилка підписки: ${e.message}"
            }
        }
    }
}
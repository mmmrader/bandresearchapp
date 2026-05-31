package com.tkachukmo.bandresearchapp.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.dto.HistoryDto
import com.tkachukmo.bandresearchapp.data.remote.dto.HistoryTrackUI
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// DTO для join: listen_history -> tracks -> bands
@Serializable
private data class HistoryWithTrackDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("track_id") val trackId: String,
    @SerialName("listened_at") val listenedAt: String,
    val tracks: TrackJoinDto? = null
)

@Serializable
private data class TrackJoinDto(
    val id: String,
    val title: String,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("duration_sec") val durationSec: Int = 0,
    @SerialName("band_id") val bandId: String = "",
    val bands: BandJoinDto? = null
)

@Serializable
private data class BandJoinDto(
    val id: String,
    val name: String
)

private const val MAX_HISTORY_ITEMS = 20

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _historyItems = MutableStateFlow<List<HistoryTrackUI>>(emptyList())
    val historyItems: StateFlow<List<HistoryTrackUI>> = _historyItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHistory()
    }

    // ================================================================
    // ЗАВАНТАЖЕННЯ ІСТОРІЇ
    // Supabase PostgREST join синтаксис:
    //   select("*, tracks(id, title, cover_url, duration_sec, band_id, bands(id, name))")
    // ================================================================

    fun loadHistory() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val rows = supabaseClient.postgrest["listen_history"]
                    .select(
                        columns = io.github.jan.supabase.postgrest.query.Columns.raw(
                            "*, tracks(id, title, cover_url, duration_sec, band_id, bands(id, name))"
                        )
                    ) {
                        filter { eq("user_id", userId) }
                        order("listened_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(MAX_HISTORY_ITEMS.toLong())
                    }
                    .decodeList<HistoryWithTrackDto>()

                _historyItems.value = rows.mapNotNull { row ->
                    val track = row.tracks ?: return@mapNotNull null
                    HistoryTrackUI(
                        historyId  = row.id ?: row.trackId,
                        trackId    = row.trackId,
                        trackTitle = track.title,
                        bandName   = track.bands?.name ?: "Невідомий гурт",
                        coverUrl   = track.coverUrl,
                        listenedAt = row.listenedAt,
                        durationSec = track.durationSec
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ================================================================
    // ОЧИЩЕННЯ ІСТОРІЇ
    // ================================================================

    fun clearHistory() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["listen_history"]
                    .delete { filter { eq("user_id", userId) } }
                _historyItems.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
package com.tkachukmo.bandresearchapp.feature.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.BandEventDto
import com.tkachukmo.bandresearchapp.data.remote.dto.EventCommentDto
import com.tkachukmo.bandresearchapp.data.remote.dto.EventCommentInsertDto
import com.tkachukmo.bandresearchapp.data.remote.dto.EventReactionDto
import com.tkachukmo.bandresearchapp.data.remote.dto.FollowDto
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
class EventsViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    // Тільки події від підписаних гуртів
    private val _events = MutableStateFlow<List<BandEventDto>>(emptyList())
    val events: StateFlow<List<BandEventDto>> = _events.asStateFlow()

    // Гурти на які підписаний користувач
    private val _followedBands = MutableStateFlow<List<BandDto>>(emptyList())
    val followedBands: StateFlow<List<BandDto>> = _followedBands.asStateFlow()

    // ID подій які лайкнув юзер
    private val _likedEventIds = MutableStateFlow<Set<String>>(emptySet())
    val likedEventIds: StateFlow<Set<String>> = _likedEventIds.asStateFlow()

    // ID подій на які RSVP («Піду»)
    private val _rsvpEventIds = MutableStateFlow<Set<String>>(emptySet())
    val rsvpEventIds: StateFlow<Set<String>> = _rsvpEventIds.asStateFlow()

    // Коментарі: eventId -> список коментарів
    private val _comments = MutableStateFlow<Map<String, List<EventCommentDto>>>(emptyMap())
    val comments: StateFlow<Map<String, List<EventCommentDto>>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadFeed()
    }

    fun clearMessage() {
        _message.value = null
    }

    // ==========================================
    // ЗАВАНТАЖЕННЯ СТРІЧКИ ПІДПИСОК
    // ==========================================

    fun loadFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                    ?: run { _isLoading.value = false; return@launch }

                // 1. Отримуємо підписки користувача
                val follows = supabaseClient.postgrest["follows"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<FollowDto>()

                val bandIds = follows.map { it.bandId }

                if (bandIds.isEmpty()) {
                    _followedBands.value = emptyList()
                    _events.value = emptyList()
                    return@launch
                }

                // 2. Завантажуємо гурти підписок
                val bands = supabaseClient.postgrest["bands"]
                    .select { filter { isIn("id", bandIds) } }
                    .decodeList<BandDto>()
                _followedBands.value = bands

                // 3. Завантажуємо події ТІЛЬКИ від підписаних гуртів
                val allEvents = supabaseClient.postgrest["band_events"]
                    .select { filter { isIn("band_id", bandIds) } }
                    .decodeList<BandEventDto>()
                    .sortedByDescending { it.createdAt ?: it.eventDate ?: "" }

                // Додаємо назву гурту до події якщо її немає
                val bandsById = bands.associateBy { it.id }
                _events.value = allEvents.map { event ->
                    if (event.bandName == null) {
                        event.copy(bandName = bandsById[event.bandId]?.name)
                    } else event
                }

                // 4. Завантажуємо лайки та RSVP поточного юзера
                loadUserReactions(userId, allEvents.map { it.id })

            } catch (e: Exception) {
                e.printStackTrace()
                _message.value = "Помилка завантаження стрічки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadUserReactions(userId: String, eventIds: List<String>) {
        if (eventIds.isEmpty()) return
        try {
            val liked = supabaseClient.postgrest["event_likes"]
                .select { filter { eq("user_id", userId); isIn("event_id", eventIds) } }
                .decodeList<EventReactionDto>()
            _likedEventIds.value = liked.map { it.eventId }.toSet()
        } catch (_: Exception) {}

        try {
            val rsvp = supabaseClient.postgrest["event_rsvp"]
                .select { filter { eq("user_id", userId); isIn("event_id", eventIds) } }
                .decodeList<EventReactionDto>()
            _rsvpEventIds.value = rsvp.map { it.eventId }.toSet()
        } catch (_: Exception) {}
    }

    // ==========================================
    // ЛАЙК
    // ==========================================

    fun toggleLike(eventId: String) {
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            try {
                val isLiked = eventId in _likedEventIds.value
                if (isLiked) {
                    supabaseClient.postgrest["event_likes"].delete {
                        filter { eq("event_id", eventId); eq("user_id", userId) }
                    }
                    _likedEventIds.value = _likedEventIds.value - eventId
                    updateEventLikesCount(eventId, -1)
                } else {
                    supabaseClient.postgrest["event_likes"].insert(
                        EventReactionDto(eventId = eventId, userId = userId)
                    )
                    _likedEventIds.value = _likedEventIds.value + eventId
                    updateEventLikesCount(eventId, +1)
                }
            } catch (e: Exception) {
                _message.value = "Помилка: ${e.message}"
            }
        }
    }

    private fun updateEventLikesCount(eventId: String, delta: Int) {
        _events.value = _events.value.map { event ->
            if (event.id == eventId) event.copy(likesCount = maxOf(0, event.likesCount + delta))
            else event
        }
    }

    // ==========================================
    // RSVP — «Піду»
    // ==========================================

    fun toggleRsvp(eventId: String) {
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            try {
                val isGoing = eventId in _rsvpEventIds.value
                if (isGoing) {
                    supabaseClient.postgrest["event_rsvp"].delete {
                        filter { eq("event_id", eventId); eq("user_id", userId) }
                    }
                    _rsvpEventIds.value = _rsvpEventIds.value - eventId
                    updateEventRsvpCount(eventId, -1)
                    _message.value = "Скасовано RSVP"
                } else {
                    supabaseClient.postgrest["event_rsvp"].insert(
                        EventReactionDto(eventId = eventId, userId = userId)
                    )
                    _rsvpEventIds.value = _rsvpEventIds.value + eventId
                    updateEventRsvpCount(eventId, +1)
                    _message.value = "Відмічено «Піду»"
                }
            } catch (e: Exception) {
                _message.value = "Помилка: ${e.message}"
            }
        }
    }

    private fun updateEventRsvpCount(eventId: String, delta: Int) {
        _events.value = _events.value.map { event ->
            if (event.id == eventId) event.copy(rsvpCount = maxOf(0, event.rsvpCount + delta))
            else event
        }
    }

    // ==========================================
    // КОМЕНТАРІ
    // ==========================================

    fun loadComments(eventId: String) {
        viewModelScope.launch {
            try {
                val list = supabaseClient.postgrest["event_comments"]
                    .select { filter { eq("event_id", eventId) } }
                    .decodeList<EventCommentDto>()
                    .sortedBy { it.createdAt }
                _comments.value = _comments.value + (eventId to list)
            } catch (e: Exception) {
                _message.value = "Не вдалося завантажити коментарі"
            }
        }
    }

    fun addComment(eventId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            try {
                supabaseClient.postgrest["event_comments"].insert(
                    EventCommentInsertDto(eventId = eventId, userId = userId, text = text)
                )
                // Оновлюємо лічильник коментарів локально
                _events.value = _events.value.map { event ->
                    if (event.id == eventId) event.copy(commentsCount = event.commentsCount + 1)
                    else event
                }
                loadComments(eventId)
            } catch (e: Exception) {
                _message.value = "Не вдалося надіслати коментар"
            }
        }
    }
}
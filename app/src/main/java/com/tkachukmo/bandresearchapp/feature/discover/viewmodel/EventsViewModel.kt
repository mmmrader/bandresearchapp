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

    private val _events = MutableStateFlow<List<BandEventDto>>(emptyList())
    val events: StateFlow<List<BandEventDto>> = _events.asStateFlow()

    private val _followedBands = MutableStateFlow<List<BandDto>>(emptyList())
    val followedBands: StateFlow<List<BandDto>> = _followedBands.asStateFlow()

    private val _comments = MutableStateFlow<Map<String, List<EventCommentDto>>>(emptyMap())
    val comments: StateFlow<Map<String, List<EventCommentDto>>> = _comments.asStateFlow()

    private val _likedEventIds = MutableStateFlow<Set<String>>(emptySet())
    val likedEventIds: StateFlow<Set<String>> = _likedEventIds.asStateFlow()

    private val _rsvpEventIds = MutableStateFlow<Set<String>>(emptySet())
    val rsvpEventIds: StateFlow<Set<String>> = _rsvpEventIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadFeed()
    }

    fun clearMessage() {
        _message.value = null
    }

    fun loadFeed() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId == null) {
                    _events.value = fallbackEvents
                    return@launch
                }

                val follows = supabaseClient.postgrest["follows"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<FollowDto>()

                val bandIds = follows.map { it.bandId }
                if (bandIds.isEmpty()) {
                    _followedBands.value = emptyList()
                    _events.value = emptyList()
                    return@launch
                }

                _followedBands.value = supabaseClient.postgrest["bands"]
                    .select { filter { isIn("id", bandIds) } }
                    .decodeList<BandDto>()

                val loadedEvents = supabaseClient.postgrest["band_events"]
                    .select { filter { isIn("band_id", bandIds) } }
                    .decodeList<BandEventDto>()
                    .sortedByDescending { it.createdAt ?: it.eventDate ?: "" }

                _events.value = loadedEvents

                _likedEventIds.value = supabaseClient.postgrest["event_likes"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<EventReactionDto>()
                    .map { it.eventId }
                    .toSet()

                _rsvpEventIds.value = supabaseClient.postgrest["event_rsvps"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<EventReactionDto>()
                    .map { it.eventId }
                    .toSet()
            } catch (t: Throwable) {
                _events.value = fallbackEvents
                _message.value = "Показано приклад стрічки. Перевірте таблиці band_events, event_likes, event_comments та event_rsvps."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(eventId: String) {
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            val isLiked = eventId in _likedEventIds.value
            _likedEventIds.value = if (isLiked) _likedEventIds.value - eventId else _likedEventIds.value + eventId
            _events.value = _events.value.map { event ->
                if (event.id == eventId) {
                    event.copy(likesCount = (event.likesCount + if (isLiked) -1 else 1).coerceAtLeast(0))
                } else event
            }
            try {
                if (isLiked) {
                    supabaseClient.postgrest["event_likes"].delete {
                        filter { eq("event_id", eventId); eq("user_id", userId) }
                    }
                } else {
                    supabaseClient.postgrest["event_likes"].insert(EventReactionDto(eventId, userId))
                }
            } catch (_: Throwable) {
                _message.value = "Реакцію збережено локально для цього сеансу."
            }
        }
    }

    fun toggleRsvp(eventId: String) {
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            val isGoing = eventId in _rsvpEventIds.value
            _rsvpEventIds.value = if (isGoing) _rsvpEventIds.value - eventId else _rsvpEventIds.value + eventId
            _events.value = _events.value.map { event ->
                if (event.id == eventId) {
                    event.copy(rsvpCount = (event.rsvpCount + if (isGoing) -1 else 1).coerceAtLeast(0))
                } else event
            }
            try {
                if (isGoing) {
                    supabaseClient.postgrest["event_rsvps"].delete {
                        filter { eq("event_id", eventId); eq("user_id", userId) }
                    }
                } else {
                    supabaseClient.postgrest["event_rsvps"].insert(EventReactionDto(eventId, userId))
                }
            } catch (_: Throwable) {
                _message.value = "Позначку RSVP збережено локально для цього сеансу."
            }
        }
    }

    fun loadComments(eventId: String) {
        viewModelScope.launch {
            try {
                val eventComments = supabaseClient.postgrest["event_comments"]
                    .select { filter { eq("event_id", eventId) } }
                    .decodeList<EventCommentDto>()
                _comments.value = _comments.value + (eventId to eventComments)
            } catch (_: Throwable) {
                _comments.value = _comments.value + (eventId to emptyList())
            }
        }
    }

    fun addComment(eventId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
            val localComment = EventCommentDto(
                id = "local-${System.currentTimeMillis()}",
                eventId = eventId,
                userId = userId,
                authorName = "Ви",
                text = text.trim()
            )
            _comments.value = _comments.value + (eventId to ((_comments.value[eventId] ?: emptyList()) + localComment))
            _events.value = _events.value.map {
                if (it.id == eventId) it.copy(commentsCount = it.commentsCount + 1) else it
            }
            try {
                supabaseClient.postgrest["event_comments"].insert(EventCommentInsertDto(eventId, userId, text.trim()))
            } catch (_: Throwable) {
                _message.value = "Коментар додано локально для цього сеансу."
            }
        }
    }
}

private val fallbackEvents = listOf(
    BandEventDto(
        id = "demo-release",
        bandId = "demo-1",
        bandName = "The Unsleeping",
        title = "Новий сингл: City Noise",
        description = "Автоматична подія релізу з посиланнями на стримінги.",
        type = "release",
        smartLink = "https://open.spotify.com",
        spotifyUrl = "https://open.spotify.com",
        appleMusicUrl = "https://music.apple.com",
        youtubeMusicUrl = "https://music.youtube.com",
        likesCount = 24,
        commentsCount = 3,
        createdAt = "2026-05-28T10:00:00Z"
    ),
    BandEventDto(
        id = "demo-show",
        bandId = "demo-2",
        bandName = "North Stage",
        title = "Живий виступ у Docker Pub",
        description = "Концерт для підписників з RSVP.",
        type = "concert",
        eventDate = "2026-06-12",
        venue = "Docker Pub",
        city = "Київ",
        likesCount = 58,
        rsvpCount = 17,
        createdAt = "2026-05-27T18:00:00Z"
    ),
    BandEventDto(
        id = "demo-video",
        bandId = "demo-3",
        bandName = "Signal Bloom",
        title = "Прем'єра кліпу на YouTube",
        description = "Відеореліз автоматично потрапив у стрічку.",
        type = "video",
        youtubeMusicUrl = "https://music.youtube.com",
        likesCount = 41,
        commentsCount = 6,
        createdAt = "2026-05-26T12:00:00Z"
    )
)

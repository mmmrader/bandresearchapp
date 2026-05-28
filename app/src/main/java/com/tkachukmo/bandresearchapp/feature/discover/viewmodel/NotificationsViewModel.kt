package com.tkachukmo.bandresearchapp.feature.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.dto.NotificationDto
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
class NotificationsViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val notifications: StateFlow<List<NotificationDto>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                _notifications.value = supabaseClient.postgrest["notifications"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<NotificationDto>()
                    .sortedByDescending { it.createdAt ?: "" }
            } catch (_: Throwable) {
                _notifications.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            _notifications.value = _notifications.value.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
            runCatching {
                supabaseClient.postgrest["notifications"].update({ set("is_read", true) }) {
                    filter { eq("id", notificationId) }
                }
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            val ids = _notifications.value.map { it.id }
            _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            runCatching {
                ids.forEach { id ->
                    supabaseClient.postgrest["notifications"].update({ set("is_read", true) }) {
                        filter { eq("id", id) }
                    }
                }
            }
        }
    }
}

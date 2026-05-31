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

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                _notifications.value = supabaseClient.postgrest["notifications"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<NotificationDto>()
                    .sortedByDescending { it.createdAt ?: "" }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["notifications"].update(
                    { set("is_read", true) }
                ) { filter { eq("id", notificationId) } }
                _notifications.value = _notifications.value.map { n ->
                    if (n.id == notificationId) n.copy(isRead = true) else n
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["notifications"].update(
                    { set("is_read", true) }
                ) { filter { eq("user_id", userId); eq("is_read", false) } }
                _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
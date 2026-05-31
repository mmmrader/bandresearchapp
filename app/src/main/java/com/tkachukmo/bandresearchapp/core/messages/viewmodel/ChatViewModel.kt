package com.tkachukmo.bandresearchapp.core.messages.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tkachukmo.bandresearchapp.data.remote.dto.ChatMessageDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ChatMessageInsertDto
import com.tkachukmo.bandresearchapp.data.remote.dto.NotificationInsertDto
import com.tkachukmo.bandresearchapp.data.remote.dto.ProfileDto
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Модель для відображення одного чату у списку чатів.
 * chatId = ID співрозмовника (recipient/sender ID)
 */
data class ChatPreview(
    val chatPartnerId: String,
    val chatPartnerName: String,
    val chatPartnerAvatar: String?,
    val lastMessage: String,
    val lastMessageTime: String?,
    val unreadCount: Int = 0
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    // ---- Список чатів ----
    private val _chatList = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chatList: StateFlow<List<ChatPreview>> = _chatList.asStateFlow()

    // ---- Повідомлення у конкретному чаті ----
    private val _messages = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val messages: StateFlow<List<ChatMessageDto>> = _messages.asStateFlow()

    // ---- Профіль співрозмовника ----
    private val _partnerProfile = MutableStateFlow<ProfileDto?>(null)
    val partnerProfile: StateFlow<ProfileDto?> = _partnerProfile.asStateFlow()

    // ---- Стан завантаження ----
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ---- Поточний UserID ----
    val currentUserId: String?
        get() = supabaseClient.auth.currentUserOrNull()?.id

    // ---- Polling job для live-оновлень ----
    private var pollingJob: Job? = null
    private var currentChatPartnerId: String? = null

    // ================================================================
    // СПИСОК ЧАТІВ
    // ================================================================

    /**
     * Завантажує список всіх чатів поточного користувача.
     * Чат є унікальним за парою (я ↔ співрозмовник).
     */
    fun loadChatList() {
        val myId = currentUserId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Отримуємо всі повідомлення де я відправник або отримувач
                val allMessages = supabaseClient.postgrest["chat_messages"]
                    .select()
                    .decodeList<ChatMessageDto>()
                    .filter { it.senderId == myId || it.recipientId == myId }
                    .sortedByDescending { it.createdAt ?: "" }

                // Будуємо унікальні чати (по partnerId)
                val partnerIds = allMessages
                    .map { if (it.senderId == myId) it.recipientId else it.senderId }
                    .distinct()

                val previews = mutableListOf<ChatPreview>()

                for (partnerId in partnerIds) {
                    val lastMsg = allMessages.first {
                        (it.senderId == myId && it.recipientId == partnerId) ||
                                (it.senderId == partnerId && it.recipientId == myId)
                    }

                    // Завантажуємо профіль партнера
                    val profile = runCatching {
                        supabaseClient.postgrest["profiles"]
                            .select { filter { eq("id", partnerId) } }
                            .decodeSingleOrNull<ProfileDto>()
                    }.getOrNull()

                    previews.add(
                        ChatPreview(
                            chatPartnerId = partnerId,
                            chatPartnerName = profile?.displayName ?: "Кандидат: ${partnerId.take(8)}",
                            chatPartnerAvatar = profile?.avatarUrl,
                            lastMessage = lastMsg.text,
                            lastMessageTime = lastMsg.createdAt
                        )
                    )
                }

                _chatList.value = previews
            } catch (e: Exception) {
                _errorMessage.value = "Помилка завантаження чатів: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ================================================================
    // ПОВІДОМЛЕННЯ У ЧАТІ
    // ================================================================

    /**
     * Завантажує повідомлення з конкретним партнером і запускає polling.
     */
    fun openChat(partnerId: String) {
        currentChatPartnerId = partnerId
        loadMessages(partnerId)
        loadPartnerProfile(partnerId)
        startPolling(partnerId)
    }

    fun closeChat() {
        stopPolling()
        currentChatPartnerId = null
        _messages.value = emptyList()
        _partnerProfile.value = null
    }

    private fun loadMessages(partnerId: String) {
        val myId = currentUserId ?: return
        viewModelScope.launch {
            try {
                val msgs = supabaseClient.postgrest["chat_messages"]
                    .select()
                    .decodeList<ChatMessageDto>()
                    .filter {
                        (it.senderId == myId && it.recipientId == partnerId) ||
                                (it.senderId == partnerId && it.recipientId == myId)
                    }
                    .sortedBy { it.createdAt ?: "" }
                _messages.value = msgs
            } catch (e: Exception) {
                // Тихо логуємо
                e.printStackTrace()
            }
        }
    }

    private fun loadPartnerProfile(partnerId: String) {
        viewModelScope.launch {
            try {
                _partnerProfile.value = supabaseClient.postgrest["profiles"]
                    .select { filter { eq("id", partnerId) } }
                    .decodeSingleOrNull<ProfileDto>()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Надсилає повідомлення поточному співрозмовнику.
     */
    fun sendMessage(partnerId: String, text: String, onSent: () -> Unit = {}) {
        if (text.isBlank()) return
        val myId = currentUserId ?: return

        viewModelScope.launch {
            try {
                supabaseClient.postgrest["chat_messages"].insert(
                    ChatMessageInsertDto(
                        senderId = myId,
                        recipientId = partnerId,
                        text = text
                    )
                )
                // Надсилаємо сповіщення партнеру
                runCatching {
                    supabaseClient.postgrest["notifications"].insert(
                        NotificationInsertDto(
                            userId = partnerId,
                            type = "message",
                            title = "Нове повідомлення",
                            body = text.take(80)
                        )
                    )
                }
                loadMessages(partnerId)
                loadChatList()
                onSent()
            } catch (e: Exception) {
                _errorMessage.value = "Помилка надсилання: ${e.message}"
            }
        }
    }

    // ================================================================
    // POLLING (оновлення кожні 5 секунд)
    // ================================================================

    private fun startPolling(partnerId: String) {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                loadMessages(partnerId)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
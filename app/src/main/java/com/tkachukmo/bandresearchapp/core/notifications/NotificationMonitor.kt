package com.tkachukmo.bandresearchapp.core.notifications

import android.content.Context
import android.util.Log
import com.tkachukmo.bandresearchapp.data.remote.dto.NotificationDto
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationMonitor @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val settingsRepository: NotificationSettingsRepository,
    private val appNotificationManager: AppNotificationManager,
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("shown_notifications", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        Log.d("NotifDebug", "🟢 Монітор сповіщень ЗАПУЩЕНО")
        job = scope.launch {
            while (isActive) {
                checkForNotifications()
                delay(30_000)
            }
        }
    }

    fun stop() {
        Log.d("NotifDebug", "🔴 Монітор сповіщень ЗУПИНЕНО")
        job?.cancel()
        job = null
    }

    private suspend fun checkForNotifications() {
        Log.d("NotifDebug", "🔄 Перевірка сповіщень...")
        val userId = supabaseClient.auth.currentUserOrNull()?.id
        if (userId == null) {
            Log.d("NotifDebug", "❌ Юзер не авторизований, скасування")
            return
        }

        val unread = runCatching {
            supabaseClient.postgrest["notifications"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_read", false)
                    }
                }
                .decodeList<NotificationDto>()
        }.getOrElse {
            Log.e("NotifDebug", "❌ Помилка запиту до БД: ${it.message}")
            return
        }

        Log.d("NotifDebug", "📥 Знайдено в базі (непрочитаних): ${unread.size}")
        if (unread.isEmpty()) return

        val shownIds = prefs.getStringSet("shown_ids", emptySet()).orEmpty().toMutableSet()
        val newUnread = unread.filterNot { it.id in shownIds }

        Log.d("NotifDebug", "👀 З них АБСОЛЮТНО нових (не в кеші): ${newUnread.size}")
        if (newUnread.isEmpty()) return

        val isEnabled = settingsRepository.enabled.first()
        Log.d("NotifDebug", "⚙️ Налаштування сповіщень у профілі увімкнено: $isEnabled")

        if (isEnabled) {
            newUnread.forEach {
                Log.d("NotifDebug", "🚀 ВІДПРАВКА ПУША СИСТЕМІ: ${it.title}")
                appNotificationManager.show(it)
            }
        }

        prefs.edit()
            .putStringSet("shown_ids", shownIds.apply { addAll(newUnread.map { it.id }) })
            .apply()
    }
}
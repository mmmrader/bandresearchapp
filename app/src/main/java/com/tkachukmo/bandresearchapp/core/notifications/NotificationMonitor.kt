package com.tkachukmo.bandresearchapp.core.notifications

import android.content.Context
import com.tkachukmo.bandresearchapp.data.remote.dto.NotificationDto
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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

        job = scope.launch {
            while (isActive) {
                checkForNotifications()
                delay(30_000)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun checkForNotifications() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        val unread = runCatching {
            supabaseClient.postgrest["notifications"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("is_read", false)
                    }
                }
                .decodeList<NotificationDto>()
        }.getOrElse { return }

        if (unread.isEmpty()) return

        val shownIds = prefs.getStringSet(KEY_SHOWN_IDS, emptySet()).orEmpty().toMutableSet()
        val newUnread = unread.filterNot { it.id in shownIds }
        if (newUnread.isEmpty()) return

        if (settingsRepository.enabled.first()) {
            newUnread.forEach { appNotificationManager.show(it) }
        }

        prefs.edit()
            .putStringSet(KEY_SHOWN_IDS, shownIds.apply { addAll(newUnread.map { it.id }) })
            .apply()
    }

    companion object {
        private const val KEY_SHOWN_IDS = "shown_ids"
    }
}

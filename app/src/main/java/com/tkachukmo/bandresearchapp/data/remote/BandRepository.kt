package com.tkachukmo.bandresearchapp.data.remote

import android.content.Context
import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BandRepository @Inject constructor(
    private val supabase: SupabaseClient,
    @ApplicationContext private val context: Context // Context для доступу до пам'яті телефону (SharedPreferences)
) {
    // Створюємо локальне сховище
    private val prefs = context.getSharedPreferences("band_cache_prefs", Context.MODE_PRIVATE)

    suspend fun getTrackById(trackId: String): TrackDto? {
        return supabase.postgrest["tracks"].select { filter { eq("id", trackId) } }.decodeSingleOrNull<TrackDto>()
    }

    // --- ОНОВЛЕНО: Завантаження конкретного гурту з кешуванням ---
    suspend fun getBandById(bandId: String): BandDto? {
        return try {
            val band = supabase.postgrest["bands"].select { filter { eq("id", bandId) } }.decodeSingleOrNull<BandDto>()
            if (band != null) {
                // Якщо завантажили успішно, зберігаємо в пам'ять
                val jsonString = Json.encodeToString(band)
                prefs.edit().putString("cached_band_$bandId", jsonString).apply()
            }
            band
        } catch (e: Exception) {
            throw e // Прокидаємо помилку далі у ViewModel
        }
    }

    // --- НОВА ФУНКЦІЯ: Дістаємо конкретний гурт з кешу (якщо немає інтернету) ---
    fun getCachedBandById(bandId: String): BandDto? {
        val cachedJson = prefs.getString("cached_band_$bandId", null) ?: return null
        return try { Json.decodeFromString(cachedJson) } catch (e: Exception) { null }
    }

    // --- ОНОВЛЕНО: Завантаження всіх гуртів з кешуванням ---
    suspend fun getAllBands(): List<BandDto> {
        try {
            val bands = supabase.postgrest["bands"].select().decodeList<BandDto>()
            val jsonString = Json.encodeToString(bands)
            prefs.edit().putString("cached_all_bands", jsonString).apply()
            return bands
        } catch (e: Exception) {
            throw e
        }
    }

    // --- НОВА ФУНКЦІЯ: Дістаємо всі гурти з кешу ---
    fun getCachedBands(): List<BandDto> {
        val cachedJson = prefs.getString("cached_all_bands", null) ?: return emptyList()
        return try {
            Json.decodeFromString(cachedJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchBands(query: String): List<BandDto> {
        return supabase.postgrest["bands"].select {
            filter { or { ilike("name", "%$query%"); ilike("description", "%$query%") } }
        }.decodeList<BandDto>()
    }

    // --- ОНОВЛЕНО: Завантаження треків гурту з кешуванням ---
    suspend fun getTracksByBand(bandId: String): List<TrackDto> {
        return try {
            val tracks = supabase.postgrest["tracks"].select { filter { eq("band_id", bandId) } }.decodeList<TrackDto>()
            // Зберігаємо треки в пам'ять
            val jsonString = Json.encodeToString(tracks)
            prefs.edit().putString("cached_tracks_$bandId", jsonString).apply()
            tracks
        } catch (e: Exception) {
            throw e
        }
    }

    // --- НОВА ФУНКЦІЯ: Дістаємо треки гурту з кешу ---
    fun getCachedTracksByBand(bandId: String): List<TrackDto> {
        val cachedJson = prefs.getString("cached_tracks_$bandId", null) ?: return emptyList()
        return try { Json.decodeFromString(cachedJson) } catch (e: Exception) { emptyList() }
    }
}
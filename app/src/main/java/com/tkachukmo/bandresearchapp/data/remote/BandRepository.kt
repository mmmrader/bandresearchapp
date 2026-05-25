package com.tkachukmo.bandresearchapp.data.remote

import com.tkachukmo.bandresearchapp.data.remote.dto.BandDto
import com.tkachukmo.bandresearchapp.data.remote.dto.TrackDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BandRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun getTrackById(trackId: String): TrackDto? {
        return supabase.postgrest["tracks"]
            .select {
                filter {
                    eq("id", trackId)
                }
            }.decodeSingleOrNull<TrackDto>()
    }
    suspend fun getBandById(bandId: String): BandDto? {
        return supabase.postgrest["bands"]
            .select {
                filter {
                    eq("id", bandId)
                }
            }.decodeSingleOrNull<BandDto>()
    }
    // Отримати всі гурти (FR-10)
    suspend fun getAllBands(): List<BandDto> {
        return supabase.postgrest["bands"].select().decodeList<BandDto>()
    }

    // Пошук гуртів (FR-20)
    suspend fun searchBands(query: String): List<BandDto> {
        return supabase.postgrest["bands"]
            .select {
                filter {
                    or {
                        ilike("name", "%$query%")
                        ilike("description", "%$query%")
                    }
                }
            }.decodeList<BandDto>()
    }

    // Отримати треки конкретного гурту (FR-11, FR-30)
    suspend fun getTracksByBand(bandId: String): List<TrackDto> {
        return supabase.postgrest["tracks"]
            .select {
                filter {
                    eq("band_id", bandId)
                }
            }.decodeList<TrackDto>()
    }
}
package com.tkachukmo.bandresearchapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BandDto(
    val id: String,
    val name: String,
    val slug: String,
    val genres: List<String>,
    val description: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    val country: String? = null,
    val city: String? = null,
    @SerialName("formed_year") val formedYear: Int? = null,
    @SerialName("manager_id") val managerId: String? = null,
    @SerialName("followers_count") val followersCount: Int = 0,
    // ДОДАНО: Кількість прослуховувань гурту
    @SerialName("plays_count") val playsCount: Int = 0
)

@Serializable
data class TrackDto(
    val id: String,
    @SerialName("band_id") val bandId: String,
    @SerialName("release_id") val releaseId: String? = null,
    val title: String,
    @SerialName("duration_sec") val durationSec: Int,
    @SerialName("audio_url") val audioUrl: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    val lyrics: String? = null,
    @SerialName("track_number") val trackNumber: Int = 1,
    @SerialName("plays_count") val playsCount: Int = 0
)
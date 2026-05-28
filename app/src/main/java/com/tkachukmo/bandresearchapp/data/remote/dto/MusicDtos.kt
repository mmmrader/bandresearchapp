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
    @SerialName("youtube_url") val youtubeUrl: String? = null,
    val country: String? = null,
    val city: String? = null,
    @SerialName("formed_year") val formedYear: Int? = null,
    @SerialName("manager_id") val managerId: String? = null,
    @SerialName("followers_count") val followersCount: Int = 0,
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

@Serializable
data class ReleaseDto(
    val id: String,
    @SerialName("band_id") val bandId: String,
    val title: String,
    @SerialName("release_type") val releaseType: String,
    @SerialName("release_year") val releaseYear: Int? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("tracks_count") val tracksCount: Int = 0
)

@Serializable
data class VideoDto(
    val id: String,
    @SerialName("band_id") val bandId: String,
    val title: String,
    @SerialName("youtube_id") val youtubeId: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("duration_sec") val durationSec: Int? = null,
    @SerialName("views_count") val viewsCount: Int = 0
)

@Serializable
data class FollowDto(
    @SerialName("user_id") val userId: String,
    @SerialName("band_id") val bandId: String
)
@Serializable
data class ProfileDto(
    val id: String,

    @SerialName("display_name")
    val displayName: String? = null,

    val bio: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("social_link")
    val socialLink: String? = null,

    @SerialName("music_genres")
    val musicGenres: List<String> = emptyList()
)
@Serializable
data class PlaylistDto(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    val name: String,

    val description: String? = null,

    @SerialName("cover_url")
    val coverUrl: String? = null,

    @SerialName("is_public")
    val isPublic: Boolean = false
)
@Serializable
data class PlaylistInsertDto(
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("is_public") val isPublic: Boolean = false
)
@Serializable
data class PlaylistTrackInsertDto(
    @SerialName("playlist_id") val playlistId: String,
    @SerialName("track_id") val trackId: String,
    val position: Int
)
@Serializable
data class PlaylistTrackDto(

    @SerialName("playlist_id")
    val playlistId: String,

    @SerialName("track_id")
    val trackId: String,

    val position: Int,

    // relation from Supabase
    val tracks: TrackDetailsDto? = null
)

@Serializable
data class TrackDetailsDto(

    val id: String,

    val title: String,

    @SerialName("cover_url")
    val coverUrl: String? = null,

    @SerialName("duration_sec")
    val durationSec: Int
)

@Serializable
data class HistoryDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("track_id") val trackId: String,
    @SerialName("listened_at") val listenedAt: String // Рядок з датою у форматі ISO/Текст
)

data class HistoryTrackUI(
    val historyId: String,
    val trackTitle: String,
    val bandName: String,
    val coverUrl: String?,
    val listenedAt: String
)
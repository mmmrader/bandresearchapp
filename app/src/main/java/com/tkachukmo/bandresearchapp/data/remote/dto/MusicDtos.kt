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
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val bio: String? = null,
    @SerialName("social_link") val socialLink: String? = null,
    @SerialName("music_genres") val musicGenres: List<String>? = null,
    val instrument: String? = null,
    val experience: String? = null,
    val location: String? = null,
    @SerialName("youtube_link") val youtubeLink: String? = null,
    @SerialName("audio_link") val audioLink: String? = null
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

@Serializable
data class HistoryUpsertDto(
    @SerialName("user_id") val userId: String,
    @SerialName("track_id") val trackId: String,
    @SerialName("listened_at") val listenedAt: String
)

data class HistoryTrackUI(
    val historyId: String,
    val trackId: String,
    val trackTitle: String,
    val bandName: String,
    val coverUrl: String?,
    val listenedAt: String,
    val durationSec: Int = 0
)

@Serializable
data class BandEventDto(
    val id: String,
    @SerialName("band_id") val bandId: String,
    @SerialName("band_name") val bandName: String? = null,
    val title: String,
    val description: String? = null,
    val type: String = "news",
    @SerialName("event_date") val eventDate: String? = null,
    val venue: String? = null,
    val city: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("smart_link") val smartLink: String? = null,
    @SerialName("spotify_url") val spotifyUrl: String? = null,
    @SerialName("apple_music_url") val appleMusicUrl: String? = null,
    @SerialName("youtube_music_url") val youtubeMusicUrl: String? = null,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("comments_count") val commentsCount: Int = 0,
    @SerialName("rsvp_count") val rsvpCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class BandEventInsertDto(
    @SerialName("band_id") val bandId: String,
    val title: String,
    val description: String? = null,
    val type: String,
    @SerialName("event_date") val eventDate: String? = null,
    val venue: String? = null,
    val city: String? = null,
    @SerialName("smart_link") val smartLink: String? = null,
    @SerialName("spotify_url") val spotifyUrl: String? = null,
    @SerialName("apple_music_url") val appleMusicUrl: String? = null,
    @SerialName("youtube_music_url") val youtubeMusicUrl: String? = null
)

@Serializable
data class EventReactionDto(
    @SerialName("event_id") val eventId: String,
    @SerialName("user_id") val userId: String
)

@Serializable
data class EventCommentDto(
    val id: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("author_name") val authorName: String? = null,
    val text: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class EventCommentInsertDto(
    @SerialName("event_id") val eventId: String,
    @SerialName("user_id") val userId: String,
    val text: String
)

@Serializable
data class VacancyDto(
    val id: String,
    @SerialName("band_id") val bandId: String,
    val instrument: String,
    val description: String? = null,
    val city: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class VacancyInsertDto(
    @SerialName("band_id") val bandId: String,
    val instrument: String,
    val description: String? = null,
    val city: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class ApplicationDto(
    val id: String,
    @SerialName("vacancy_id") val vacancyId: String,
    @SerialName("user_id") val userId: String,
    val message: String? = null,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ApplicationInsertDto(
    @SerialName("vacancy_id") val vacancyId: String,
    @SerialName("user_id") val userId: String,
    val message: String? = null,
    val status: String = "pending"
)

@Serializable
data class ChatMessageInsertDto(
    @SerialName("sender_id") val senderId: String,
    @SerialName("recipient_id") val recipientId: String,
    @SerialName("band_id") val bandId: String? = null,
    val text: String
)

@Serializable
data class NotificationInsertDto(
    @SerialName("user_id") val userId: String,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("is_read") val isRead: Boolean = false
)

@Serializable
data class NotificationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

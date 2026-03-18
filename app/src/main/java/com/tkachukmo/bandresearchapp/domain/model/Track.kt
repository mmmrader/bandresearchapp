package com.tkachukmo.bandresearchapp.domain.model

data class Track(
    val id: String = "",
    val bandId: String = "",
    val bandName: String = "",
    val title: String = "",
    val durationSec: Int = 0,
    val audioUrl: String? = null,
    val coverUrl: String? = null,
    val releaseId: String? = null,
    val lyrics: String? = null,
    val trackNumber: Int = 1
)
package com.tkachukmo.bandresearchapp.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val musicGenres: List<String> = emptyList(),
    val createdAt: String = ""
)
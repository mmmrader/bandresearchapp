package com.tkachukmo.bandresearchapp.domain.model

data class Band(
    val id: String = "",
    val name: String = "",
    val slug: String = "",
    val genres: List<String> = emptyList(),
    val description: String? = null,
    val avatarUrl: String? = null,
    val coverUrl: String? = null,
    val country: String? = null,
    val city: String? = null,
    val formedYear: Int? = null,
    val managerId: String? = null,
    val followersCount: Int = 0
)
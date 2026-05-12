package com.example.vybrasiapp

import kotlinx.serialization.Serializable

@Serializable
data class AdminProfile(
    val id_admin: String,
    val profile_id: String,
    val level_admin: String = "admin"
)
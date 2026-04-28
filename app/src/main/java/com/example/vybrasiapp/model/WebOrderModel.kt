package com.example.vybrasiapp.model

import kotlinx.serialization.Serializable

@Serializable
data class WebOrderModel(
    val id: Int? = 0,
    val status: String? = "",
    val total_harga: Long? = 0L,
    val created_at: String? = ""
)
package com.example.vybrasiapp.model

import kotlinx.serialization.Serializable

@Serializable
data class WebOrderModel(
    val id_transaksi: String? = null,
    val status: String? = "pending",
    val total_harga: Double? = 0.0,
    val created_at: String? = ""
)
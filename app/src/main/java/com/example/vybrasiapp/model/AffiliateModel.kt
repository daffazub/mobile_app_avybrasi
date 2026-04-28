package com.example.vybrasiapp.model
import kotlinx.serialization.Serializable

@Serializable
data class AffiliateModel(
    val id: Int = 0,
    val nama: String = "",
    val total_penjualan: Int = 0,
    val komisi: Long = 0L
)
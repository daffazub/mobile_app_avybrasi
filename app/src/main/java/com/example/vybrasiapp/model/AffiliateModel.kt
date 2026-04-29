package com.example.vybrasiapp.model

import kotlinx.serialization.Serializable

@Serializable
data class AffiliateModel(
    val id_affiliate: String? = null,
    val nama_lengkap: String? = "",
    val total_komisi: Double? = 0.0
)
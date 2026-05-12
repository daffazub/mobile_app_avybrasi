package com.example.vybrasiapp.model

import kotlinx.serialization.Serializable

@Serializable
data class WebOrderModel(
    val id_transaksi: String? = null,
    val status: String? = null,
    val total_harga: Double? = 0.0,
    val created_at: String? = null,
    val id_affiliate: String? = null,    // ← pastikan ada
    val kode_referal_digunakan: String? = null,
    val komisi_affiliate: Double? = null
)
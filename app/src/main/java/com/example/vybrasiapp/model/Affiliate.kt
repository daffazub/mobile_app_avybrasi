package com.example.vybrasiapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Affiliate(
    @SerialName("id_affiliate") val idAffiliate: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("nama_lengkap") val namaLengkap: String? = null,
    @SerialName("kode_referal") val kodeReferal: String? = null,
    @SerialName("komisi_persen") val komisiPersen: Double? = 0.0,
    @SerialName("total_komisi") val totalKomisi: Double? = 0.0,
    @SerialName("minimum_payout") val minimumPayout: Double? = 0.0,
    @SerialName("status_affiliate") val statusAffiliate: String? = "pending",
    // UBAH: Gunakan JsonElement agar tidak error saat membaca data JSONB
    @SerialName("payment_method") val paymentMethod: JsonElement? = null,
    @SerialName("created_at") val createdAt: String? = null
)
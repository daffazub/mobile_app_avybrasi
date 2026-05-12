package com.example.vybrasiapp.model

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class KeuanganAffiliate(
    @SerialName("id_keuangan") val idKeuangan: String = "",
    @SerialName("id_transaksi") val idTransaksi: String? = null,
    @SerialName("id_affiliate") val idAffiliate: String? = null,
    @SerialName("tipe") val tipe: String = "",
    @SerialName("jumlah") val jumlah: Double = 0.0,
    @SerialName("keterangan") val keterangan: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
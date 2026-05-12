package com.example.vybrasiapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Produk(
    @SerialName("id_produk") val id_produk: String? = null,
    @SerialName("nama") val nama: String? = "",
    @SerialName("harga") val harga: Double? = 0.0,
    @SerialName("deskripsi_singkat") val deskripsi_singkat: String? = "",
    @SerialName("deskripsi_lengkap") val deskripsi_lengkap: String? = "",
    @SerialName("gambar_utama") val gambar_utama: String? = "",
    @SerialName("stok") val stok: Int? = 0
)

@Serializable
data class ProdukInsert(
    @SerialName("nama") val nama: String? = "",
    @SerialName("harga") val harga: Double? = 0.0,
    @SerialName("deskripsi_singkat") val deskripsi_singkat: String? = "",
    @SerialName("deskripsi_lengkap") val deskripsi_lengkap: String? = "",
    @SerialName("gambar_utama") val gambar_utama: String? = "",
    @SerialName("stok") val stok: Int? = 0
)
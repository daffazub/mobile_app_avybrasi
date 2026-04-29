package com.example.vybrasiapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Produk(
    val id_produk: String? = null,
    val nama: String? = "",
    val harga: Double? = 0.0,
    val deskripsi_lengkap: String? = "",
    val gambar_utama: String? = ""
)

@Serializable
data class ProdukInsert(
    val nama: String? = "",
    val harga: Double? = 0.0,
    val deskripsi_lengkap: String? = "",
    val gambar_utama: String? = ""
)
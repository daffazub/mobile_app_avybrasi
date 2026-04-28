package com.example.vybrasiapp.model
import kotlinx.serialization.Serializable

// Ini wadah untuk MENARIK/MEMBACA data (sudah ada ID-nya)
@Serializable
data class Produk(
    val id: Int = 0,
    val nama_produk: String = "",
    val harga: Int = 0,
    val deskripsi: String? = null,
    val image_url: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

// TAMBAHKAN KODE INI: Wadah khusus untuk MENGIRIM data baru (TANPA ID)
@Serializable
data class ProdukInsert(
    val nama_produk: String,
    val harga: Int,
    val deskripsi: String?,
    val image_url: String?
)
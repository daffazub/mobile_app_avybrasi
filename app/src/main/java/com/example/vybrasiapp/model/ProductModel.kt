package com.example.vybrasiapp.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductModel(
    val id: Int = 0,
    val nama_produk: String = "", // <-- INI YANG KITA UBAH
    val sisa_kg: Int = 0,
    val terjual: Int = 0
)
package com.example.vybrasiapp.model

import kotlinx.serialization.Serializable

@Serializable
data class InventoryModel(
    val id: Int = 0,
    val nama_produk: String? = "Barang",
    val sisa_kg: Int = 0
)
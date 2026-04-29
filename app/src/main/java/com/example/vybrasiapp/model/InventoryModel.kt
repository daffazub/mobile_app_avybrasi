package com.example.vybrasiapp.model

import kotlinx.serialization.Serializable

@Serializable
data class InventoryModel(
    val id_produk: String? = null,
    val nama: String? = "Barang",
    val stok: Int? = 0
)
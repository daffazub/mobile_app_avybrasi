package com.example.vybrasiapp

// Wajib ada tulisan "data class" dan isi di dalam kurungnya harus persis seperti ini:
data class SaleModel(
    val week: String = "",
    val amount: Long = 0L,
    val totalOrders: Int = 0
)
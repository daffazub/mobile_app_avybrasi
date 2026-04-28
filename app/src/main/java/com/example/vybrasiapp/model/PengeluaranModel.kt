package com.example.vybrasiapp.model

data class PengeluaranModel(
    val id: String = "",
    val keterangan: String = "",
    val nominal: Long = 0L,
    val kategori: String = "Operasional",
    val tanggal: Long = System.currentTimeMillis()
)
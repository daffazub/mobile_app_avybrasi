package com.example.vybrasiapp

data class PayoutItem(
    val idRequest: String,
    val idAffiliate: String,
    val namaAffiliate: String,
    val jumlah: Double,
    val bank: String,
    val tanggal: String,
    val status: String
)
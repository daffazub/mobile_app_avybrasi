package com.example.vybrasiapp.model

import kotlinx.serialization.Serializable

@Serializable
data class FinanceModel(
    val id: Int = 0,
    val omset: Long = 0,
    val keuntungan: Long = 0
)
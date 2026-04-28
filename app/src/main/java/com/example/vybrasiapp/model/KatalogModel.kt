package com.example.vybrasiapp.model

data class KatalogModel(
    var id: String = "",
    var namaKopi: String = "",       // Contoh: Arabica Wine
    var kategori: String = "",       // Arabica / Robusta
    var kemasan: String = "",        // 100gr, 250gr, 1Kg
    var harga: Long = 0L,            // 75000
    var deskripsi: String = "",      // "Kopi dengan fermentasi ceri..."
    var imageUrl: String = ""        // Link foto kemasan pink/oranye/kuning
)
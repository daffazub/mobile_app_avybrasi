package com.example.vybrasiapp

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class LogAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_admin) // Pastikan XML ini sudah dibuat

        val container = findViewById<LinearLayout>(R.id.logContainer) // Gunakan ID dari XML Anda

        val logAktivitas = listOf(
            mapOf("waktu" to "22 Apr 2026, 08:15 WIB", "nama" to "Ario", "aksi" to "Mengubah harga produk 'Kopi Susu Aren'", "tipe" to "Update"),
            mapOf("waktu" to "21 Apr 2026, 14:30 WIB", "nama" to "Raha", "aksi" to "Menambahkan data supplier: 'Susu Segar Nusantara'", "tipe" to "Create"),
            mapOf("waktu" to "20 Apr 2026, 09:00 WIB", "nama" to "Fadil", "aksi" to "Menghapus produk lama dari Katalog", "tipe" to "Delete")
        )

        for (log in logAktivitas) {
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 16) }
                radius = 12f
                cardElevation = 2f
            }

            val cardContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)

                // Beri warna latar berbeda berdasarkan tipe aksi
                val bgColor = when (log["tipe"]) {
                    "Create" -> "#E8F5E9" // Hijau muda
                    "Update" -> "#E3F2FD" // Biru muda
                    "Delete" -> "#FFEBEE" // Merah muda
                    else -> "#FFFFFF"
                }
                setBackgroundColor(Color.parseColor(bgColor))
            }

            val tvWaktu = TextView(this).apply {
                text = "🕒 ${log["waktu"]}"
                textSize = 12f
                setTextColor(Color.parseColor("#757575"))
            }

            val tvAdmin = TextView(this).apply {
                text = "Admin: ${log["nama"]}"
                textSize = 14f
                setTextColor(Color.parseColor("#1976D2"))
                paint.isFakeBoldText = true
                setPadding(0, 8, 0, 4)
            }

            val tvAksi = TextView(this).apply {
                text = log["aksi"]
                textSize = 16f
                setTextColor(Color.parseColor("#333333"))
            }

            cardContent.addView(tvWaktu)
            cardContent.addView(tvAdmin)
            cardContent.addView(tvAksi)
            card.addView(cardContent)

            container.addView(card)
        }
    }
}
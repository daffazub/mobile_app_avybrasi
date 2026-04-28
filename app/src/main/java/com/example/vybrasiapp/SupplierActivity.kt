package com.example.vybrasiapp

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class SupplierActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supplier)

        val container = findViewById<LinearLayout>(R.id.supplierContainer)

        // Data Dummy
        val dataSupplier = listOf(
            mapOf("nama" to "PT Biji Kopi Sentosa", "kategori" to "Biji Kopi Arabica & Robusta", "kontak" to "0812-3333-4444", "status" to "Aktif"),
            mapOf("nama" to "Susu Segar Nusantara", "kategori" to "Susu UHT & Fresh Milk", "kontak" to "0898-7777-6666", "status" to "Aktif"),
            mapOf("nama" to "Gula Aren Organik", "kategori" to "Gula Aren Cair & Bubuk", "kontak" to "0856-1111-2222", "status" to "Non-Aktif")
        )

        // Render UI Card secara otomatis berdasarkan data dummy
        for (supplier in dataSupplier) {
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 24) }
                radius = 16f
                cardElevation = 4f
                setCardBackgroundColor(Color.WHITE)
            }

            val cardContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(40, 40, 40, 40)
            }

            val tvNama = TextView(this).apply {
                text = "🏢 ${supplier["nama"]}"
                textSize = 18f
                setTextColor(Color.parseColor("#333333"))
                paint.isFakeBoldText = true
            }

            val tvKategori = TextView(this).apply {
                text = supplier["kategori"]
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
                setPadding(0, 8, 0, 16)
            }

            val tvKontak = TextView(this).apply {
                text = "📞 ${supplier["kontak"]}  |  Status: ${supplier["status"]}"
                textSize = 14f
                setTextColor(if (supplier["status"] == "Aktif") Color.parseColor("#388E3C") else Color.parseColor("#D32F2F"))
            }

            cardContent.addView(tvNama)
            cardContent.addView(tvKategori)
            cardContent.addView(tvKontak)
            card.addView(cardContent)

            container.addView(card)
        }
    }
}
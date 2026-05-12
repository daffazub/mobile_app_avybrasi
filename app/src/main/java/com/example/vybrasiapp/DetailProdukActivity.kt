package com.example.vybrasiapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import java.text.NumberFormat
import java.util.Locale

class DetailProdukActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_produk)

        // ── Ambil data dari intent ─────────────────────────
        val nama      = intent.getStringExtra("EXTRA_NAMA") ?: "-"
        val harga     = intent.getDoubleExtra("EXTRA_HARGA", 0.0)
        val deskripsi = intent.getStringExtra("EXTRA_DESKRIPSI") ?: "-"
        val gambar    = intent.getStringExtra("EXTRA_GAMBAR") ?: ""
        val stok      = intent.getIntExtra("EXTRA_STOK", 0)

        // ── Views ──────────────────────────────────────────
        // Perbaiki: tombol back menggunakan ImageView (sesuai error ClassCastException)
        val btnBack     = findViewById<ImageView>(R.id.btnBackDetail)
        val ivFoto      = findViewById<ImageView>(R.id.ivDetailFoto)
        val tvNama      = findViewById<TextView>(R.id.tvDetailNamaProduk)
        val tvHarga     = findViewById<TextView>(R.id.tvDetailHarga)
        val tvDeskripsi = findViewById<TextView>(R.id.tvDetailDeskripsi)
        val tvStok      = findViewById<TextView>(R.id.tvDetailStok)

        // ── Isi data ───────────────────────────────────────
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        tvNama.text      = nama
        tvHarga.text     = formatRupiah.format(harga)
        tvDeskripsi.text = deskripsi

        // Stok + warna dinamis
        tampilkanStok(tvStok, stok)

        // Foto produk
        if (gambar.isNotEmpty()) {
            ivFoto.load(gambar) {
                crossfade(true)
                placeholder(R.mipmap.ic_launcher_round)
                error(R.mipmap.ic_launcher_round)
            }
        } else {
            ivFoto.setImageResource(R.mipmap.ic_launcher_round)
        }

        // ── Navigasi ───────────────────────────────────────
        btnBack.setOnClickListener { finish() }
    }

    private fun tampilkanStok(tv: TextView, stok: Int) {
        val warna = when {
            stok < 5  -> Color.parseColor("#F44336")
            stok <= 10 -> Color.parseColor("#FF9800")
            else      -> Color.parseColor("#4CAF50")
        }

        tv.text = "Stok: $stok"
        tv.backgroundTintList = ColorStateList.valueOf(warna)
    }
}
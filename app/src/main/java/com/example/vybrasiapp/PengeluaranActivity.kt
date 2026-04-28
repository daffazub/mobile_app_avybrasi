package com.example.vybrasiapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vybrasiapp.model.PengeluaranModel
import com.google.firebase.firestore.FirebaseFirestore

class PengeluaranActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengeluaran)

        // ==========================================
        // 1. MENGHIDUPKAN TOMBOL BACK (KEMBALI)
        // ==========================================
        val btnBack = findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Ini adalah perintah sakti untuk kembali ke Profil
        }

        // ==========================================
        // 2. INISIALISASI FORM PENGELUARAN
        // ==========================================
        val etKeterangan = findViewById<EditText>(R.id.etKeterangan)
        val etNominal = findViewById<EditText>(R.id.etNominal)
        val rgKategori = findViewById<RadioGroup>(R.id.rgKategori)
        val btnSimpan = findViewById<Button>(R.id.btnSimpanPengeluaran)

        // ==========================================
        // 3. AKSI SAAT TOMBOL SIMPAN DITEKAN
        // ==========================================
        btnSimpan.setOnClickListener {
            val keterangan = etKeterangan.text.toString().trim()
            val nominalStr = etNominal.text.toString().trim()

            if (keterangan.isEmpty() || nominalStr.isEmpty()) {
                Toast.makeText(this, "Harap isi keterangan dan nominal!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tentukan Kategori berdasarkan yang dipilih Bos
            val kategori = when (rgKategori.checkedRadioButtonId) {
                R.id.rbBahan -> "Bahan Baku"
                R.id.rbGaji -> "Gaji Pegawai"
                R.id.rbLainnya -> "Lain-lain"
                else -> "Operasional"
            }

            val nominal = nominalStr.toLong()
            simpanKeFirestore(keterangan, nominal, kategori)
        }
    }

    private fun simpanKeFirestore(keterangan: String, nominal: Long, kategori: String) {
        // Buat ID unik acak
        val id = db.collection("pengeluaran").document().id
        val dataPengeluaran = PengeluaranModel(id, keterangan, nominal, kategori)

        db.collection("pengeluaran").document(id)
            .set(dataPengeluaran)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil dicatat! 💸", Toast.LENGTH_SHORT).show()
                finish() // Otomatis kembali ke profil jika data sukses tersimpan
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
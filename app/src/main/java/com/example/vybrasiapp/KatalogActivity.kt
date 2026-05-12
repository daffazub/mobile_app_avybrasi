package com.example.vybrasiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vybrasiapp.model.Produk
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KatalogActivity : AppCompatActivity() {

    private lateinit var rvKatalog: RecyclerView
    private val listKopi = mutableListOf<Produk>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_katalog)

        val btnBack = findViewById<TextView>(R.id.btnBackKatalog)
        btnBack.setOnClickListener { finish() }

        val fabTambah = findViewById<FloatingActionButton>(R.id.fabTambahProduk)
        fabTambah?.visibility = View.GONE

        rvKatalog = findViewById(R.id.rvKatalog)
        rvKatalog.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        loadDataSupabase()
    }

    private fun loadDataSupabase() {
        lifecycleScope.launch {
            try {
                val daftarProduk = withContext(Dispatchers.IO) {
                    SupabaseManager.client
                        .from("produk")
                        .select()
                        .decodeList<Produk>()
                }

                listKopi.clear()
                listKopi.addAll(daftarProduk)

                // ✅ Tambah onClick → buka DetailProdukActivity
                val adapter = KatalogAdapter(listKopi) { produk ->
                    val intent = Intent(this@KatalogActivity, DetailProdukActivity::class.java).apply {
                        putExtra("EXTRA_ID", produk.id_produk ?: "")
                        putExtra("EXTRA_NAMA", produk.nama ?: "")
                        putExtra("EXTRA_HARGA", produk.harga ?: 0.0)
                        putExtra("EXTRA_DESKRIPSI", produk.deskripsi_lengkap ?: "")
                        putExtra("EXTRA_GAMBAR", produk.gambar_utama ?: "")
                        putExtra("EXTRA_STOK", produk.stok ?: 0)
                    }
                    startActivity(intent)
                }
                rvKatalog.adapter = adapter

            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Gagal memuat data: ${e.message}")
                Toast.makeText(this@KatalogActivity, "Gagal memuat katalog", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
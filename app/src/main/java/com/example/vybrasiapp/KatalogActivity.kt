package com.example.vybrasiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        fabTambah.setOnClickListener {
            val intent = Intent(this, FormKatalogActivity::class.java)
            startActivity(intent)
        }

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

                val adapter = KatalogAdapter(
                    listKopi = listKopi,
                    onEdit = { produkYangDiedit ->
                        val intent = Intent(this@KatalogActivity, FormKatalogActivity::class.java)
                        intent.putExtra("EXTRA_ID", produkYangDiedit.id_produk)
                        intent.putExtra("EXTRA_NAMA", produkYangDiedit.nama) // Sesuai Model
                        intent.putExtra("EXTRA_HARGA", produkYangDiedit.harga)
                        intent.putExtra("EXTRA_DESKRIPSI", produkYangDiedit.deskripsi_lengkap) // Sesuai Model
                        intent.putExtra("EXTRA_GAMBAR", produkYangDiedit.gambar_utama) // Sesuai Model
                        startActivity(intent)
                    },
                    onDelete = { produkYangDihapus ->
                        hapusProdukSupabase(produkYangDihapus)
                    }
                )
                rvKatalog.adapter = adapter

            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Gagal memuat data: ${e.message}")
                Toast.makeText(this@KatalogActivity, "Gagal memuat katalog", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hapusProdukSupabase(produk: Produk) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseManager.client
                        .from("produk")
                        .delete {
                            // UBAH: Sesuaikan nama kolom UUID dengan ERD baru
                            filter { eq("id_produk", produk.id_produk ?: "") }
                        }
                }

                Toast.makeText(this@KatalogActivity, "${produk.nama} berhasil dihapus!", Toast.LENGTH_SHORT).show()
                loadDataSupabase()

            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Gagal menghapus: ${e.message}")
                Toast.makeText(this@KatalogActivity, "Gagal menghapus produk", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
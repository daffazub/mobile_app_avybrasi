package com.example.vybrasiapp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.vybrasiapp.model.InventoryModel
import com.example.vybrasiapp.model.WebOrderModel
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MonitorFragment : Fragment() {

    private lateinit var llContainerPesanan: LinearLayout
    private lateinit var llContainerStokDetail: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monitor, container, false)
        llContainerPesanan = view.findViewById(R.id.llContainerPesanan)
        llContainerStokDetail = view.findViewById(R.id.llContainerStokDetail)
        return view
    }

    override fun onResume() {
        super.onResume()
        muatDataOperasional() // Selalu update saat halaman dibuka
    }

    private fun muatDataOperasional() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Tarik HANYA Pesanan yang butuh tindakan (Menunggu Proses)
                val pesananList = withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("web_orders")
                        .select { filter { eq("status", "Menunggu Proses") } }
                        .decodeList<WebOrderModel>()
                }

                // 2. Tarik SEMUA Stok Barang
                val stokList = withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("inventory").select().decodeList<InventoryModel>()
                }

                withContext(Dispatchers.Main) {
                    renderPesanan(pesananList)
                    renderStok(stokList)
                }
            } catch (e: Exception) {
                Log.e("MONITOR_ERROR", "Gagal memuat operasional: ${e.message}")
            }
        }
    }

    // ==========================================
    // FUNGSI RENDER ANTREAN PESANAN (DENGAN TOMBOL)
    // ==========================================
    private fun renderPesanan(pesananList: List<WebOrderModel>) {
        llContainerPesanan.removeAllViews()

        if (pesananList.isEmpty()) {
            val tvKosong = TextView(requireContext()).apply {
                text = "Hore! Meja bersih, tidak ada antrean pesanan saat ini."
                setPadding(16, 16, 16, 16)
                setTextColor(Color.GRAY)
            }
            llContainerPesanan.addView(tvKosong)
            return
        }

        pesananList.forEach { pesanan ->
            // Membuat Kartu Pesanan Dinamis
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                background = requireContext().getDrawable(R.drawable.bg_card_outline) // Pastikan Anda punya drawable kotak
            }

            val tvTitle = TextView(requireContext()).apply {
                text = "Pesanan #${pesanan.id}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.BLACK)
            }

            // Format Rp jika ada harganya
            val hargaRp = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID")).format(pesanan.total_harga ?: 0)
            val tvDetail = TextView(requireContext()).apply {
                text = "Total Nilai: $hargaRp\nStatus: ${pesanan.status}"
                setTextColor(Color.parseColor("#F44336")) // Merah tanda belum diproses
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 4, 0, 12)
                }
            }

            // TOMBOL EKSEKUSI
            val btnSelesai = Button(requireContext()).apply {
                text = "Tandai Selesai & Kirim"
                setBackgroundColor(Color.parseColor("#4CAF50")) // Hijau Selesai
                setTextColor(Color.WHITE)

                // Apa yang terjadi saat tombol ditekan?
                setOnClickListener {
                    konfirmasiSelesaikanPesanan(pesanan.id ?: 0)
                }
            }

            val garisBawah = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2).apply {
                    setMargins(0, 16, 0, 0)
                }
                setBackgroundColor(Color.parseColor("#EEEEEE"))
            }

            itemLayout.addView(tvTitle)
            itemLayout.addView(tvDetail)
            itemLayout.addView(btnSelesai)
            itemLayout.addView(garisBawah)

            llContainerPesanan.addView(itemLayout)
        }
    }

    // ==========================================
    // FUNGSI UPDATE STATUS KE SUPABASE
    // ==========================================
    private fun konfirmasiSelesaikanPesanan(idPesanan: Int) {
        // Tampilkan peringatan sebelum mengeksekusi
        AlertDialog.Builder(requireContext())
            .setTitle("Selesaikan Pesanan?")
            .setMessage("Apakah barang untuk Pesanan #$idPesanan sudah siap dan dikirim?")
            .setPositiveButton("Ya, Sudah") { _, _ ->
                updateStatusSupabase(idPesanan)
            }
            .setNegativeButton("Belum", null)
            .show()
    }

    private fun updateStatusSupabase(idPesanan: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Proses update ke Supabase
                withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("web_orders")
                        .update(
                            {
                                set("status", "Dikirim") // <-- Mengubah statusnya di database!
                            }
                        ) {
                            filter { eq("id", idPesanan) }
                        }
                }

                // Jika sukses, beri tahu admin dan refresh layarnya
                Toast.makeText(requireContext(), "Pesanan #$idPesanan berhasil diselesaikan!", Toast.LENGTH_SHORT).show()
                muatDataOperasional() // Tarik data ulang agar pesanan tadi hilang dari antrean

            } catch (e: Exception) {
                Log.e("MONITOR_ERROR", "Gagal update: ${e.message}")
                Toast.makeText(requireContext(), "Gagal mengubah status pesanan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================
    // FUNGSI RENDER STOK (Seperti Sebelumnya)
    // ==========================================
    private fun renderStok(stokList: List<InventoryModel>) {
        llContainerStokDetail.removeAllViews()
        stokList.forEach { stok ->
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_stok_warning, llContainerStokDetail, false)
            val kotakWarna = itemView.findViewById<View>(R.id.viewColorIndicator)
            val tvNamaStok = itemView.findViewById<TextView>(R.id.tvNamaStokWarning)

            val sisa = stok.sisa_kg ?: 0
            val nama = stok.nama_produk ?: "Barang"

            val warnaStatus = when {
                sisa < 5 -> "#F44336" // Kritis
                sisa in 5..10 -> "#FFC107" // Waspada
                else -> "#4CAF50" // Aman
            }

            kotakWarna.setBackgroundColor(Color.parseColor(warnaStatus))
            tvNamaStok.text = "$nama - Tersedia: $sisa Kg"

            llContainerStokDetail.addView(itemView)
        }
    }
}
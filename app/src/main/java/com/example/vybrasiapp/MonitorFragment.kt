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
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.NumberFormat
import java.util.Locale

// ==============================================================================
// CETAKAN DATA LOKAL (Penangkal Error Model Lama)
// ==============================================================================
@Serializable
data class TransaksiMonitorDTO(
    val id_transaksi: String = "",
    val no_invoice: String = "",
    val total_harga: Double = 0.0,
    val status: String = ""
)

@Serializable
data class ProdukMonitorDTO(
    val nama: String = "",
    val stok: Int = 0
)

@Serializable
data class UpdateStatusDTO(
    val status: String
)

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
        muatDataOperasional()
    }

    private fun muatDataOperasional() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Tarik Pesanan yang statusnya 'pending' (Menggunakan tabel baru: transaksi)
                val pesananList = withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("transaksi")
                        .select { filter { eq("status", "pending") } }
                        .decodeList<TransaksiMonitorDTO>()
                }

                // 2. Tarik SEMUA Stok Barang (Menggunakan tabel baru: produk)
                val stokList = withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("produk").select().decodeList<ProdukMonitorDTO>()
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
    // FUNGSI RENDER ANTREAN PESANAN
    // ==========================================
    private fun renderPesanan(pesananList: List<TransaksiMonitorDTO>) {
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
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                try {
                    background = requireContext().getDrawable(R.drawable.bg_card_outline)
                } catch (e: Exception) {
                    setBackgroundColor(Color.parseColor("#FAFAFA")) // Fallback aman
                }
            }

            val tvTitle = TextView(requireContext()).apply {
                text = "Pesanan ${pesanan.no_invoice}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(Color.BLACK)
            }

            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val hargaRp = formatRupiah.format(pesanan.total_harga)
            val tvDetail = TextView(requireContext()).apply {
                text = "Total Nilai: $hargaRp\nStatus: ${pesanan.status}"
                setTextColor(Color.parseColor("#F44336"))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 4, 0, 12)
                }
            }

            val btnSelesai = Button(requireContext()).apply {
                text = "Tandai Selesai & Kirim"
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setTextColor(Color.WHITE)

                setOnClickListener {
                    konfirmasiSelesaikanPesanan(pesanan.id_transaksi, pesanan.no_invoice)
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
    private fun konfirmasiSelesaikanPesanan(idTransaksi: String, noInvoice: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Selesaikan Pesanan?")
            .setMessage("Apakah barang untuk $noInvoice sudah siap dan dikirim?")
            .setPositiveButton("Ya, Sudah") { _, _ ->
                updateStatusSupabase(idTransaksi, noInvoice)
            }
            .setNegativeButton("Belum", null)
            .show()
    }

    private fun updateStatusSupabase(idTransaksi: String, noInvoice: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Update status dari 'pending' menjadi 'shipped' (sesuai ENUM database)
                    SupabaseManager.client.from("transaksi")
                        .update(UpdateStatusDTO("shipped")) {
                            filter { eq("id_transaksi", idTransaksi) }
                        }
                }

                Toast.makeText(requireContext(), "$noInvoice berhasil diselesaikan!", Toast.LENGTH_SHORT).show()
                muatDataOperasional()

            } catch (e: Exception) {
                Log.e("MONITOR_ERROR", "Gagal update: ${e.message}")
                Toast.makeText(requireContext(), "Gagal mengubah status pesanan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==========================================
    // FUNGSI RENDER STOK
    // ==========================================
    private fun renderStok(stokList: List<ProdukMonitorDTO>) {
        llContainerStokDetail.removeAllViews()
        stokList.forEach { produk ->
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_stok_warning, llContainerStokDetail, false)
            val kotakWarna = itemView.findViewById<View>(R.id.viewColorIndicator)
            val tvNamaStok = itemView.findViewById<TextView>(R.id.tvNamaStokWarning)

            val sisa = produk.stok
            val nama = produk.nama

            val warnaStatus = when {
                sisa < 5 -> "#F44336" // Kritis
                sisa in 5..10 -> "#FFC107" // Waspada
                else -> "#4CAF50" // Aman
            }

            kotakWarna.setBackgroundColor(Color.parseColor(warnaStatus))
            tvNamaStok.text = "$nama - Tersedia: $sisa Kemasan"

            llContainerStokDetail.addView(itemView)
        }
    }
}
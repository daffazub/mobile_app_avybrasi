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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
                // MENGGUNAKAN TABEL TRANSAKSI
                val pesananList = withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("transaksi").select { filter { eq("status", "pending") } }
                        .decodeList<WebOrderModel>()
                }

                // MENGGUNAKAN TABEL PRODUK UNTUK CEK STOK
                val stokList = withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("produk").select().decodeList<InventoryModel>()
                }

                withContext(Dispatchers.Main) {
                    renderPesanan(pesananList)
                    renderStok(stokList)
                }
            } catch (e: Exception) {
                Log.e("MONITOR_ERROR", "Gagal memuat: ${e.message}")
            }
        }
    }

    private fun renderPesanan(pesananList: List<WebOrderModel>) {
        llContainerPesanan.removeAllViews()
        if (pesananList.isEmpty()) {
            llContainerPesanan.addView(TextView(requireContext()).apply { text = "Antrean bersih!"; setPadding(16,16,16,16) })
            return
        }

        pesananList.forEach { pesanan ->
            val itemLayout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 16, 16, 16) }
            // ID sekarang UUID, kita ambil 8 karakter pertama saja agar tidak kepanjangan di UI
            val shortId = pesanan.id_transaksi?.take(8) ?: "Unknown"

            val tvTitle = TextView(requireContext()).apply { text = "Pesanan #$shortId"; textSize = 16f; setTypeface(null, android.graphics.Typeface.BOLD) }
            val tvDetail = TextView(requireContext()).apply { text = "Total: Rp${pesanan.total_harga}\nStatus: ${pesanan.status}"; setTextColor(Color.RED) }
            val btnSelesai = Button(requireContext()).apply {
                text = "Tandai Selesai"
                setBackgroundColor(Color.parseColor("#4CAF50"))
                setTextColor(Color.WHITE)
                setOnClickListener { konfirmasiSelesaikanPesanan(pesanan.id_transaksi ?: "") } // Kirim UUID
            }
            itemLayout.addView(tvTitle); itemLayout.addView(tvDetail); itemLayout.addView(btnSelesai)
            llContainerPesanan.addView(itemLayout)
        }
    }

    private fun konfirmasiSelesaikanPesanan(idPesanan: String) { // Tipe parameter jadi String (UUID)
        AlertDialog.Builder(requireContext())
            .setTitle("Selesaikan Pesanan?")
            .setPositiveButton("Ya") { _, _ -> updateStatusSupabase(idPesanan) }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateStatusSupabase(idPesanan: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("transaksi")
                        .update({ set("status", "shipped") }) { filter { eq("id_transaksi", idPesanan) } }
                }
                Toast.makeText(requireContext(), "Berhasil!", Toast.LENGTH_SHORT).show()
                muatDataOperasional()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderStok(stokList: List<InventoryModel>) {
        llContainerStokDetail.removeAllViews()
        stokList.forEach { stok ->
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_stok_warning, llContainerStokDetail, false)
            val kotakWarna = itemView.findViewById<View>(R.id.viewColorIndicator)
            val tvNamaStok = itemView.findViewById<TextView>(R.id.tvNamaStokWarning)

            val sisa = stok.stok ?: 0
            val nama = stok.nama ?: "Barang"
            kotakWarna.setBackgroundColor(Color.parseColor(if (sisa < 5) "#F44336" else "#4CAF50"))
            tvNamaStok.text = "$nama - Sisa: $sisa"
            llContainerStokDetail.addView(itemView)
        }
    }
}
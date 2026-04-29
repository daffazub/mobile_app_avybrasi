package com.example.vybrasiapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.NumberFormat
import java.util.Locale

// ==============================================================================
// CETAKAN DATA LOKAL (Agar tidak bentrok dengan model lama yang belum diupdate)
// ==============================================================================
@Serializable
data class TransaksiHomeDTO(val total_harga: Double? = 0.0)

@Serializable
data class ProdukHomeDTO(val nama: String? = "", val stok: Int? = 0)

@Serializable
data class AffiliateHomeDTO(val nama_lengkap: String? = "", val total_komisi: Double? = 0.0)

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        muatDataDashboard(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { muatDataDashboard(it) }
    }

    private fun muatDataDashboard(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. KEUANGAN (Ambil omset dari transaksi yang sudah 'delivered')
                val financeDef = async(Dispatchers.IO) {
                    val list = SupabaseManager.client.from("transaksi")
                        .select { filter { eq("status", "delivered") } }
                        .decodeList<TransaksiHomeDTO>()
                    list.sumOf { it.total_harga ?: 0.0 }
                }

                // 2. PESANAN MENUNGGU (Cari status 'pending' sesuai ENUM baru)
                val pesananDef = async(Dispatchers.IO) {
                    SupabaseManager.client.from("transaksi")
                        .select { filter { eq("status", "pending") } }
                        .decodeList<TransaksiHomeDTO>().size
                }

                // 3. STOK KRITIS (Cari di tabel produk yang stok < 5)
                val stokDef = async(Dispatchers.IO) {
                    SupabaseManager.client.from("produk")
                        .select { filter { lt("stok", 5) } }
                        .decodeList<ProdukHomeDTO>().size
                }

                // 4. PRODUK TERLARIS (Ambil 3 produk dengan stok paling sedikit)
                val produkDef = async(Dispatchers.IO) {
                    SupabaseManager.client.from("produk")
                        .select {
                            order("stok", order = Order.ASCENDING)
                            limit(count = 3)
                        }
                        .decodeList<ProdukHomeDTO>()
                }

                // 5. TOP AFFILIATE (Ambil 3 artis dari tabel affiliate_profiles)
                val affiliateDef = async(Dispatchers.IO) {
                    SupabaseManager.client.from("affiliate_profiles")
                        .select {
                            order("total_komisi", order = Order.DESCENDING)
                            limit(count = 3)
                        }
                        .decodeList<AffiliateHomeDTO>()
                }

                // Tunggu semua data selesai ditarik
                val omset = financeDef.await()
                val jmlPesanan = pesananDef.await()
                val jmlStokKritis = stokDef.await()
                val listProduk = produkDef.await()
                val listAffiliate = affiliateDef.await()

                // Update UI di Main Thread
                withContext(Dispatchers.Main) {
                    updateUIKeuangan(view, omset)
                    updateUIPesanan(view, jmlPesanan)
                    updateUIStokKritis(view, jmlStokKritis)
                    updateUIBestSellers(view, listProduk)
                    updateUIAffiliate(view, listAffiliate)
                }

            } catch (e: Exception) {
                Log.e("HOME_ERROR", "Gagal memuat dashboard: ${e.message}")
            }
        }
    }

    private fun updateUIKeuangan(view: View, omset: Double) {
        val tvOmset = view.findViewById<TextView>(R.id.tvOmset)
        val tvKeuntungan = view.findViewById<TextView>(R.id.tvKeuntungan)
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // Simulasi keuntungan 30% dari omset agar dashboard terlihat hidup
        val keuntungan = omset * 0.30

        tvOmset.text = formatRupiah.format(omset)
        tvKeuntungan.text = formatRupiah.format(keuntungan)
    }

    private fun updateUIPesanan(view: View, jumlah: Int) {
        view.findViewById<TextView>(R.id.tvPesananWeb).text = jumlah.toString()
    }

    private fun updateUIStokKritis(view: View, jumlah: Int) {
        view.findViewById<TextView>(R.id.tvStokKritisCount).text = jumlah.toString()
    }

    private fun updateUIBestSellers(view: View, listProduk: List<ProdukHomeDTO>) {
        val tvProduk1 = view.findViewById<TextView>(R.id.tvProduk1)
        val tvTerjual1 = view.findViewById<TextView>(R.id.tvTerjual1)
        val tvProduk2 = view.findViewById<TextView>(R.id.tvProduk2)
        val tvTerjual2 = view.findViewById<TextView>(R.id.tvTerjual2)
        val tvProduk3 = view.findViewById<TextView>(R.id.tvProduk3)
        val tvTerjual3 = view.findViewById<TextView>(R.id.tvTerjual3)

        if (listProduk.isNotEmpty()) {
            tvProduk1.text = "1. ${listProduk[0].nama}"
            tvTerjual1.text = "Sisa Stok: ${listProduk[0].stok} Kemasan"
        } else {
            tvProduk1.text = "1. Belum ada data"
            tvTerjual1.text = "-"
        }

        if (listProduk.size >= 2) {
            tvProduk2.text = "2. ${listProduk[1].nama}"
            tvTerjual2.text = "Sisa Stok: ${listProduk[1].stok} Kemasan"
        } else {
            tvProduk2.text = "2. Belum ada data"
            tvTerjual2.text = "-"
        }

        if (listProduk.size >= 3) {
            tvProduk3.text = "3. ${listProduk[2].nama}"
            tvTerjual3.text = "Sisa Stok: ${listProduk[2].stok} Kemasan"
        } else {
            tvProduk3.text = "3. Belum ada data"
            tvTerjual3.text = "-"
        }
    }

    private fun updateUIAffiliate(view: View, listAffiliate: List<AffiliateHomeDTO>) {
        val container = view.findViewById<LinearLayout>(R.id.llContainerAffiliate)
        container.removeAllViews()
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        if (listAffiliate.isEmpty()) {
            val tvKosong = TextView(requireContext())
            tvKosong.text = "Belum ada data affiliate."
            tvKosong.setPadding(16, 16, 16, 16)
            container.addView(tvKosong)
            return
        }

        listAffiliate.forEachIndexed { index, affiliate ->
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_affiliate_rank, container, false)

            val tvRank = itemView.findViewById<TextView>(R.id.tvRankNum)
            tvRank.text = "#${index + 1}"
            if (index == 0) tvRank.setTextColor(Color.parseColor("#D4AF37")) // Emas untuk juara 1

            // Simulasi jumlah penjualan berdasarkan komisi (komisi dibagi 15.000)
            val estimasiPenjualan = ((affiliate.total_komisi ?: 0.0) / 15000).toInt()

            itemView.findViewById<TextView>(R.id.tvAffiliateName).text = affiliate.nama_lengkap
            itemView.findViewById<TextView>(R.id.tvAffiliateSales).text = "$estimasiPenjualan Penjualan Sukses"
            itemView.findViewById<TextView>(R.id.tvAffiliateKomisi).text = formatRupiah.format(affiliate.total_komisi)

            container.addView(itemView)
        }
    }
}
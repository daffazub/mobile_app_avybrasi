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
import com.example.vybrasiapp.model.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

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

            // 1. Omset (Dihitung dari total transaksi yang sukses)
            val financeDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("transaksi")
                        .select() // Tarik semua untuk dihitung totalnya
                        .decodeList<WebOrderModel>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // 2. Pesanan Menunggu (Dari tabel transaksi)
            val pesananDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("transaksi").select { filter { eq("status", "pending") } }
                        .decodeList<WebOrderModel>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // 3. Stok Kritis (< 5) dari tabel produk
            val stokDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("produk")
                        .select { filter { lt("stok", 5) } }
                        .decodeList<InventoryModel>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // 4. Produk Terlaris (Karena tidak ada kolom terjual, kita urutkan dari stok terendah)
            val produkDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("produk")
                        .select {
                            order("stok", order = Order.ASCENDING)
                            limit(count = 3)
                        }
                        .decodeList<InventoryModel>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // 5. Top Affiliate (Dari affiliate_profiles)
            val affiliateDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("affiliate_profiles")
                        .select {
                            order("total_komisi", order = Order.DESCENDING)
                            limit(count = 3)
                        }
                        .decodeList<AffiliateModel>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val dataSemuaTransaksi = financeDef.await()
            val dataPesanan = pesananDef.await()
            val dataStok = stokDef.await()
            val dataProduk = produkDef.await()
            val dataAffiliate = affiliateDef.await()

            withContext(Dispatchers.Main) {
                updateUIKeuangan(view, dataSemuaTransaksi)
                updateUIPesanan(view, dataPesanan.size)
                updateUIStokKritis(view, dataStok.size)
                updateUIBestSellers(view, dataProduk)
                updateUIAffiliate(view, dataAffiliate)
            }
        }
    }

    private fun updateUIKeuangan(view: View, transaksi: List<WebOrderModel>) {
        val tvOmset = view.findViewById<TextView>(R.id.tvOmset)
        val tvKeuntungan = view.findViewById<TextView>(R.id.tvKeuntungan)
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // Hitung total harga dari semua transaksi
        val totalOmset = transaksi.sumOf { it.total_harga ?: 0.0 }
        val estimasiKeuntungan = totalOmset * 0.3 // Asumsi margin keuntungan 30% untuk demo

        tvOmset.text = formatRupiah.format(totalOmset)
        tvKeuntungan.text = formatRupiah.format(estimasiKeuntungan)
    }

    private fun updateUIPesanan(view: View, jumlah: Int) {
        view.findViewById<TextView>(R.id.tvPesananWeb).text = jumlah.toString()
    }

    private fun updateUIStokKritis(view: View, jumlah: Int) {
        view.findViewById<TextView>(R.id.tvStokKritisCount).text = jumlah.toString()
    }

    private fun updateUIBestSellers(view: View, listProduk: List<InventoryModel>) {
        val tvProduk1 = view.findViewById<TextView>(R.id.tvProduk1)
        val tvTerjual1 = view.findViewById<TextView>(R.id.tvTerjual1)
        val tvProduk2 = view.findViewById<TextView>(R.id.tvProduk2)
        val tvTerjual2 = view.findViewById<TextView>(R.id.tvTerjual2)
        val tvProduk3 = view.findViewById<TextView>(R.id.tvProduk3)
        val tvTerjual3 = view.findViewById<TextView>(R.id.tvTerjual3)

        if (listProduk.isNotEmpty()) {
            tvProduk1.text = "1. ${listProduk[0].nama}"
            tvTerjual1.text = "Sisa Stok: ${listProduk[0].stok}"
        }

        if (listProduk.size >= 2) {
            tvProduk2.text = "2. ${listProduk[1].nama}"
            tvTerjual2.text = "Sisa Stok: ${listProduk[1].stok}"
        }

        if (listProduk.size >= 3) {
            tvProduk3.text = "3. ${listProduk[2].nama}"
            tvTerjual3.text = "Sisa Stok: ${listProduk[2].stok}"
        }
    }

    private fun updateUIAffiliate(view: View, listAffiliate: List<AffiliateModel>) {
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
            if (index == 0) tvRank.setTextColor(Color.parseColor("#D4AF37"))

            // Sesuai dengan kolom di AffiliateModel yang baru
            itemView.findViewById<TextView>(R.id.tvAffiliateName).text = affiliate.nama_lengkap ?: "Tanpa Nama"
            itemView.findViewById<TextView>(R.id.tvAffiliateSales).text = "Komisi Tertinggi"
            itemView.findViewById<TextView>(R.id.tvAffiliateKomisi).text = formatRupiah.format(affiliate.total_komisi ?: 0.0)

            container.addView(itemView)
        }
    }
}
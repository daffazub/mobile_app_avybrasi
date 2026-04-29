package com.example.vybrasiapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

            // 1. Keuangan
            val financeDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("finance")
                        .select { filter { eq("id", 1) } }
                        .decodeSingleOrNull<FinanceModel>()
                } catch (e: Exception) {
                    null
                }
            }

            // 2. Pesanan Menunggu
            val pesananDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("web_orders")
                        .select { filter { eq("status", "Menunggu Proses") } }
                        .decodeList<WebOrderModel>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // 3. Stok Kritis (< 5 kg)
            val stokDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("inventory")
                        .select { filter { lt("sisa_kg", 5) } }
                        .decodeList<InventoryModel>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // 4. Produk Terlaris (Top 3)
            val produkDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("produk")
                        .select {
                            order("terjual", order = Order.DESCENDING)
                            limit(count = 3)
                        }
                        .decodeList<ProductModel>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // 5. Top Affiliate (Top 3)
            val affiliateDef = async(Dispatchers.IO) {
                try {
                    SupabaseManager.client.from("affiliates")
                        .select {
                            order("komisi", order = Order.DESCENDING)
                            limit(count = 3)
                        }
                        .decodeList<AffiliateModel>()
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // Tunggu semua proses selesai
            val dataFinance = financeDef.await()
            val dataPesanan = pesananDef.await()
            val dataStok = stokDef.await()
            val dataProduk = produkDef.await()
            val dataAffiliate = affiliateDef.await()

            // Update UI
            withContext(Dispatchers.Main) {
                updateUIKeuangan(view, dataFinance)
                updateUIPesanan(view, dataPesanan.size)
                updateUIStokKritis(view, dataStok.size)
                updateUIBestSellers(view, dataProduk)
                updateUIAffiliate(view, dataAffiliate)
            }
        }
    }

    private fun updateUIKeuangan(view: View, finance: FinanceModel?) {
        val tvOmset = view.findViewById<TextView>(R.id.tvOmset)
        val tvKeuntungan = view.findViewById<TextView>(R.id.tvKeuntungan)
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        val omset = finance?.omset ?: 0L
        val keuntungan = finance?.keuntungan ?: 0L

        tvOmset.text = formatRupiah.format(omset)
        tvKeuntungan.text = formatRupiah.format(keuntungan)
    }

    private fun updateUIPesanan(view: View, jumlah: Int) {
        val tvPesananWeb = view.findViewById<TextView>(R.id.tvPesananWeb)
        tvPesananWeb.text = jumlah.toString()
    }

    private fun updateUIStokKritis(view: View, jumlah: Int) {
        val tvStokKritisCount = view.findViewById<TextView>(R.id.tvStokKritisCount)
        tvStokKritisCount.text = jumlah.toString()
    }

    private fun updateUIBestSellers(view: View, listProduk: List<ProductModel>) {
        val tvProduk1 = view.findViewById<TextView>(R.id.tvProduk1)
        val tvTerjual1 = view.findViewById<TextView>(R.id.tvTerjual1)
        val tvProduk2 = view.findViewById<TextView>(R.id.tvProduk2)
        val tvTerjual2 = view.findViewById<TextView>(R.id.tvTerjual2)
        val tvProduk3 = view.findViewById<TextView>(R.id.tvProduk3)
        val tvTerjual3 = view.findViewById<TextView>(R.id.tvTerjual3)

        if (listProduk.isNotEmpty()) {
            tvProduk1.text = "1. ${listProduk[0].nama_produk}"
            tvTerjual1.text = "Terjual ${listProduk[0].terjual} Kemasan"
        } else {
            tvProduk1.text = "1. Belum ada data"
            tvTerjual1.text = "-"
        }

        if (listProduk.size >= 2) {
            tvProduk2.text = "2. ${listProduk[1].nama_produk}"
            tvTerjual2.text = "Terjual ${listProduk[1].terjual} Kemasan"
        } else {
            tvProduk2.text = "2. Belum ada data"
            tvTerjual2.text = "-"
        }

        if (listProduk.size >= 3) {
            tvProduk3.text = "3. ${listProduk[2].nama_produk}"
            tvTerjual3.text = "Terjual ${listProduk[2].terjual} Kemasan"
        } else {
            tvProduk3.text = "3. Belum ada data"
            tvTerjual3.text = "-"
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
            // Menggunakan layout item_affiliate_rank yang sudah Anda punya
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_affiliate_rank, container, false)

            val tvRank = itemView.findViewById<TextView>(R.id.tvRankNum)
            tvRank.text = "#${index + 1}"
            if (index == 0) tvRank.setTextColor(Color.parseColor("#D4AF37")) // Emas untuk juara 1

            itemView.findViewById<TextView>(R.id.tvAffiliateName).text = affiliate.nama
            itemView.findViewById<TextView>(R.id.tvAffiliateSales).text = "${affiliate.total_penjualan} Penjualan Sukses"
            itemView.findViewById<TextView>(R.id.tvAffiliateKomisi).text = formatRupiah.format(affiliate.komisi)

            container.addView(itemView)
        }
    }
}
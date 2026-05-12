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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// DTO (tidak berubah)
@Serializable
data class TransaksiHomeDTO(val total_harga: Double? = 0.0)
@Serializable
data class ProdukHomeDTO(val nama: String? = "", val stok: Int? = 0)
@Serializable
data class AffiliateHomeDTO(val nama_lengkap: String? = "", val total_komisi: Double? = 0.0)
@Serializable
data class TransaksiAffiliateDTO(val id_affiliate: String? = null, val total_harga: Double? = 0.0)
@Serializable
data class AffiliateProfileDTO(val id_affiliate: String = "", val nama_lengkap: String? = null)
@Serializable
data class DetailTransaksiDTO(val id_produk: String? = null, val nama_produk: String? = null, val jumlah: Int? = 0)
@Serializable
data class ProdukNamaDTO(val id_produk: String = "", val nama: String? = null)

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
                val financeDef = async(Dispatchers.IO) {
                    SupabaseManager.client.from("transaksi")
                        .select { filter { eq("status", "delivered") } }
                        .decodeList<TransaksiHomeDTO>().sumOf { it.total_harga ?: 0.0 }
                }
                val pesananDef = async(Dispatchers.IO) {
                    SupabaseManager.client.from("transaksi")
                        .select { filter { eq("status", "pending") } }
                        .decodeList<TransaksiHomeDTO>().size
                }
                val totalTransaksiDef = async(Dispatchers.IO) {
                    SupabaseManager.client.from("transaksi")
                        .select { filter { eq("status", "delivered") } }
                        .decodeList<TransaksiHomeDTO>().size
                }
                val stokDef = async(Dispatchers.IO) {
                    SupabaseManager.client.from("produk")
                        .select { filter { lt("stok", 5) } }
                        .decodeList<ProdukHomeDTO>().size
                }
                val produkDef = async(Dispatchers.IO) { hitungProdukTerlaris() }
                val affiliateDef = async(Dispatchers.IO) { hitungTopAffiliateBulanIni() }

                val omset = financeDef.await()
                val jmlPesanan = pesananDef.await()
                val jmlTransaksiDelivered = totalTransaksiDef.await()
                val jmlStokKritis = stokDef.await()
                val listProduk = produkDef.await()
                val topAffiliates = affiliateDef.await()

                if (!isAdded) return@launch
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        updateUIKeuangan(view, omset)
                        updateUIPesanan(view, jmlPesanan)
                        updateUITotalTransaksi(view, jmlTransaksiDelivered)
                        updateUIStokKritis(view, jmlStokKritis)
                        updateUIBestSellers(view, listProduk)
                        updateUIAffiliate(view, topAffiliates)
                    }
                }
            } catch (e: Exception) {
                Log.e("HOME_ERROR", "Gagal memuat dashboard: ${e.message}")
            }
        }
    }

    private suspend fun hitungProdukTerlaris(): List<ProdukHomeDTO> {
        return try {
            val detailList = SupabaseManager.client.from("transaksi_detail").select().decodeList<DetailTransaksiDTO>()
            val jumlahPerProduk = mutableMapOf<String, Int>()
            detailList.forEach { detail ->
                val id = detail.id_produk ?: return@forEach
                jumlahPerProduk[id] = (jumlahPerProduk[id] ?: 0) + (detail.jumlah ?: 0)
            }
            if (jumlahPerProduk.isEmpty()) return emptyList()
            val top3Ids = jumlahPerProduk.entries.sortedByDescending { it.value }.take(3)
            val namaMap = runCatching {
                SupabaseManager.client.from("produk").select().decodeList<ProdukNamaDTO>()
                    .associate { it.id_produk to (it.nama ?: "-") }
            }.getOrElse { emptyMap() }
            top3Ids.map { (id, total) -> ProdukHomeDTO(nama = namaMap[id] ?: id, stok = total) }
        } catch (e: Exception) {
            Log.e("PRODUK_TERLARIS", "Error: ${e.message}")
            emptyList()
        }
    }

    private suspend fun hitungTopAffiliateBulanIni(): List<AffiliateHomeDTO> {
        val kalender = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(kalender.time)
        kalender.add(Calendar.MONTH, 1)
        kalender.add(Calendar.SECOND, -1)
        val endStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(kalender.time)

        val transaksiBulanIni = runCatching {
            SupabaseManager.client.from("transaksi").select {
                filter {
                    gte("created_at", startStr)
                    lte("created_at", endStr)
                    or { eq("status", "delivered"); eq("status", "paid") }
                }
            }.decodeList<TransaksiAffiliateDTO>()
        }.getOrElse { return emptyList() }

        val komisiMap = mutableMapOf<String, Long>()
        transaksiBulanIni.forEach { trx ->
            val id = trx.id_affiliate ?: return@forEach
            komisiMap[id] = (komisiMap[id] ?: 0L) + ((trx.total_harga?.toLong() ?: 0L) * 5 / 100)
        }
        if (komisiMap.isEmpty()) return emptyList()
        val namaMap = runCatching {
            SupabaseManager.client.from("affiliate_profiles").select().decodeList<AffiliateProfileDTO>()
                .associate { it.id_affiliate to (it.nama_lengkap ?: "-") }
        }.getOrElse { emptyMap() }
        return komisiMap.entries.sortedByDescending { it.value }.take(3).map { (id, komisi) ->
            AffiliateHomeDTO(nama_lengkap = namaMap[id] ?: id, total_komisi = komisi.toDouble())
        }
    }

    private fun updateUIKeuangan(view: View, omset: Double) { if (!isAdded) return
        val tvOmset = view.findViewById<TextView>(R.id.tvOmset)
        val tvKeuntungan = view.findViewById<TextView>(R.id.tvKeuntungan)
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        tvOmset.text = format.format(omset)
        tvKeuntungan.text = format.format(omset * 0.30)
    }
    private fun updateUIPesanan(view: View, jumlah: Int) { if (!isAdded) return
        view.findViewById<TextView>(R.id.tvPesananWeb).text = jumlah.toString()
    }
    private fun updateUITotalTransaksi(view: View, jumlah: Int) { if (!isAdded) return
        view.findViewById<TextView>(R.id.tvTotalTransaksiHome).text = jumlah.toString()
    }
    private fun updateUIStokKritis(view: View, jumlah: Int) { if (!isAdded) return
        view.findViewById<TextView>(R.id.tvStokKritisCount).text = jumlah.toString()
    }
    private fun updateUIBestSellers(view: View, listProduk: List<ProdukHomeDTO>) { if (!isAdded) return
        val views = listOf(
            view.findViewById<TextView>(R.id.tvProduk1) to view.findViewById<TextView>(R.id.tvTerjual1),
            view.findViewById<TextView>(R.id.tvProduk2) to view.findViewById<TextView>(R.id.tvTerjual2),
            view.findViewById<TextView>(R.id.tvProduk3) to view.findViewById<TextView>(R.id.tvTerjual3)
        )
        views.forEachIndexed { i, (tvNama, tvTerjual) ->
            if (i < listProduk.size) {
                tvNama.text = "${i+1}. ${listProduk[i].nama}"
                tvTerjual.text = "Terjual: ${listProduk[i].stok} kemasan"
            } else {
                tvNama.text = "${i+1}. Belum ada data"
                tvTerjual.text = "-"
            }
        }
    }
    private fun updateUIAffiliate(view: View, listAffiliate: List<AffiliateHomeDTO>) { if (!isAdded) return
        val container = view.findViewById<LinearLayout>(R.id.llContainerAffiliate)
        container.removeAllViews()
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        if (listAffiliate.isEmpty()) {
            container.addView(TextView(requireContext()).apply {
                text = "Belum ada data affiliate bulan ini."
                setPadding(16, 16, 16, 16)
                setTextColor(Color.GRAY)
            })
            return
        }
        listAffiliate.forEachIndexed { index, affiliate ->
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_affiliate_rank, container, false)
            val tvRank = itemView.findViewById<TextView>(R.id.tvRankNum).apply { text = "#${index + 1}" }
            if (index == 0) tvRank.setTextColor(Color.parseColor("#D4AF37"))
            val estimasiPenjualan = ((affiliate.total_komisi ?: 0.0) / 15000).toInt()
            itemView.findViewById<TextView>(R.id.tvAffiliateName).text = affiliate.nama_lengkap
            itemView.findViewById<TextView>(R.id.tvAffiliateSales).text = "$estimasiPenjualan Penjualan Sukses"
            itemView.findViewById<TextView>(R.id.tvAffiliateKomisi).text = formatRupiah.format(affiliate.total_komisi)
            container.addView(itemView)
        }
    }
}
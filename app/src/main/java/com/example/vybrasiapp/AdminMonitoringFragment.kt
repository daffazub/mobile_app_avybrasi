package com.example.vybrasiapp

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AdminMonitoringFragment : Fragment() {

    private lateinit var adapter: PayoutAdapter
    private val payoutList = mutableListOf<PayoutItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_monitoring, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rvPayoutRequests)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = PayoutAdapter(payoutList) { item -> showVerificationDialog(item) }
        rv.adapter = adapter
        loadPendingPayouts()
    }

    private fun loadPendingPayouts() {
        lifecycleScope.launch {
            val rawList = withContext(Dispatchers.IO) {
                SupabaseManager.getPayoutRequests().filter { it.status == "pending" }
            }
            Log.d("MONITORING", "Jumlah pending: ${rawList.size}")
            payoutList.clear()
            for (req in rawList) {
                val aff = withContext(Dispatchers.IO) {
                    SupabaseManager.getAffiliateById(req.id_affiliate)
                }
                val payment = aff?.payment_method ?: "{}"
                val bank = try {
                    org.json.JSONObject(payment).optString("bank", "Belum diatur")
                } catch (e: Exception) { "Belum diatur" }
                val nama = aff?.nama_lengkap ?: "Unknown"

                payoutList.add(PayoutItem(
                    idRequest = req.id_request,
                    idAffiliate = req.id_affiliate,
                    namaAffiliate = nama,
                    jumlah = req.jumlah,
                    bank = bank,
                    tanggal = req.created_at ?: "",
                    status = req.status ?: ""
                ))
            }
            Log.d("MONITORING", "Item setelah diproses: ${payoutList.size}")
            adapter.notifyDataSetChanged()
        }
    }

    // ── Dialog Verifikasi dengan pengecekan lengkap ─────────
    private fun showVerificationDialog(item: PayoutItem) {
        lifecycleScope.launch {
            // 1. Ambil data affiliate
            val aff = try {
                withContext(Dispatchers.IO) { SupabaseManager.getAffiliateById(item.idAffiliate) }
            } catch (e: Exception) {
                Log.e("VERIFY", "Gagal ambil affiliate: ${e.message}")
                Toast.makeText(requireContext(), "Gagal mengambil data affiliate", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (aff == null) {
                Toast.makeText(requireContext(), "Data affiliate tidak ditemukan", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 2. Hitung saldo tersedia
            val saldo = try {
                val totalPayout = withContext(Dispatchers.IO) {
                    try {
                        val url = "${SupabaseManager.SUPABASE_URL}/rest/v1/keuangan?select=jumlah&id_affiliate=eq.${aff.id_affiliate}&tipe=eq.payout&schema=jualan_kopi"
                        val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.setRequestProperty("apikey", SupabaseManager.SUPABASE_KEY)
                        conn.setRequestProperty("Authorization", "Bearer ${SupabaseManager.SUPABASE_KEY}")
                        if (conn.responseCode in 200..299) {
                            val text = conn.inputStream.bufferedReader().readText()
                            conn.disconnect()
                            val arr = org.json.JSONArray(text)
                            var sum = 0.0
                            for (i in 0 until arr.length()) sum += arr.getJSONObject(i).getDouble("jumlah")
                            sum
                        } else { conn.disconnect(); 0.0 }
                    } catch (e: Exception) { 0.0 }
                }
                (aff.total_komisi ?: 0.0) - totalPayout
            } catch (e: Exception) { aff.total_komisi ?: 0.0 }

            // 3. Parsing payment method
            val paymentObj = try {
                org.json.JSONObject(aff.payment_method ?: "{}")
            } catch (e: Exception) { org.json.JSONObject() }

            // 4. Inflate layout dialog
            val dialogView = try {
                layoutInflater.inflate(R.layout.dialog_payout_verification, null)
            } catch (e: Exception) {
                Log.e("VERIFY", "Layout dialog tidak ditemukan: ${e.message}")
                Toast.makeText(requireContext(), "Layout dialog tidak tersedia", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val tvNama = dialogView.findViewById<TextView>(R.id.tvDetailNama)
            val tvJumlah = dialogView.findViewById<TextView>(R.id.tvDetailJumlah)
            val tvBank = dialogView.findViewById<TextView>(R.id.tvDetailBank)
            val tvRekening = dialogView.findViewById<TextView>(R.id.tvDetailRekening)
            val tvAtasNama = dialogView.findViewById<TextView>(R.id.tvDetailAtasNama)
            val tvSaldo = dialogView.findViewById<TextView>(R.id.tvDetailSaldo)
            val etAlasan = dialogView.findViewById<EditText>(R.id.etAlasanTolak)

            if (tvNama == null || tvJumlah == null || tvBank == null || tvRekening == null || tvAtasNama == null || tvSaldo == null) {
                Log.e("VERIFY", "Satu atau lebih view tidak ditemukan di dialog")
                Toast.makeText(requireContext(), "Kesalahan tampilan dialog", Toast.LENGTH_SHORT).show()
                return@launch
            }

            tvNama.text = item.namaAffiliate
            tvJumlah.text = SupabaseManager.formatRupiah(item.jumlah)
            tvBank.text = paymentObj.optString("bank", "Belum diatur")
            tvRekening.text = paymentObj.optString("nomor_rekening", "-")
            tvAtasNama.text = paymentObj.optString("atas_nama", "-")
            tvSaldo.text = SupabaseManager.formatRupiah(saldo)

            // 5. Tampilkan dialog
            try {
                AlertDialog.Builder(requireContext())
                    .setTitle("Verifikasi Penarikan")
                    .setView(dialogView)
                    .setPositiveButton("Setujui") { _, _ -> approvePayout(item) }
                    .setNegativeButton("Tolak") { _, _ ->
                        val alasan = etAlasan?.text?.toString()?.trim() ?: ""
                        rejectPayout(item, alasan)
                    }
                    .setNeutralButton("Batal", null)
                    .show()
            } catch (e: Exception) {
                Log.e("VERIFY", "Gagal menampilkan dialog: ${e.message}")
                Toast.makeText(requireContext(), "Gagal menampilkan dialog", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Setujui ─────────────────────────────────
    private fun approvePayout(item: PayoutItem) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                SupabaseManager.updatePayoutStatus(item.idRequest, "disetujui")
            }
            if (result != "OK") {
                Toast.makeText(requireContext(), "Gagal menyetujui: $result", Toast.LENGTH_LONG).show()
                return@launch
            }

            val keuanganData = mapOf<String, Any?>(
                "id_keuangan" to UUID.randomUUID().toString(),
                "id_affiliate" to item.idAffiliate,
                "tipe" to "payout",
                "jumlah" to item.jumlah,
                "saldo_sebelum" to 0.0,
                "saldo_sesudah" to 0.0,
                "status" to "completed",
                "keterangan" to "Payout disetujui admin",
                "created_at" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(Date())
            )
            val insertKeu = withContext(Dispatchers.IO) {
                SupabaseManager.insertKeuangan(keuanganData)
            }
            if (insertKeu == "OK") {
                Toast.makeText(requireContext(), "✅ Penarikan disetujui, dicatat di keuangan.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "✅ Disetujui, tapi gagal catat keuangan: $insertKeu", Toast.LENGTH_LONG).show()
            }
            loadPendingPayouts()
        }
    }

    // ── Tolak ────────────────────────────────────
    private fun rejectPayout(item: PayoutItem, alasan: String) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = "${SupabaseManager.SUPABASE_URL}/rest/v1/payout_requests?id_request=eq.${item.idRequest}&schema=jualan_kopi"
                    val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "PATCH"
                    conn.setRequestProperty("apikey", SupabaseManager.SUPABASE_KEY)
                    conn.setRequestProperty("Authorization", "Bearer ${SupabaseManager.SUPABASE_KEY}")
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("Prefer", "return=minimal")
                    conn.doOutput = true
                    val json = org.json.JSONObject().apply {
                        put("status", "ditolak")
                        if (alasan.isNotBlank()) put("keterangan_admin", alasan)
                        put("updated_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(Date()))
                    }
                    conn.outputStream.write(json.toString().toByteArray())
                    val code = conn.responseCode
                    if (code in 200..299) "OK" else "Error $code"
                } catch (e: Exception) { e.message ?: "Gagal" }
            }
            if (result == "OK") {
                Toast.makeText(requireContext(), "Pengajuan ditolak.${if (alasan.isNotBlank()) " Alasan: $alasan" else ""}", Toast.LENGTH_LONG).show()
                loadPendingPayouts()
            } else {
                Toast.makeText(requireContext(), "Gagal menolak: $result", Toast.LENGTH_LONG).show()
            }
        }
    }
}
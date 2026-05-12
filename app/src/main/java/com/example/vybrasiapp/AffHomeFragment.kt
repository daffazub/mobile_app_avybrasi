package com.example.vybrasiapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.vybrasiapp.SupabaseManager.Affiliate
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AffHomeFragment : Fragment() {

    private lateinit var tvNama: TextView
    private lateinit var tvKode: TextView
    private lateinit var tvBadge: TextView
    private lateinit var tvTotalKomisi: TextView
    private lateinit var tvKomisiPersen: TextView
    private lateinit var tvMinPayout: TextView
    private lateinit var tvSaldoTersedia: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnAjukan: Button
    private lateinit var btnSalin: TextView
    private lateinit var tilJumlah: TextInputLayout
    private lateinit var etJumlah: EditText

    private var currentAffiliate: Affiliate? = null
    private var saldoTersedia: Double = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_aff_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvNama = view.findViewById(R.id.tvAffHomeName)
        tvKode = view.findViewById(R.id.tvAffHomeKode)
        tvBadge = view.findViewById(R.id.tvAffHomeBadge)
        tvStatus = view.findViewById(R.id.tvAffHomeStatus)
        tvTotalKomisi = view.findViewById(R.id.tvAffHomeTotalKomisi)
        tvKomisiPersen = view.findViewById(R.id.tvAffHomeKomisiPersen)
        tvMinPayout = view.findViewById(R.id.tvAffHomeMinPayout)
        tvSaldoTersedia = view.findViewById(R.id.tvAffHomeSaldoTersedia)
        btnAjukan = view.findViewById(R.id.btnAjukanPenarikan)
        btnSalin = view.findViewById(R.id.btnSalinKode)
        tilJumlah = view.findViewById(R.id.tilJumlahPenarikan)
        etJumlah = view.findViewById(R.id.etJumlahPenarikan)

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val affiliate = withContext(Dispatchers.IO) {
                SupabaseManager.getAffiliateProfile()
            }
            if (affiliate == null) {
                tvNama.text = "Gagal memuat data"
                return@launch
            }

            currentAffiliate = affiliate

            // Hitung total komisi
            tvNama.text = affiliate.nama_lengkap ?: "-"
            tvKode.text = affiliate.kode_referal ?: "-"
            tvTotalKomisi.text = SupabaseManager.formatRupiah(affiliate.total_komisi ?: 0.0)
            tvKomisiPersen.text = "${affiliate.komisi_persen?.toInt() ?: 0}%"
            tvMinPayout.text = SupabaseManager.formatRupiah(affiliate.minimum_payout ?: 100000.0)

            // Hitung saldo tersedia (total komisi - total payout)
            val totalPayout = withContext(Dispatchers.IO) {
                // Query langsung ke tabel keuangan via REST (ada di SupabaseManager)
                try {
                    val url = "${SupabaseManager.SUPABASE_URL}/rest/v1/keuangan?select=jumlah&id_affiliate=eq.${affiliate.id_affiliate}&tipe=eq.payout&schema=jualan_kopi"
                    val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("apikey", SupabaseManager.SUPABASE_KEY)
                    conn.setRequestProperty("Authorization", "Bearer ${SupabaseManager.SUPABASE_KEY}")
                    val code = conn.responseCode
                    if (code in 200..299) {
                        val text = conn.inputStream.bufferedReader().readText()
                        conn.disconnect()
                        val arr = org.json.JSONArray(text)
                        var sum = 0.0
                        for (i in 0 until arr.length()) {
                            sum += arr.getJSONObject(i).getDouble("jumlah")
                        }
                        sum
                    } else {
                        conn.disconnect()
                        0.0
                    }
                } catch (e: Exception) {
                    Log.e("AFF_HOME", "Gagal hitung payout", e)
                    0.0
                }
            }
            saldoTersedia = (affiliate.total_komisi ?: 0.0) - totalPayout
            tvSaldoTersedia.text = SupabaseManager.formatRupiah(saldoTersedia)

            // Status badge
            val status = affiliate.status_affiliate ?: "inactive"
            tvStatus.text = status.replaceFirstChar { it.uppercase() }
            if (status == "active") {
                tvBadge.text = "AKTIF"
                tvBadge.setBackgroundColor(android.graphics.Color.parseColor("#C9A84C"))
            } else {
                tvBadge.text = "NONAKTIF"
                tvBadge.setBackgroundColor(android.graphics.Color.parseColor("#888888"))
            }

            // Tampilkan menu penarikan jika memenuhi minimum & status active
            val boleh = status == "active" && saldoTersedia >= (affiliate.minimum_payout ?: 100000.0)
            tilJumlah.visibility = if (boleh) View.VISIBLE else View.GONE
            btnAjukan.visibility = if (boleh) View.VISIBLE else View.GONE

            if (!boleh && status == "active") {
                Toast.makeText(requireContext(), "Saldo belum mencapai minimum penarikan (Rp ${affiliate.minimum_payout?.toInt() ?: 100000})", Toast.LENGTH_SHORT).show()
            }

            btnAjukan.setOnClickListener {
                ajukanPenarikan(affiliate)
            }

            btnSalin.setOnClickListener {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("kode", affiliate.kode_referal ?: ""))
                Toast.makeText(requireContext(), "Kode Disalin! ✅", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ajukanPenarikan(affiliate: Affiliate) {
        val jumlahStr = etJumlah.text.toString().trim()
        if (jumlahStr.isEmpty()) {
            etJumlah.error = "Masukkan jumlah"
            return
        }
        val jumlah = jumlahStr.toDoubleOrNull()
        if (jumlah == null || jumlah <= 0) {
            etJumlah.error = "Jumlah tidak valid"
            return
        }
        val min = affiliate.minimum_payout ?: 100000.0
        if (jumlah < min) {
            etJumlah.error = "Minimal Rp ${SupabaseManager.formatRupiah(min)}"
            return
        }
        if (jumlah > saldoTersedia) {
            etJumlah.error = "Melebihi saldo tersedia"
            return
        }

        // Cek pengajuan pending
        lifecycleScope.launch {
            val pendingResult = withContext(Dispatchers.IO) {
                try {
                    val url = "${SupabaseManager.SUPABASE_URL}/rest/v1/payout_requests?select=id_request&id_affiliate=eq.${affiliate.id_affiliate}&status=eq.pending&schema=jualan_kopi"
                    val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("apikey", SupabaseManager.SUPABASE_KEY)
                    conn.setRequestProperty("Authorization", "Bearer ${SupabaseManager.SUPABASE_KEY}")
                    val code = conn.responseCode
                    if (code in 200..299) {
                        val text = conn.inputStream.bufferedReader().readText()
                        conn.disconnect()
                        val arr = org.json.JSONArray(text)
                        arr.length() > 0
                    } else {
                        conn.disconnect()
                        false
                    }
                } catch (e: Exception) {
                    Log.e("AFF_HOME", "Gagal cek pending", e)
                    false
                }
            }
            if (pendingResult) {
                Toast.makeText(requireContext(), "Anda sudah memiliki pengajuan yang belum disetujui.", Toast.LENGTH_LONG).show()
                return@launch
            }

            // Insert payout request
            val request = mapOf<String, Any?>(
                "id_request" to UUID.randomUUID().toString(),
                "id_affiliate" to affiliate.id_affiliate,
                "jumlah" to jumlah,
                "status" to "pending",
                "created_at" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(Date()),
                "updated_at" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(Date())
            )
            val result = withContext(Dispatchers.IO) {
                SupabaseManager.insertPayoutRequest(request)
            }
            if (result == "OK") {
                Toast.makeText(requireContext(), "✅ Penarikan diajukan! Menunggu persetujuan admin.", Toast.LENGTH_LONG).show()
                etJumlah.text?.clear()
                loadData()
            } else {
                Toast.makeText(requireContext(), "❌ Gagal mengajukan: $result", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun formatRupiah(amount: Double): String {
        return SupabaseManager.formatRupiah(amount)
    }
}
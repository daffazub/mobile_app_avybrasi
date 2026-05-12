package com.example.vybrasiapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vybrasiapp.SupabaseManager.Affiliate
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class AdminPayoutDetailActivity : AppCompatActivity() {

    private lateinit var tvNama: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvKode: TextView
    private lateinit var tvJumlah: TextView
    private lateinit var tvMetode: TextView
    private lateinit var tilAlasan: TextInputLayout
    private lateinit var etAlasan: EditText
    private lateinit var btnSetujui: Button
    private lateinit var btnTolak: Button

    private var idRequest: String = ""
    private var idAffiliate: String = ""
    private var jumlah: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_payout_detail)

        tvNama = findViewById(R.id.tvDetailNama)
        tvEmail = findViewById(R.id.tvDetailEmail)
        tvKode = findViewById(R.id.tvDetailKodeReferral)
        tvJumlah = findViewById(R.id.tvDetailJumlah)
        tvMetode = findViewById(R.id.tvDetailPaymentMethod)
        tilAlasan = findViewById(R.id.tilAlasan)
        etAlasan = findViewById(R.id.etAlasan)
        btnSetujui = findViewById(R.id.btnSetujui)
        btnTolak = findViewById(R.id.btnTolak)

        idRequest = intent.getStringExtra("id_request") ?: ""
        idAffiliate = intent.getStringExtra("id_affiliate") ?: ""
        jumlah = intent.getDoubleExtra("jumlah", 0.0)

        if (idRequest.isEmpty()) {
            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadDetailAffiliate()

        btnSetujui.setOnClickListener { prosesPayout("disetujui") }
        btnTolak.setOnClickListener {
            tilAlasan.visibility = View.VISIBLE
            if (etAlasan.text.isNotEmpty()) {
                prosesPayout("ditolak", etAlasan.text.toString())
            } else {
                Toast.makeText(this, "Isi alasan penolakan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDetailAffiliate() {
        lifecycleScope.launch {
            val affiliate = withContext(Dispatchers.IO) {
                try {
                    val url = java.net.URL("${SupabaseManager.SUPABASE_URL}/rest/v1/affiliate_profiles?id_affiliate=eq.${idAffiliate}&select=*")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.setRequestProperty("apikey", SupabaseManager.SUPABASE_KEY)
                    conn.setRequestProperty("Authorization", "Bearer ${SupabaseManager.SUPABASE_KEY}")
                    val text = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    val arr = org.json.JSONArray(text)
                    if (arr.length() > 0) {
                        val obj = arr.getJSONObject(0)
                        Affiliate(
                            id_affiliate = obj.getString("id_affiliate"),
                            nama_lengkap = obj.optString("nama_lengkap"),
                            kode_referal = obj.optString("kode_referal"),
                            payment_method = obj.optString("payment_method"),
                            user_id = obj.optString("user_id")
                        )
                    } else null
                } catch (e: Exception) { null }
            }

            if (affiliate == null) {
                Toast.makeText(this@AdminPayoutDetailActivity, "Gagal memuat data affiliate", Toast.LENGTH_SHORT).show()
                return@launch
            }

            tvNama.text = "Nama: ${affiliate.nama_lengkap ?: "-"}"
            tvKode.text = "Kode Referral: ${affiliate.kode_referal ?: "-"}"
            tvJumlah.text = "Jumlah Penarikan: Rp ${SupabaseManager.formatRupiah(jumlah)}"

            val email = withContext(Dispatchers.IO) {
                try {
                    val url = java.net.URL("${SupabaseManager.SUPABASE_URL}/rest/v1/profiles?user_id=eq.${affiliate.user_id}&select=email")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.setRequestProperty("apikey", SupabaseManager.SUPABASE_KEY)
                    conn.setRequestProperty("Authorization", "Bearer ${SupabaseManager.SUPABASE_KEY}")
                    val text = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    val arr = org.json.JSONArray(text)
                    if (arr.length() > 0) arr.getJSONObject(0).optString("email") else "-"
                } catch (e: Exception) { "-" }
            }
            tvEmail.text = "Email: $email"

            try {
                val pmStr = affiliate.payment_method ?: ""
                if (pmStr.isNotEmpty() && pmStr != "{}") {
                    val pm = JSONObject(pmStr)
                    val type = pm.optString("type", "-")
                    val holder = pm.optString("account_holder", "-")
                    val number = pm.optString("account_number", "-")
                    val bank = if (type == "bank") pm.optString("bank_name", "") else ""
                    tvMetode.text = "Tipe: $type\nPemilik: $holder\nNomor: $number${if (bank.isNotEmpty()) "\nBank: $bank" else ""}"
                } else {
                    tvMetode.text = "Metode pembayaran belum diatur"
                }
            } catch (e: Exception) {
                tvMetode.text = "Metode pembayaran tidak valid"
            }
        }
    }

    private fun prosesPayout(status: String, keterangan: String? = null) {
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val updateResult = SupabaseManager.updatePayoutStatus(idRequest, status)
                    if (!updateResult.startsWith("OK")) {
                        Log.e("PAYOUT_DETAIL", "Gagal update status: $updateResult")
                        return@withContext false
                    }

                    if (status == "disetujui") {
                        val data = mapOf(
                            "id_keuangan" to UUID.randomUUID().toString(),
                            "id_affiliate" to idAffiliate,
                            "tipe" to "payout",
                            "jumlah" to -jumlah,
                            "status" to "completed",
                            "keterangan" to "Payout disetujui admin"
                        )
                        SupabaseManager.insertKeuangan(data)
                    }
                    true
                } catch (e: Exception) {
                    Log.e("PAYOUT_DETAIL", "Error: ${e.message}", e)
                    false
                }
            }

            if (ok) {
                Toast.makeText(this@AdminPayoutDetailActivity, "Status: $status", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@AdminPayoutDetailActivity, "Gagal memproses", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
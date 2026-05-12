package com.example.vybrasiapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class AffProfilFragment : Fragment() {

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    private lateinit var ivAvatar: ImageView
    private lateinit var tvNama: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvBadge: TextView
    private lateinit var tvKode: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvKomisi: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvMin: TextView
    private lateinit var tvPayment: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_aff_profil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivAvatar   = view.findViewById(R.id.ivAvatar)
        tvNama     = view.findViewById(R.id.tvProfilNama)
        tvEmail    = view.findViewById(R.id.tvProfilEmail)
        tvBadge    = view.findViewById(R.id.tvProfilBadge)
        tvKode     = view.findViewById(R.id.tvProfilKode)
        tvStatus   = view.findViewById(R.id.tvProfilStatus)
        tvKomisi   = view.findViewById(R.id.tvProfilKomisi)
        tvTotal    = view.findViewById(R.id.tvProfilTotalKomisi)
        tvMin      = view.findViewById(R.id.tvProfilMinPayout)
        tvPayment  = view.findViewById(R.id.tvProfilPayment)
        btnEdit    = view.findViewById(R.id.btnEditPayment)
        btnLogout  = view.findViewById(R.id.btnLogout)

        loadProfile()
        btnEdit.setOnClickListener { showEditPaymentDialog() }
        btnLogout.setOnClickListener { performLogout() }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val affiliate = withContext(Dispatchers.IO) {
                    SupabaseManager.getAffiliateProfile()
                }
                val email = withContext(Dispatchers.IO) {
                    SupabaseManager.getCurrentUserEmail()
                } ?: "-"
                val name = withContext(Dispatchers.IO) {
                    SupabaseManager.getCurrentUserName()
                } ?: (affiliate?.nama_lengkap ?: "Tanpa Nama")

                tvNama.text = name
                tvEmail.text = email

                // Avatar (kalau ada di tabel profiles)
                val profile = withContext(Dispatchers.IO) {
                    SupabaseManager.getUserProfile()
                }
                val avatarUrl = profile?.avatar_url ?: ""
                if (avatarUrl.isNotBlank()) {
                    com.bumptech.glide.Glide.with(this@AffProfilFragment)
                        .load(avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(ivAvatar)
                }

                if (affiliate != null) {
                    tvKode.text = affiliate.kode_referal ?: "-"
                    tvKomisi.text = "${affiliate.komisi_persen?.toInt() ?: 0}%"
                    tvTotal.text = formatRupiah.format(affiliate.total_komisi ?: 0.0)
                    tvMin.text = formatRupiah.format(affiliate.minimum_payout ?: 0.0)

                    val status = affiliate.status_affiliate ?: "active"
                    tvStatus.text = status.uppercase()
                    tvBadge.text = if (status == "active") "AFFILIATE AKTIF" else "NONAKTIF"

                    // Payment method
                    val raw = affiliate.payment_method ?: ""
                    val clean = if (raw.isNotBlank() && raw != "null" && raw.length > 5) {
                        try {
                            val json = JSONObject(raw)
                            val bank = json.optString("bank", "")
                            val rek = json.optString("nomor_rekening", "")
                            val atasNama = json.optString("atas_nama", "")
                            "Bank: $bank\nRek: $rek\na/n: $atasNama"
                        } catch (e: Exception) {
                            "Tidak valid"
                        }
                    } else "Belum diatur"
                    tvPayment.text = clean
                } else {
                    tvNama.text = "Data tidak tersedia"
                }
            } catch (e: Exception) {
                Log.e("PROFIL_AFF", "Error: ${e.message}")
                Toast.makeText(requireContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditPaymentDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_payment, null)
        builder.setView(dialogView)

        val etBank = dialogView.findViewById<EditText>(R.id.etBank)
        val etRekening = dialogView.findViewById<EditText>(R.id.etRekening)
        val etAtasNama = dialogView.findViewById<EditText>(R.id.etAtasNama)

        // Load existing
        lifecycleScope.launch {
            val aff = withContext(Dispatchers.IO) {
                SupabaseManager.getAffiliateProfile()
            }
            if (aff != null) {
                val raw = aff.payment_method ?: ""
                if (raw.isNotBlank() && raw != "null") {
                    try {
                        val json = JSONObject(raw)
                        etBank.setText(json.optString("bank", ""))
                        etRekening.setText(json.optString("nomor_rekening", ""))
                        etAtasNama.setText(json.optString("atas_nama", ""))
                    } catch (_: Exception) {}
                }
            }
        }

        builder.setPositiveButton("Simpan") { dialog, _ ->
            val bank = etBank.text.toString().trim()
            val rek = etRekening.text.toString().trim()
            val atasNama = etAtasNama.text.toString().trim()
            if (bank.isEmpty() || rek.isEmpty() || atasNama.isEmpty()) {
                Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            val paymentJson = JSONObject().apply {
                put("bank", bank)
                put("nomor_rekening", rek)
                put("atas_nama", atasNama)
            }.toString()
            updatePaymentMethod(paymentJson)
        }
        builder.setNegativeButton("Batal", null)
        builder.show()
    }

    private fun updatePaymentMethod(newPaymentJson: String) {
        lifecycleScope.launch {
            val userId = withContext(Dispatchers.IO) {
                SupabaseManager.getCurrentUserId()
            } ?: return@launch
            Log.d("PROFIL_AFF", "Memperbarui payment utk userId=$userId dengan data=$newPaymentJson")
            val success = withContext(Dispatchers.IO) {
                SupabaseManager.updateAffiliatePayment(userId, newPaymentJson)
            }
            if (success) {
                Toast.makeText(requireContext(), "Metode pembayaran diperbarui", Toast.LENGTH_SHORT).show()
                loadProfile()
            } else {
                Toast.makeText(requireContext(), "Gagal memperbarui", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    SupabaseManager.logout()
                } catch (e: Exception) {
                    Log.e("LOGOUT", "Error: ${e.message}")
                }
            }
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
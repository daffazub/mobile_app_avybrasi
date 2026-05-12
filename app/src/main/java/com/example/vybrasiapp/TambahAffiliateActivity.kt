package com.example.vybrasiapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class TambahAffiliateActivity : AppCompatActivity() {

    private lateinit var etNamaLengkap: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etKomisiPersen: EditText
    private lateinit var etMinimumPayout: EditText
    private lateinit var btnSubmit: Button
    private lateinit var ivToggle: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_affiliate)

        etNamaLengkap = findViewById(R.id.etNamaLengkap)
        etEmail = findViewById(R.id.etEmailAffiliate)
        etPassword = findViewById(R.id.etPasswordAffiliate)
        etKomisiPersen = findViewById(R.id.etKomisiPersen)
        etMinimumPayout = findViewById(R.id.etMinimumPayout)
        btnSubmit = findViewById(R.id.btnSubmitAffiliate)
        ivToggle = findViewById(R.id.ivTogglePassword)

        // Tombol submit
        btnSubmit.setOnClickListener {
            validateAndCreate()
        }

        // Tombol batal
        findViewById<TextView>(R.id.btnBatal).setOnClickListener {
            finish()
        }

        // Toggle password visibility
        ivToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggle.setImageResource(android.R.drawable.ic_menu_view)
            }
            etPassword.setSelection(etPassword.text.length)
        }
    }

    private fun validateAndCreate() {
        val nama = etNamaLengkap.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val komisi = etKomisiPersen.text.toString().trim().toDoubleOrNull() ?: 5.0
        val minPayout = etMinimumPayout.text.toString().trim().toDoubleOrNull() ?: 100000.0

        if (nama.isEmpty()) {
            etNamaLengkap.error = "Nama wajib diisi"
            return
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email tidak valid"
            return
        }
        if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            return
        }
        if (komisi < 0 || komisi > 100) {
            etKomisiPersen.error = "0-100"
            return
        }
        if (minPayout < 10000) {
            etMinimumPayout.error = "Minimal 10.000"
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "Membuat..."

        lifecycleScope.launch {
            val resultMessage = withContext(Dispatchers.IO) {
                try {
                    // 1. Buat user di Auth
                    val userId = SupabaseManager.createUserWithEmail(email, password)
                    Log.d("TAMBAH_AFF", "createUserWithEmail returned: $userId")
                    if (userId == null) {
                        return@withContext "Gagal membuat user. Email mungkin sudah terdaftar."
                    }

                    // 2. Buat data affiliate
                    val idAffiliate = UUID.randomUUID().toString()
                    val kodeReferal = "REF${UUID.randomUUID().toString().take(6).uppercase()}"
                    val affiliateData = mapOf<String, Any?>(
                        "id_affiliate" to idAffiliate,
                        "user_id" to userId,
                        "nama_lengkap" to nama,
                        "kode_referal" to kodeReferal,
                        "komisi_persen" to komisi,
                        "total_komisi" to 0.0,
                        "minimum_payout" to minPayout,
                        "status_affiliate" to "active",
                        "payment_method" to "{}"
                    )

                    // 3. Insert ke database
                    SupabaseManager.insertAffiliate(affiliateData) // mengembalikan String
                } catch (e: Exception) {
                    Log.e("TAMBAH_AFF", "Error: ${e.message}", e)
                    "Gagal: ${e.message}"
                }
            }

            btnSubmit.isEnabled = true
            btnSubmit.text = "Buat Affiliate"

            if (resultMessage == "OK") {
                Toast.makeText(this@TambahAffiliateActivity, "✅ Affiliate berhasil ditambahkan!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this@TambahAffiliateActivity, "❌ $resultMessage", Toast.LENGTH_LONG).show()
            }
        }
    }
}
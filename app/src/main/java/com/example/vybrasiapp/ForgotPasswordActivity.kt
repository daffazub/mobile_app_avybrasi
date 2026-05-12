package com.example.vybrasiapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Status bar hitam
        window.statusBarColor = android.graphics.Color.parseColor("#1A1A1A")
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        val btnBack   = findViewById<android.widget.TextView>(R.id.btnBackForgot)
        val etEmail   = findViewById<TextInputEditText>(R.id.etForgotEmail)
        val btnKirim  = findViewById<Button>(R.id.btnKirimReset)

        btnBack.setOnClickListener { finish() }

        btnKirim.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            kirimResetPassword(email, btnKirim)
        }
    }

    private fun kirimResetPassword(email: String, btnKirim: Button) {
        btnKirim.text      = "Mengirim..."
        btnKirim.isEnabled = false

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseManager.client.auth.resetPasswordForEmail(email)
                }

                // ✅ Dialog dengan tombol buka email langsung
                AlertDialog.Builder(this@ForgotPasswordActivity)
                    .setTitle("✅ Link Terkirim!")
                    .setMessage(
                        "Link reset password sudah dikirim ke:\n\n" +
                                "📧 $email\n\n" +
                                "Langkah selanjutnya:\n" +
                                "1️⃣ Buka aplikasi Email kamu\n" +
                                "2️⃣ Cari email dari Supabase\n" +
                                "3️⃣ Klik link di dalam email\n" +
                                "4️⃣ Isi password baru\n" +
                                "5️⃣ Kembali ke app & login\n\n" +
                                "⚠️ Link berlaku selama 1 jam.\n" +
                                "Cek folder Spam jika tidak ada."
                    )
                    .setPositiveButton("📬 Buka Email Sekarang") { _, _ ->
                        // ✅ Langsung buka app email
                        val emailIntent = android.content.Intent(
                            android.content.Intent.ACTION_MAIN
                        ).apply {
                            addCategory(android.content.Intent.CATEGORY_APP_EMAIL)
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            startActivity(emailIntent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@ForgotPasswordActivity,
                                "Buka aplikasi email kamu secara manual",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        finish()
                    }
                    .setNegativeButton("Nanti") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()

            } catch (e: Exception) {
                Log.e("AUTH_ERROR", "Reset gagal: ${e.message}")
                AlertDialog.Builder(this@ForgotPasswordActivity)
                    .setTitle("❌ Gagal Mengirim Email")
                    .setMessage(
                        "Email:\n$email\n\n" +
                                "tidak ditemukan di sistem Vybrasi.\n\n" +
                                "Pastikan email sudah benar atau\nhubungi Owner untuk bantuan."
                    )
                    .setPositiveButton("Coba Lagi", null)
                    .show()
            } finally {
                btnKirim.text      = "Kirim Link Reset"
                btnKirim.isEnabled = true
            }
        }
    }
}
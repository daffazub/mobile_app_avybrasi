package com.example.vybrasiapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vybrasiapp.model.ProfileModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var tvLupaPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail        = findViewById<EditText>(R.id.etEmail)
        val etPassword     = findViewById<EditText>(R.id.etPassword)
        val btnLogin       = findViewById<Button>(R.id.btnLogin)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)
        tvLupaPassword     = findViewById(R.id.tvLupaPassword)

        // ── Cek session aktif ──────────────────────────────
        lifecycleScope.launch {
            val session = withContext(Dispatchers.IO) {
                try { SupabaseManager.client.auth.currentSessionOrNull() } catch (e: Exception) { null }
            }
            if (session != null) {
                // Sudah login, langsung arahkan ke RoleLauncherActivity
                startActivity(Intent(this@LoginActivity, RoleLauncherActivity::class.java))
                finish()
                return@launch
            }
        }

        // ── Setup Google Sign In ───────────────────────────
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("756738487382-iv3ab0uc2m0tti88v4892jv8tm3nifu5.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ── Login Email ────────────────────────────────────
        btnLogin.setOnClickListener {
            val emailStr    = etEmail.text.toString().trim()
            val passwordStr = etPassword.text.toString().trim()

            if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "Email dan Sandi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val blockMessage = LoginRateLimiter.check(this)
            if (blockMessage != null) {
                Toast.makeText(this, blockMessage, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnLogin.text      = "Memuat..."
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        SupabaseManager.client.auth.signInWith(Email) {
                            email    = emailStr
                            password = passwordStr
                        }
                    }
                    LoginRateLimiter.recordSuccess(this@LoginActivity)
                    Toast.makeText(this@LoginActivity, "Berhasil Masuk!", Toast.LENGTH_SHORT).show()

                    // ✅ Arahkan ke RoleLauncherActivity
                    startActivity(Intent(this@LoginActivity, RoleLauncherActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    LoginRateLimiter.recordFailure(this@LoginActivity)
                    val sisa = LoginRateLimiter.getRemainingAttempts(this@LoginActivity)
                    val pesanError = if (sisa > 0)
                        "Email atau Sandi salah! Sisa percobaan: $sisa"
                    else
                        "Akun diblokir sementara selama 15 menit."
                    Log.e("AUTH_ERROR", "Gagal Login: ${e.message}")
                    Toast.makeText(this@LoginActivity, pesanError, Toast.LENGTH_LONG).show()

                } finally {
                    btnLogin.text      = "Masuk"
                    btnLogin.isEnabled = true
                }
            }
        }

        // ── Login Google ───────────────────────────────────
        btnGoogleLogin.setOnClickListener {
            launcherLoginGoogle.launch(googleSignInClient.signInIntent)
        }

        // ── Lupa Password ──────────────────────────────────
        tvLupaPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    // ── Kirim reset password ─────────────────────────────
    private fun kirimResetPassword(email: String) {
        tvLupaPassword.text      = "Mengirim..."
        tvLupaPassword.isEnabled = false

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseManager.client.auth.resetPasswordForEmail(email)
                }

                AlertDialog.Builder(this@LoginActivity)
                    .setTitle("✅ Email Terkirim!")
                    .setMessage(
                        "Link reset password sudah dikirim ke:\n\n$email\n\n" +
                                "Silakan cek inbox atau folder spam kamu."
                    )
                    .setPositiveButton("OK", null)
                    .show()

            } catch (e: Exception) {
                Log.e("AUTH_ERROR", "Reset password gagal: ${e.message}")
                AlertDialog.Builder(this@LoginActivity)
                    .setTitle("Gagal Kirim Email")
                    .setMessage(
                        "Pastikan email $email sudah terdaftar di sistem.\n\n" +
                                "Hubungi admin jika masalah berlanjut."
                    )
                    .setPositiveButton("OK", null)
                    .show()
            } finally {
                tvLupaPassword.text      = "Lupa Password?"
                tvLupaPassword.isEnabled = true
            }
        }
    }

    // ── Google launcher ──────────────────────────────────
    private val launcherLoginGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            supabaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Gagal Google Sign In: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun supabaseAuthWithGoogle(idTokenString: String) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseManager.client.auth.signInWith(IDToken) {
                        idToken = idTokenString
                        provider = Google
                    }
                }
                Toast.makeText(this@LoginActivity, "Berhasil Masuk dengan Google!", Toast.LENGTH_SHORT).show()

                // ✅ Arahkan ke RoleLauncherActivity
                startActivity(Intent(this@LoginActivity, RoleLauncherActivity::class.java))
                finish()

            } catch (e: Exception) {
                Log.e("AUTH_ERROR", "Supabase Google Error: ${e.message}")
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
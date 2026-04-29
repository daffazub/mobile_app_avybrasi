package com.example.vybrasiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

// IMPORT SUPABASE V3
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    // Wadah untuk pop-up Google
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hubungkan dengan desain XML Anda
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)

        // 1. Cek apakah user sudah login sebelumnya (Auto-Login)
        lifecycleScope.launch {
            val session = withContext(Dispatchers.IO) {
                try {
                    SupabaseManager.client.auth.currentSessionOrNull()
                } catch (e: Exception) {
                    null
                }
            }
            if (session != null) {
                pindahKeDashboard()
            }
        }

        // 2. Siapkan pengaturan pop-up Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("68381799357-lipo17u2q05mmd0hlh2tiq9tel9qpp6k.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ==========================================
        // 3. AKSI TOMBOL MANUAL (EMAIL & PASSWORD)
        // ==========================================
        btnLogin.setOnClickListener {
            val emailStr = etEmail.text.toString().trim()
            val passwordStr = etPassword.text.toString().trim()

            if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "Email dan Sandi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.text = "Memuat..."
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                try {
                    // Tembak API Supabase untuk Login Email (Sintaks V3 dengan Tipe Eksplisit)
                    withContext(Dispatchers.IO) {
                        SupabaseManager.client.auth.signInWith(Email) {
                            email = emailStr
                            password = passwordStr
                        }
                    }
                    Toast.makeText(this@LoginActivity, "Berhasil Masuk!", Toast.LENGTH_SHORT).show()
                    pindahKeDashboard()
                } catch (e: Exception) {
                    Log.e("AUTH_ERROR", "Gagal Login: ${e.message}")
                    Toast.makeText(this@LoginActivity, "Email atau Sandi salah!", Toast.LENGTH_LONG).show()
                } finally {
                    btnLogin.text = "Masuk"
                    btnLogin.isEnabled = true
                }
            }
        }

        // ==========================================
        // 4. AKSI TOMBOL GOOGLE
        // ==========================================
        btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcherLoginGoogle.launch(signInIntent)
        }
    }

    // Penangkap hasil setelah user memilih email di pop-up Google
    private val launcherLoginGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            // Berhasil memilih akun, ambil token-nya
            val account = task.getResult(ApiException::class.java)!!
            supabaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Gagal memunculkan Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ==========================================
    // 5. FUNGSI SAMBUNGKAN TOKEN GOOGLE KE SUPABASE
    // ==========================================
    private fun supabaseAuthWithGoogle(idTokenString: String) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Jembatan: Menyerahkan token Google ke Supabase (Sintaks V3 dengan Tipe Eksplisit)
                    SupabaseManager.client.auth.signInWith(IDToken) {
                        idToken = idTokenString
                        provider = Google
                    }
                }
                // YAY! Berhasil Login.
                Toast.makeText(this@LoginActivity, "Berhasil Masuk dengan Google!", Toast.LENGTH_SHORT).show()
                pindahKeDashboard()
            } catch (e: Exception) {
                Log.e("AUTH_ERROR", "Supabase Google Error: ${e.message}")
                Toast.makeText(this@LoginActivity, "Otentikasi Gagal", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Fungsi otomatis pindah halaman
    private fun pindahKeDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

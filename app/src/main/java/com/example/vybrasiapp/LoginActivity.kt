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
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)

        lifecycleScope.launch {
            val session = withContext(Dispatchers.IO) {
                try { SupabaseManager.client.auth.currentSessionOrNull() } catch (e: Exception) { null }
            }
            if (session != null) pindahKeDashboard()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("68381799357-lipo17u2q05mmd0hlh2tiq9tel9qpp6k.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnLogin.setOnClickListener {
            val emailStr = etEmail.text.toString().trim()
            val passwordStr = etPassword.text.toString().trim()

            if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "Email dan Sandi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ CEK RATE LIMITER SEBELUM LOGIN
            val blockMessage = LoginRateLimiter.check(this)
            if (blockMessage != null) {
                Toast.makeText(this, blockMessage, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnLogin.text = "Memuat..."
            btnLogin.isEnabled = false

            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        SupabaseManager.client.auth.signInWith(Email) {
                            email = emailStr
                            password = passwordStr
                        }
                    }

                    // ✅ LOGIN BERHASIL - reset rate limiter
                    LoginRateLimiter.recordSuccess(this@LoginActivity)
                    Toast.makeText(this@LoginActivity, "Berhasil Masuk!", Toast.LENGTH_SHORT).show()
                    pindahKeDashboard()

                } catch (e: Exception) {
                    // ✅ LOGIN GAGAL - catat percobaan
                    LoginRateLimiter.recordFailure(this@LoginActivity)

                    val sisa = LoginRateLimiter.getRemainingAttempts(this@LoginActivity)
                    val pesanError = if (sisa > 0) {
                        "Email atau Sandi salah! Sisa percobaan: $sisa"
                    } else {
                        "Akun diblokir sementara selama 15 menit."
                    }

                    Log.e("AUTH_ERROR", "Gagal Login: ${e.message}")
                    Toast.makeText(this@LoginActivity, pesanError, Toast.LENGTH_LONG).show()

                } finally {
                    btnLogin.text = "Masuk"
                    btnLogin.isEnabled = true
                }
            }
        }

        btnGoogleLogin.setOnClickListener {
            launcherLoginGoogle.launch(googleSignInClient.signInIntent)
        }
    }

    private val launcherLoginGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            supabaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Gagal memunculkan Google: ${e.message}", Toast.LENGTH_SHORT).show()
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
                pindahKeDashboard()
            } catch (e: Exception) {
                Log.e("AUTH_ERROR", "Supabase Google Error: ${e.message}")
                Toast.makeText(this@LoginActivity, "Otentikasi Gagal", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pindahKeDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
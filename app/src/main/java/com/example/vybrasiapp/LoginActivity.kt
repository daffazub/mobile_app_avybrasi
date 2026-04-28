package com.example.vybrasiapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    // Siapkan wadah untuk Firebase dan Google
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)

        // 1. Panggil mesin Firebase
        auth = FirebaseAuth.getInstance()

        // Jika user sudah pernah login sebelumnya, langsung buka Dashboard!
        if (auth.currentUser != null) {
            pindahKeDashboard()
        }

        // 2. Siapkan pengaturan pop-up Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("68381799357-lipo17u2q05mmd0hlh2tiq9tel9qpp6k.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Aksi tombol login Email & Password
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginWithEmailPassword(email, password)
        }

        // Aksi tombol Google ditekan
        btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            launcherLoginGoogle.launch(signInIntent)
        }
    }

    private fun loginWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Berhasil Masuk!", Toast.LENGTH_SHORT).show()
                    pindahKeDashboard()
                } else {
                    // Jika login gagal karena user belum terdaftar, coba daftarkan
                    // Ini sesuai permintaan: "tambahkan fungsi daftarkan email lewar aplikasi"
                    registerAndLogin(email, password)
                }
            }
    }

    private fun registerAndLogin(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Akun berhasil dibuat dan otomatis masuk!", Toast.LENGTH_SHORT).show()
                    pindahKeDashboard()
                } else {
                    Toast.makeText(this, "Gagal Masuk: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
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
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Gagal memunculkan Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Sambungkan token Google tersebut ke Firebase kita
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // YAY! Berhasil Login.
                    Toast.makeText(this, "Berhasil Masuk!", Toast.LENGTH_SHORT).show()
                    pindahKeDashboard()
                } else {
                    Toast.makeText(this, "Otentikasi Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Fungsi otomatis pindah halaman
    private fun pindahKeDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Tutup halaman login agar tidak bisa di-back
    }
}

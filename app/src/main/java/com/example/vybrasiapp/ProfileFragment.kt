package com.example.vybrasiapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.switchmaterial.SwitchMaterial
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    companion object {
        private const val PREFS_NAME = "stok_prefs"
        private const val KEY_NOTIF_STOK = "notif_stok_enabled"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // ── Views ──────────────────────────────────────────
        val ivProfile      = view.findViewById<ImageView>(R.id.ivProfile)
        val tvProfileName  = view.findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout      = view.findViewById<Button>(R.id.btnLogout)
        val menuKatalog    = view.findViewById<LinearLayout>(R.id.menuKatalog)
        val switchNotif    = view.findViewById<SwitchMaterial>(R.id.switchNotif)

        // ── Load profil dari Supabase ──────────────────────
        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                try { SupabaseManager.client.auth.currentUserOrNull() } catch (e: Exception) { null }
            }
            if (user != null) {
                val nama = user.userMetadata?.get("full_name")?.toString()?.trim('"')
                    ?: user.userMetadata?.get("name")?.toString()?.trim('"')
                    ?: "Pengguna Vybrasi"
                val email = user.email ?: "Tidak ada email"
                val foto = user.userMetadata?.get("avatar_url")?.toString()?.trim('"')
                    ?: user.userMetadata?.get("picture")?.toString()?.trim('"')

                tvProfileName.text  = nama
                tvProfileEmail.text = email

                if (!foto.isNullOrEmpty()) {
                    Glide.with(this@ProfileFragment)
                        .load(foto)
                        .circleCrop()
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(ivProfile)
                }
            } else {
                tvProfileName.text  = "Memuat..."
                tvProfileEmail.text = "memuat..."
            }
        }

        // ── Switch Peringatan Stok Menipis ─────────────────
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Baca status tersimpan (default true)
        val isNotifEnabled = prefs.getBoolean(KEY_NOTIF_STOK, true)
        switchNotif.isChecked = isNotifEnabled

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            // Simpan pilihan
            prefs.edit().putBoolean(KEY_NOTIF_STOK, isChecked).apply()
            val msg = if (isChecked) "Notifikasi stok dinyalakan" else "Notifikasi stok dimatikan"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        // ── Klik Menu Lihat Katalog ────────────────────────
        menuKatalog.setOnClickListener {
            val intent = Intent(requireContext(), KatalogActivity::class.java)
            startActivity(intent)
        }

        // ── Logout ─────────────────────────────────────────
        btnLogout.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    try { SupabaseManager.client.auth.signOut() } catch (e: Exception) { }
                }
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        return view
    }
}
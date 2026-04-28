package com.example.vybrasiapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // 1. Inisialisasi Tampilan Profil
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // 2. Inisialisasi Menu-Menu Pusat Kendali (Command Center)
        val menuPengeluaran = view.findViewById<MaterialCardView>(R.id.menuPengeluaran)
        val menuKatalog = view.findViewById<TextView>(R.id.menuKatalog)
        val menuSupplier = view.findViewById<TextView>(R.id.menuSupplier)
        val menuLogAdmin = view.findViewById<TextView>(R.id.menuLogAdmin)

        // 3. Tampilkan Data Google
        if (currentUser != null) {
            tvProfileName.text = currentUser.displayName ?: "Owner Spakung Coffee"
            tvProfileEmail.text = currentUser.email ?: "Tidak ada email"

            val photoUrl = currentUser.photoUrl
            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).circleCrop().into(ivProfile)
            }
        }

        // --- AKSI KLIK MENU PUSAT KENDALI ---

        menuPengeluaran.setOnClickListener {
            // HAPUS TOAST-NYA, GANTI DENGAN INI:
            val intent = Intent(requireContext(), PengeluaranActivity::class.java)
            startActivity(intent)
        }

        menuKatalog.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka: Sinkronisasi Harga Web...", Toast.LENGTH_SHORT).show()
        }

        menuSupplier.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka: Database Akun Supplier...", Toast.LENGTH_SHORT).show()
        }

        menuLogAdmin.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka: Riwayat Aktivitas Admin Web...", Toast.LENGTH_SHORT).show()
        }

        menuKatalog.setOnClickListener {
            val intent = Intent(requireContext(), KatalogActivity::class.java)
            startActivity(intent)
        }

        menuPengeluaran.setOnClickListener {
            val intent = Intent(requireContext(), PengeluaranActivity::class.java)
            startActivity(intent)
        }

        menuKatalog.setOnClickListener {
            val intent = Intent(requireContext(), KatalogActivity::class.java)
            startActivity(intent)
        }

        menuSupplier.setOnClickListener {
            val intent = Intent(requireContext(), SupplierActivity::class.java)
            startActivity(intent)
        }

        menuLogAdmin.setOnClickListener {
            val intent = Intent(requireContext(), LogAdminActivity::class.java)
            startActivity(intent)
        }


        // --- AKSI LOGOUT ---
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}
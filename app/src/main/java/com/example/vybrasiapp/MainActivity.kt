package com.example.vybrasiapp

import android.os.Bundle
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. === LOGIKA MENU BAWAH (BOTTOM NAVIGATION) ===
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_report -> {
                    replaceFragment(ReportFragment())
                    true
                }
                R.id.nav_monitor -> {
                    replaceFragment(MonitorFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // 2. === LOGIKA PILIH CABANG (POPUP MENU) ===
        val tvPilihCabang = findViewById<TextView>(R.id.tvPilihCabang)

        tvPilihCabang.setOnClickListener { view ->
            val popup = PopupMenu(this, view)

            // Mengubah daftar cabang sesuai permintaan Anda
            popup.menu.add("📍 Pusat Telemoyo")
            popup.menu.add("📍 Cabang 2 (Segera Datang!)")

            popup.setOnMenuItemClickListener { item ->
                // Jika bos klik "Pusat", ganti teksnya.
                // Tapi kalau klik "Segera Datang", biarkan saja teks tetap di Pusat.
                if (item.title == "📍 Pusat Spakung Coffee") {
                    tvPilihCabang.text = "📍 Pusat ▾"
                    Toast.makeText(this, "Tetap di Pusat", Toast.LENGTH_SHORT).show()
                } else {
                    // Beri tahu Bos kalau cabang ini belum buka
                    Toast.makeText(this, "Cabang ini masih dibangun! ☕", Toast.LENGTH_SHORT).show()
                }
                true
            }

            popup.show()
        }
    }

    // Fungsi ajaib untuk mengganti fragment di dalam wadah
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
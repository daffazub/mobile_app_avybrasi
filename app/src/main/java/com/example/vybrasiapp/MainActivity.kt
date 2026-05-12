package com.example.vybrasiapp

import android.os.Bundle
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Status bar hitam, icon putih
        window.statusBarColor = android.graphics.Color.parseColor("#1A1A1A")
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContentView(R.layout.activity_main)

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home    -> { replaceFragment(HomeFragment()); true }
                R.id.nav_report  -> { replaceFragment(ReportFragment()); true }
                R.id.nav_monitor -> { replaceFragment(MonitorFragment()); true }
                R.id.nav_profile -> { replaceFragment(ProfileFragment()); true }
                else -> false
            }
        }

        // Pilih Cabang
        val tvPilihCabang = findViewById<TextView>(R.id.tvPilihCabang)
        tvPilihCabang.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("📍 Pusat Telemoyo")
            popup.menu.add("📍 Cabang 2 (Segera Datang!)")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "📍 Pusat Telemoyo") {
                    tvPilihCabang.text = "📍 Pusat ▾"
                    Toast.makeText(this, "Tetap di Pusat", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Cabang ini masih dibangun! ☕", Toast.LENGTH_SHORT).show()
                }
                true
            }
            popup.show()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
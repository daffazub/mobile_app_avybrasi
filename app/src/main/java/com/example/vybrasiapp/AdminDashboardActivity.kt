package com.example.vybrasiapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.parseColor("#1A1A1A")
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContentView(R.layout.activity_admin_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.adminBottomNavigation)
        val tvInfo    = findViewById<TextView>(R.id.tvAdminInfo)

        if (savedInstanceState == null) {
            replaceFragment(AdminDashboardFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.admin_nav_dashboard  -> { replaceFragment(AdminDashboardFragment()); true }
                R.id.admin_nav_laporan    -> { replaceFragment(AdminLaporanFragment()); true }
                R.id.admin_nav_monitoring -> { replaceFragment(AdminMonitoringFragment()); true }
                R.id.admin_nav_akun       -> { replaceFragment(AdminAkunFragment()); true }
                R.id.admin_nav_monitoring -> { replaceFragment(AdminMonitoringFragment()); true }
                else -> false
            }
        }

        lifecycleScope.launch {
            val nama = withContext(Dispatchers.IO) { SupabaseManager.getCurrentUserName() } ?: "Admin"
            tvInfo.text = nama
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
    }
}
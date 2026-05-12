package com.example.vybrasiapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AffiliateDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.parseColor("#1A1A1A")
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setContentView(R.layout.activity_affiliate_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.affBottomNavigation)

        if (savedInstanceState == null) {
            replaceFragment(AffHomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.aff_nav_home     -> { replaceFragment(AffHomeFragment()); true }
                R.id.aff_nav_transaksi -> { replaceFragment(AffTransaksiFragment()); true }
                R.id.aff_nav_komisi   -> { replaceFragment(AffKomisiFragment()); true }
                R.id.aff_nav_profil   -> { replaceFragment(AffProfilFragment()); true }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.aff_fragment_container, fragment)
            .commit()
    }
}
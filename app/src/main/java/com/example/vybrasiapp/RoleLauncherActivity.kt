package com.example.vybrasiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RoleLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                try {
                    SupabaseManager.client.auth.currentUserOrNull()
                } catch (e: Exception) { null }
            }

            if (user == null) {
                startActivity(Intent(this@RoleLauncherActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                })
                finish()
                return@launch
            }

            val role = withContext(Dispatchers.IO) {
                try { SupabaseManager.getCurrentUserRole() }
                catch (e: Exception) { Log.e("ROLE", e.message ?: ""); null }
            }

            val email = withContext(Dispatchers.IO) { SupabaseManager.getCurrentUserEmail() } ?: "-"
            val dest = when (role) {
                "owner" -> MainActivity::class.java
                "admin" -> AdminDashboardActivity::class.java
                "affiliate" -> AffiliateDashboardActivity::class.java
                else -> MainActivity::class.java
            }
            startActivity(Intent(this@RoleLauncherActivity, dest))
            finish()
        }
    }
}
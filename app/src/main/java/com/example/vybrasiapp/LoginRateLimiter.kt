package com.example.vybrasiapp

import android.content.Context
import android.content.SharedPreferences

object LoginRateLimiter {

    private const val PREFS_NAME = "login_rate_limiter"
    private const val KEY_ATTEMPT_COUNT = "attempt_count"
    private const val KEY_FIRST_ATTEMPT_TIME = "first_attempt_time"
    private const val KEY_LOCKOUT_TIME = "lockout_time"

    private const val MAX_ATTEMPTS = 5          // Maksimal percobaan
    private const val WINDOW_MS = 5 * 60 * 1000L    // Jendela waktu: 5 menit
    private const val LOCKOUT_MS = 15 * 60 * 1000L  // Durasi lockout: 15 menit

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Cek apakah login boleh dilanjutkan.
     * Return null = boleh lanjut
     * Return String = pesan error, login diblokir
     */
    fun check(context: Context): String? {
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()

        val lockoutTime = prefs.getLong(KEY_LOCKOUT_TIME, 0L)

        // Cek apakah sedang dalam masa lockout
        if (lockoutTime > 0 && now < lockoutTime) {
            val sisaDetik = (lockoutTime - now) / 1000
            val menitSisa = sisaDetik / 60
            val detikSisa = sisaDetik % 60
            return "Terlalu banyak percobaan. Coba lagi dalam ${menitSisa}m ${detikSisa}d."
        }

        // Reset jika lockout sudah lewat
        if (lockoutTime > 0 && now >= lockoutTime) {
            reset(context)
        }

        val attemptCount = prefs.getInt(KEY_ATTEMPT_COUNT, 0)
        val firstAttemptTime = prefs.getLong(KEY_FIRST_ATTEMPT_TIME, 0L)

        // Reset jika jendela waktu sudah lewat
        if (firstAttemptTime > 0 && now - firstAttemptTime > WINDOW_MS) {
            reset(context)
        }

        return null // Boleh lanjut
    }

    /**
     * Dipanggil ketika login GAGAL
     */
    fun recordFailure(context: Context) {
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()
        val editor = prefs.edit()

        val attemptCount = prefs.getInt(KEY_ATTEMPT_COUNT, 0)
        val firstAttemptTime = prefs.getLong(KEY_FIRST_ATTEMPT_TIME, 0L)

        // Set waktu percobaan pertama
        if (firstAttemptTime == 0L) {
            editor.putLong(KEY_FIRST_ATTEMPT_TIME, now)
        }

        val newCount = attemptCount + 1
        editor.putInt(KEY_ATTEMPT_COUNT, newCount)

        // Jika sudah mencapai batas, aktifkan lockout
        if (newCount >= MAX_ATTEMPTS) {
            editor.putLong(KEY_LOCKOUT_TIME, now + LOCKOUT_MS)
        }

        editor.apply()
    }

    /**
     * Dipanggil ketika login BERHASIL
     */
    fun recordSuccess(context: Context) {
        reset(context)
    }

    /**
     * Reset semua data
     */
    fun reset(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    /**
     * Info sisa percobaan (untuk ditampilkan ke user)
     */
    fun getRemainingAttempts(context: Context): Int {
        val prefs = getPrefs(context)
        val attemptCount = prefs.getInt(KEY_ATTEMPT_COUNT, 0)
        return maxOf(0, MAX_ATTEMPTS - attemptCount)
    }
}
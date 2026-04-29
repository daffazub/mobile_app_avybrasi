// =============================================
// FILE: SupabaseManager.kt
// PATH: app/src/main/java/com/example/vybrasiapp/SupabaseManager.kt
// VERSI: Supabase SDK 3.5.0 + Ktor 3.0.1
// =============================================

package com.example.vybrasiapp

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object SupabaseManager {

    // =============================================
    // KONFIGURASI SUPABASE
    // GANTI dengan URL & Key project Anda
    // =============================================
    private const val SUPABASE_URL = "https://YOUR-PROJECT-ID.supabase.co"
    private const val SUPABASE_KEY = "YOUR_ANON_KEY"

    // Supabase Client (Lazy initialization)
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            // Install Postgrest untuk akses database
            install(Postgrest) {
                // Set default schema ke jualan_kopi
                defaultSchema = "jualan_kopi"
            }

            // Install Auth module
            install(Auth) {
                // Auto load session dari penyimpanan lokal
                autoLoadFromStorage = true
            }

            // Install Storage (opsional, untuk upload gambar)
            install(Storage)
        }
    }

    // =============================================
    // DATA CLASSES (HARUS @Serializable untuk Supabase)
    // =============================================

    @Serializable
    data class AffiliateDashboard(
        val id_affiliate: String,
        val profile_id: String,
        val email: String = "",
        val full_name: String,
        val kode_referal: String,
        val komisi_persen: Double,
        val total_komisi: Double,
        val minimum_payout: Double,
        val status_affiliate: String,
        val total_transaksi_referal: Long = 0,
        val komisi_pending: Double = 0.0,
        val komisi_terkonfirmasi: Double = 0.0,
        val bisa_withdraw: Boolean = false,
        val payment_method: Map<String, String>? = null,
        val bergabung_sejak: String? = null
    )

    @Serializable
    data class UserProfile(
        val id: String,
        val user_id: String,
        val username: String,
        val full_name: String,
        val role: String,
        val can_shop: Boolean = true,
        val kode_unik: String? = null
    )

    @Serializable
    data class TransaksiAffiliate(
        val id_transaksi: String,
        val no_invoice: String,
        val total_harga: Double,
        val komisi_affiliate: Double,
        val status: String,
        val created_at: String
    )

    @Serializable
    data class ProdukItem(
        val id_produk: String,
        val nama: String,
        val harga: Double,
        val gambar_utama: String? = null
    )

    // =============================================
    // FUNGSI AUTH (KOMPATIBEL SDK 3.x)
    // =============================================

    /**
     * Cek apakah user sudah login
     */
    suspend fun isLoggedIn(): Boolean {
        return try {
            client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            Log.e("AUTH", "Error check login: ${e.message}")
            false
        }
    }

    /**
     * Ambil user ID saat ini
     */
    suspend fun getCurrentUserId(): String? {
        return try {
            client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Log.e("AUTH", "Error get user: ${e.message}")
            null
        }
    }

    /**
     * Ambil session user saat ini
     */
    suspend fun getCurrentSession() = client.auth.currentSessionOrNull()

    /**
     * Logout user
     */
    suspend fun logout() {
        try {
            client.auth.signOut()
            Log.d("AUTH", "Logout berhasil")
        } catch (e: Exception) {
            Log.e("AUTH", "Error logout: ${e.message}")
        }
    }

    // =============================================
    // FUNGSI PROFILE
    // =============================================

    /**
     * Ambil profil user dari tabel profiles
     */
    suspend fun getUserProfile(): UserProfile? {
        return try {
            val userId = getCurrentUserId() ?: return null

            val response = client.from("profiles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<UserProfile>()

            response
        } catch (e: Exception) {
            Log.e("PROFILE", "Error get profile: ${e.message}")
            null
        }
    }

    /**
     * Cek role user
     */
    suspend fun getUserRole(): String? {
        return getUserProfile()?.role
    }

    /**
     * Cek apakah user adalah affiliate
     */
    suspend fun isAffiliate(): Boolean {
        val role = getUserRole()
        return role == "affiliate" || role == "admin"
    }

    // =============================================
    // FUNGSI AFFILIATE DASHBOARD
    // =============================================

    /**
     * Ambil data dashboard affiliate dari view
     */
    suspend fun getAffiliateDashboard(): AffiliateDashboard? {
        return try {
            val profile = getUserProfile() ?: return null

            // Cek role
            if (profile.role != "affiliate" && profile.role != "admin") {
                Log.d("AFFILIATE", "User bukan affiliate, role: ${profile.role}")
                return null
            }

            // Ambil dari view v_affiliate_mobile_dashboard
            val result = client.from("v_affiliate_mobile_dashboard")
                .select {
                    filter {
                        eq("profile_id", profile.id)
                    }
                }
                .decodeSingle<AffiliateDashboard>()

            Log.d("AFFILIATE", "Dashboard loaded: ${result.full_name}")
            result
        } catch (e: Exception) {
            Log.e("AFFILIATE", "Error get dashboard: ${e.message}")
            null
        }
    }

    /**
     * Ambil riwayat transaksi dengan kode referral affiliate
     */
    suspend fun getTransaksiAffiliate(limit: Int = 20): List<TransaksiAffiliate> {
        return try {
            val dashboard = getAffiliateDashboard() ?: return emptyList()

            client.from("transaksi")
                .select {
                    filter {
                        eq("id_affiliate", dashboard.id_affiliate)
                    }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(limit)
                }
                .decodeList<TransaksiAffiliate>()
        } catch (e: Exception) {
            Log.e("TRANSAKSI", "Error get transaksi: ${e.message}")
            emptyList()
        }
    }

    /**
     * Ambil jumlah total transaksi affiliate
     */
    suspend fun getTotalTransaksiAffiliate(): Long {
        return try {
            val dashboard = getAffiliateDashboard() ?: return 0
            dashboard.total_transaksi_referal
        } catch (e: Exception) {
            0
        }
    }

    // =============================================
    // FUNGSI WITHDRAW / PENCAIRAN
    // =============================================

    /**
     * Ajukan pencairan komisi
     */
    suspend fun requestWithdraw(jumlah: Double): Boolean {
        return try {
            val dashboard = getAffiliateDashboard() ?: return false

            // Validasi
            if (!dashboard.bisa_withdraw) {
                Log.e("WITHDRAW", "Belum memenuhi syarat withdraw")
                return false
            }

            if (jumlah < dashboard.minimum_payout) {
                Log.e("WITHDRAW", "Jumlah kurang dari minimum payout: ${dashboard.minimum_payout}")
                return false
            }

            if (jumlah > dashboard.komisi_terkonfirmasi) {
                Log.e("WITHDRAW", "Jumlah melebihi komisi tersedia: ${dashboard.komisi_terkonfirmasi}")
                return false
            }

            // Insert ke tabel keuangan
            client.from("keuangan")
                .insert(
                    mapOf(
                        "id_affiliate" to dashboard.id_affiliate,
                        "tipe" to "komisi",
                        "jumlah" to jumlah,
                        "status" to "pending",
                        "keterangan" to "Pengajuan pencairan komisi via mobile app"
                    )
                )

            Log.d("WITHDRAW", "Pengajuan withdraw berhasil: Rp $jumlah")
            true
        } catch (e: Exception) {
            Log.e("WITHDRAW", "Error request withdraw: ${e.message}")
            false
        }
    }

    /**
     * Cek status withdraw terakhir
     */
    suspend fun getLastWithdrawStatus(): String? {
        return try {
            val dashboard = getAffiliateDashboard() ?: return null

            val result = client.from("keuangan")
                .select {
                    filter {
                        eq("id_affiliate", dashboard.id_affiliate)
                        eq("tipe", "komisi")
                    }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(1)
                }
                .decodeSingle<Map<String, String>>()

            result["status"]
        } catch (e: Exception) {
            null
        }
    }

    // =============================================
    // FUNGSI PRODUK (Jika diperlukan)
    // =============================================

    /**
     * Ambil daftar produk
     */
    suspend fun getProdukList(limit: Int = 10): List<ProdukItem> {
        return try {
            client.from("produk")
                .select {
                    filter {
                        eq("status_produk", "active")
                    }
                    limit(limit)
                }
                .decodeList<ProdukItem>()
        } catch (e: Exception) {
            Log.e("PRODUK", "Error get produk: ${e.message}")
            emptyList()
        }
    }

    // =============================================
    // FUNGSI UTILITY
    // =============================================

    /**
     * Format rupiah
     */
    fun formatRupiah(amount: Double): String {
        val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
        return formatter.format(amount)
    }

    /**
     * Format tanggal
     */
    fun formatDate(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale("id", "ID"))
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }
}
package com.example.vybrasiapp

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.Serializable

object SupabaseManager {

    private const val SUPABASE_URL = "https://dtjvahuoxwtagdwziibh.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR0anZhaHVveHd0YWdkd3ppaWJoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY3NDE1ODcsImV4cCI6MjA5MjMxNzU4N30.a9g_HuWbydLp-N9OAK5bYhPrTXOsxEPDtvEuEfDnJCU"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest) {
                defaultSchema = "jualan_kopi"
            }
            install(Storage)
        }
    }

    @Serializable
    data class UserProfile(val id: String, val user_id: String, val username: String? = null, val full_name: String? = null, val role: String? = null, val can_shop: Boolean? = true, val kode_unik: String? = null)

    @Serializable
    data class AffiliateProfile(val id_affiliate: String, val profile_id: String? = null, val nama_lengkap: String? = null, val kode_referal: String? = null, val komisi_persen: Double? = 0.0, val total_komisi: Double? = 0.0, val minimum_payout: Double? = 0.0, val status_affiliate: String? = null)

    @Serializable
    data class TransaksiAffiliate(val id_transaksi: String, val no_invoice: String? = null, val total_harga: Double? = 0.0, val komisi_affiliate: Double? = 0.0, val status: String? = null, val created_at: String? = null)

    @Serializable
    data class ProdukItem(val id_produk: String, val nama: String? = null, val harga: Double? = 0.0, val gambar_utama: String? = null, val status_produk: String? = null)

    suspend fun isLoggedIn(): Boolean {
        return try {
            client.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentUserId(): String? {
        return try {
            client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun logout() {
        try {
            client.auth.signOut()
        } catch (e: Exception) {
            Log.e("AUTH", "Error logout: ${e.message}")
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        return try {
            val userId = getCurrentUserId() ?: return null
            client.from("profiles")
                .select { filter { eq("user_id", userId) } }
                .decodeSingle<UserProfile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAffiliateProfile(): AffiliateProfile? {
        return try {
            val profile = getUserProfile() ?: return null
            client.from("affiliate_profiles")
                .select { filter { eq("profile_id", profile.id) } }
                .decodeSingle<AffiliateProfile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTransaksiAffiliate(limit: Int = 20): List<TransaksiAffiliate> {
        return try {
            val affiliate = getAffiliateProfile() ?: return emptyList()
            client.from("transaksi")
                .select {
                    filter { eq("id_affiliate", affiliate.id_affiliate) }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<TransaksiAffiliate>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun requestWithdraw(jumlah: Double): Boolean {
        return try {
            val affiliate = getAffiliateProfile() ?: return false
            if (jumlah < (affiliate.minimum_payout ?: 0.0) || jumlah > (affiliate.total_komisi ?: 0.0)) return false
            client.from("keuangan").insert(
                mapOf("id_affiliate" to affiliate.id_affiliate, "tipe" to "komisi", "jumlah" to jumlah, "status" to "pending")
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLastWithdrawStatus(): String? {
        return try {
            val affiliate = getAffiliateProfile() ?: return null
            val result = client.from("keuangan")
                .select {
                    filter { eq("id_affiliate", affiliate.id_affiliate); eq("tipe", "komisi") }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(1L)
                }
                .decodeSingle<Map<String, String>>()
            result["status"]
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getProdukList(limit: Int = 10): List<ProdukItem> {
        return try {
            client.from("produk")
                .select { filter { eq("status_produk", "active") }; limit(limit.toLong()) }
                .decodeList<ProdukItem>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun formatRupiah(amount: Double): String {
        val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
        return formatter.format(amount)
    }
}
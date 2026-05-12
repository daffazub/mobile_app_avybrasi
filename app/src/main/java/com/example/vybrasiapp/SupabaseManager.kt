package com.example.vybrasiapp

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object SupabaseManager {

    internal const val SUPABASE_URL = "https://jrgoxsxvccbcowqqrgxl.supabase.co"
    internal const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpyZ294c3h2Y2NiY293cXFyZ3hsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgxMTEyODIsImV4cCI6MjA5MzY4NzI4Mn0.4ZUI4YKsTSOZBGAbb25bQl1n4AX0U_f5GqW1JjoWw6s"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest) {
                defaultSchema = "jualan_kopi"   // ← semua query otomatis ke schema ini
            }
            install(Storage)
        }
    }

    // ── Data Classes ─────────────────────
    @Serializable
    data class Profile(
        val id: String,
        val user_id: String,
        val username: String? = null,
        val full_name: String? = null,
        val role: String? = null,
        val can_shop: Boolean? = true,
        val kode_unik: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val avatar_url: String? = null
    )

    @Serializable
    data class AdminProfile(
        val id_admin: String, val profile_id: String,
        val level_admin: String? = null, val department: String? = null
    )

    @Serializable
    data class Affiliate(
        val id_affiliate: String,
        val profile_id: String? = null,
        val nama_lengkap: String? = null,
        val kode_referal: String? = null,
        val komisi_persen: Double? = 0.0,
        val total_komisi: Double? = 0.0,
        val minimum_payout: Double? = 0.0,
        val status_affiliate: String? = null,
        val payment_method: String? = null,
        val user_id: String? = null
    )

    @Serializable
    data class TransaksiAffiliate(
        val id_transaksi: String, val no_invoice: String? = null,
        val total_harga: Double? = 0.0, val komisi_affiliate: Double? = 0.0,
        val status: String? = null, val created_at: String? = null
    )

    @Serializable
    data class ProdukItem(
        val id_produk: String, val nama: String? = null,
        val harga: Double? = 0.0, val gambar_utama: String? = null,
        val status_produk: String? = null
    )

    @Serializable
    data class PayoutRequest(
        val id_request: String, val id_affiliate: String,
        val jumlah: Double, val status: String? = "pending",
        val keterangan_admin: String? = null, val created_at: String? = null,
        val updated_at: String? = null, val approved_by: String? = null
    )

    @Serializable
    data class Keuangan(
        val id_keuangan: String, val id_affiliate: String,
        val tipe: String? = null, val jumlah: Double? = 0.0,
        val saldo_sebelum: Double? = 0.0, val saldo_sesudah: Double? = 0.0,
        val keterangan: String? = null
    )

    // ── Auth ─────────────────────────────
    suspend fun getCurrentUserEmail(): String? {
        return try {
            client.auth.currentUserOrNull()?.let { user ->
                (user::class.java.getMethod("getEmail").invoke(user) as? String)
                    ?: (user::class.java.getMethod("getUserMetadata").invoke(user)?.let { meta ->
                        meta::class.java.getMethod("get", String::class.java).invoke(meta, "email") as? String
                    })
            }
        } catch (e: Exception) { null }
    }

    suspend fun getCurrentUserName(): String? {
        return try {
            client.auth.currentUserOrNull()?.let { user ->
                (user::class.java.getMethod("getUserMetadata").invoke(user)?.let { meta ->
                    (meta::class.java.getMethod("get", String::class.java).invoke(meta, "full_name") as? String)
                        ?: (meta::class.java.getMethod("get", String::class.java).invoke(meta, "name") as? String)
                })
            }
        } catch (e: Exception) { null }
    }

    suspend fun isLoggedIn(): Boolean =
        try { client.auth.currentSessionOrNull() != null } catch (e: Exception) { false }

    suspend fun getCurrentUserId(): String? {
        return try {
            val user = client.auth.currentUserOrNull()
            user?.let { it::class.java.getMethod("getId").invoke(it) as? String }
        } catch (e: Exception) { null }
    }

    suspend fun logout() { try { client.auth.signOut() } catch (_: Exception) {} }

    suspend fun getUserProfile(): Profile? {
        return try {
            val userId = getCurrentUserId() ?: return null
            client.from("profiles")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull()
        } catch (e: Exception) { null }
    }

    suspend fun getCurrentUserRole(): String? {
        return try {
            val userId = getCurrentUserId() ?: return null
            val affiliate = client.from("affiliate_profiles")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<Affiliate>()
            if (affiliate != null && affiliate.status_affiliate == "active") return "affiliate"

            val profile = client.from("profiles")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<Profile>()
            if (profile != null) {
                val admin = client.from("admin_profiles")
                    .select { filter { eq("profile_id", profile.id) } }
                    .decodeSingleOrNull<AdminProfile>()
                if (admin != null) return when (admin.level_admin) { "owner" -> "owner"; else -> "admin" }
            }
            "user"
        } catch (e: Exception) { null }
    }

    suspend fun getAffiliateProfile(): Affiliate? {
        return try {
            val userId = getCurrentUserId() ?: return null
            client.from("affiliate_profiles")
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull()
        } catch (e: Exception) { null }
    }

    // ── REST API untuk operasi khusus ──
    suspend fun createUserWithEmail(email: String, password: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("${SUPABASE_URL}/auth/v1/signup")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("apikey", SUPABASE_KEY)
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                val json = JSONObject().apply { put("email", email); put("password", password) }
                conn.outputStream.write(json.toString().toByteArray())
                val code = conn.responseCode
                if (code in 200..299) {
                    val resp = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    JSONObject(resp).optJSONObject("user")?.optString("id")
                } else {
                    val err = conn.errorStream?.bufferedReader()?.readText() ?: "no details"
                    Log.e("SIGNUP", "Error $code: $err")
                    conn.disconnect()
                    null
                }
            } catch (e: Exception) {
                Log.e("SIGNUP", "Exception: ${e.message}")
                null
            }
        }

    suspend fun insertAffiliate(affiliateData: Map<String, Any?>): String {
        // Bisa gunakan SDK langsung, tapi REST sudah bekerja, jadi biarkan REST saja.
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${SUPABASE_URL}/rest/v1/affiliate_profiles?schema=jualan_kopi")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("apikey", SUPABASE_KEY)
                connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Prefer", "return=minimal")
                connection.doOutput = true

                val json = JSONObject(affiliateData)
                connection.outputStream.write(json.toString().toByteArray())

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    connection.disconnect()
                    "OK"
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Tidak ada detail error"
                    connection.disconnect()
                    Log.e("INSERT_AFFILIATE", "Error $responseCode: $errorBody")
                    "Error $responseCode: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("INSERT_AFFILIATE", "Exception: ${e.message}", e)
                "Exception: ${e.message}"
            }
        }
    }

    suspend fun insertPayoutRequest(request: Map<String, Any?>): String =
        withContext(Dispatchers.IO) {
            try {
                // Gunakan REST karena belum ada fungsi SDK insert khusus
                val url = URL("${SUPABASE_URL}/rest/v1/payout_requests?schema=jualan_kopi")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("apikey", SUPABASE_KEY)
                conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Prefer", "return=minimal")
                conn.doOutput = true
                val json = JSONObject(request)
                conn.outputStream.write(json.toString().toByteArray())
                val code = conn.responseCode
                if (code in 200..299) "OK"
                else {
                    val err = conn.errorStream?.bufferedReader()?.readText() ?: "no details"
                    Log.e("INSERT_PAYOUT", "Error $code: $err")
                    "Error $code: $err"
                }
            } catch (e: Exception) {
                Log.e("INSERT_PAYOUT", "Exception: ${e.message}")
                "Gagal: ${e.message}"
            }
        }

    // ✅ SUDAH DIPERBAIKI: gunakan SDK (otomatis schema sudah true)
    suspend fun getPayoutRequests(): List<PayoutRequest> {
        return try {
            client.from("payout_requests")
                .select {
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<PayoutRequest>()
        } catch (e: Exception) {
            Log.e("GET_PAYOUT", "Error: ${e.message}")
            emptyList()
        }
    }

    suspend fun updatePayoutStatus(idRequest: String, newStatus: String): String =
        withContext(Dispatchers.IO) {
            try {
                // REST tetap dipakai untuk update
                val url = URL("${SUPABASE_URL}/rest/v1/payout_requests?id_request=eq.$idRequest&schema=jualan_kopi")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "PATCH"
                conn.setRequestProperty("apikey", SUPABASE_KEY)
                conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Prefer", "return=minimal")
                conn.doOutput = true
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
                val now = sdf.format(Date())
                val json = JSONObject().apply {
                    put("status", newStatus)
                    put("updated_at", now)
                }
                conn.outputStream.write(json.toString().toByteArray())
                val code = conn.responseCode
                if (code in 200..299) "OK"
                else {
                    val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "Tidak ada detail"
                    Log.e("UPDATE_PAYOUT", "Error $code: $errorBody")
                    "Error $code: $errorBody"
                }
            } catch (e: Exception) {
                Log.e("UPDATE_PAYOUT", "Exception: ${e.message}", e)
                "Gagal: ${e.message}"
            }
        }

    suspend fun insertKeuangan(data: Map<String, Any?>): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${SUPABASE_URL}/rest/v1/keuangan?schema=jualan_kopi")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("apikey", SUPABASE_KEY)
                conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Prefer", "return=minimal")
                conn.doOutput = true
                val json = JSONObject(data)
                conn.outputStream.write(json.toString().toByteArray())
                val code = conn.responseCode
                if (code in 200..299) "OK"
                else {
                    val err = conn.errorStream?.bufferedReader()?.readText() ?: "no details"
                    Log.e("INSERT_KEUANGAN", "Error $code: $err")
                    "Error $code: $err"
                }
            } catch (e: Exception) {
                Log.e("INSERT_KEUANGAN", "Exception: ${e.message}")
                "Gagal: ${e.message}"
            }
        }
    }

    suspend fun updateAffiliatePayment(userId: String, paymentJson: String): Boolean {
        return try {
            client.from("affiliate_profiles")
                .update(mapOf("payment_method" to paymentJson)) {
                    filter { eq("user_id", userId) }
                }
            true
        } catch (e: Exception) {
            Log.e("UPDATE_PAYMENT", "Gagal update payment: ${e.message}")
            false
        }
    }

    // ✅ SUDAH DIPERBAIKI: gunakan SDK
    suspend fun getAffiliateById(id: String): Affiliate? {
        return try {
            client.from("affiliate_profiles")
                .select { filter { eq("id_affiliate", id) } }
                .decodeSingleOrNull<Affiliate>()
        } catch (e: Exception) {
            Log.e("Supabase", "getAffiliateById error: ${e.message}")
            null
        }
    }

    fun formatRupiah(amount: Double): String {
        val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
        return formatter.format(amount)
    }
}
package com.example.vybrasiapp

import android.annotation.SuppressLint
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import com.example.vybrasiapp.model.KeuanganAffiliate


object AffiliateRepository {

    // Ambil semua affiliates
    suspend fun getAllAffiliates(): List<Affiliate> {
        return SupabaseManager.client.postgrest
            .from("affiliate_profiles")
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Affiliate>()
    }

    // Ambil detail 1 affiliate by ID
    suspend fun getAffiliateById(idAffiliate: String): Affiliate? {
        return SupabaseManager.client.postgrest
            .from("affiliate_profiles")
            .select {
                filter { eq("id_affiliate", idAffiliate) }
                limit(1)
            }
            .decodeSingleOrNull<Affiliate>()
    }

    // Ambil total transaksi dari kode referal affiliate
    suspend fun getKomisiAffiliate(idAffiliate: String): List<KeuanganAffiliate> {
        return SupabaseManager.client.postgrest
            .from("keuangan")
            .select {
                filter { eq("id_affiliate", idAffiliate) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<KeuanganAffiliate>()
    }
}

// 3. DATA CLASS AFFILIATE (Sudah dilengkapi variabelnya)
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Affiliate(
    @SerialName("id_affiliate") val idAffiliate: String,
    @SerialName("nama_lengkap") val namaLengkap: String? = null,
    @SerialName("kode_referal") val kodeReferal: String? = null,
    @SerialName("komisi_persen") val komisiPersen: Double? = 0.0,       // DITAMBAHKAN
    @SerialName("total_komisi") val totalKomisi: Double? = 0.0,
    @SerialName("minimum_payout") val minimumPayout: Double? = 0.0,     // DITAMBAHKAN
    @SerialName("payment_method") val paymentMethod: String? = null,    // DITAMBAHKAN
    @SerialName("status_affiliate") val statusAffiliate: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
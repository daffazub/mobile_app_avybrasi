package com.example.vybrasiapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class AffiliateListActivity : AppCompatActivity() {

    private lateinit var rvAffiliate: RecyclerView
    private lateinit var adapter: AffiliateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_affiliate_list)

        rvAffiliate = findViewById(R.id.rvAffiliate)
        rvAffiliate.layoutManager = LinearLayoutManager(this)

        adapter = AffiliateAdapter(emptyList()) { /* klik untuk detail */ }
        rvAffiliate.adapter = adapter

        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                try {
                    val url = URL("${SupabaseManager.SUPABASE_URL}/rest/v1/affiliate_profiles?select=*")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.setRequestProperty("apikey", SupabaseManager.SUPABASE_KEY)
                    conn.setRequestProperty("Authorization", "Bearer ${SupabaseManager.SUPABASE_KEY}")
                    val text = conn.inputStream.bufferedReader().readText()
                    conn.disconnect()
                    val arr = JSONArray(text)
                    (0 until arr.length()).map { i ->
                        val obj = arr.getJSONObject(i)
                        SupabaseManager.Affiliate(
                            id_affiliate = obj.getString("id_affiliate"),
                            nama_lengkap = obj.optString("nama_lengkap"),
                            kode_referal = obj.optString("kode_referal"),
                            komisi_persen = obj.optDouble("komisi_persen"),
                            total_komisi = obj.optDouble("total_komisi"),
                            minimum_payout = obj.optDouble("minimum_payout"),
                            status_affiliate = obj.optString("status_affiliate"),
                            payment_method = obj.optString("payment_method"),
                            user_id = obj.optString("user_id")
                        )
                    }
                } catch (e: Exception) { emptyList() }
            }
            adapter.updateData(list)
        }
    }
}
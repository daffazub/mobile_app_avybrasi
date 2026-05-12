package com.example.vybrasiapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vybrasiapp.model.KeuanganAffiliate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AffiliateDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_affiliate_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val idAffiliate    = intent.getStringExtra("id_affiliate") ?: return
        val tvNama         = findViewById<TextView>(R.id.tvDetailNama)
        val tvKode         = findViewById<TextView>(R.id.tvDetailKode)
        val tvStatus       = findViewById<TextView>(R.id.tvDetailStatus)
        val tvKomisiPersen = findViewById<TextView>(R.id.tvDetailKomisiPersen)
        val tvTotalKomisi  = findViewById<TextView>(R.id.tvDetailTotalKomisi)
        val tvMinPayout    = findViewById<TextView>(R.id.tvDetailMinPayout)
        val tvPayment      = findViewById<TextView>(R.id.tvDetailPayment)
        val rvRiwayat      = findViewById<RecyclerView>(R.id.rvRiwayatKomisi)
        val progressBar    = findViewById<ProgressBar>(R.id.progressBar)

        rvRiwayat.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                val affiliate = withContext(Dispatchers.IO) {
                    AffiliateRepository.getAffiliateById(idAffiliate)
                }
                val riwayat = withContext(Dispatchers.IO) {
                    AffiliateRepository.getKomisiAffiliate(idAffiliate)
                }

                progressBar.visibility = View.GONE

                if (affiliate != null) {
                    supportActionBar?.title = affiliate.namaLengkap ?: "Detail Affiliate"
                    tvNama.text         = affiliate.namaLengkap ?: "-"
                    tvKode.text         = "Kode: ${affiliate.kodeReferal ?: "-"}"
                    tvStatus.text       = affiliate.statusAffiliate?.replaceFirstChar { it.uppercase() } ?: "-"
                    tvKomisiPersen.text = "${affiliate.komisiPersen ?: 0}%"
                    tvTotalKomisi.text  = "Rp ${"%,.0f".format(affiliate.totalKomisi ?: 0.0)}"
                    tvMinPayout.text    = "Rp ${"%,.0f".format(affiliate.minimumPayout ?: 0.0)}"
                    tvPayment.text      = affiliate.paymentMethod ?: "-"
                }

                rvRiwayat.adapter = RiwayatKomisiAdapter(riwayat)

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvNama.text = "Gagal memuat: ${e.message}"
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

class RiwayatKomisiAdapter(
    private val list: List<KeuanganAffiliate>
) : RecyclerView.Adapter<RiwayatKomisiAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipe: TextView    = view.findViewById(R.id.tvRiwayatTipe)
        val tvJumlah: TextView  = view.findViewById(R.id.tvRiwayatJumlah)
        val tvTanggal: TextView = view.findViewById(R.id.tvRiwayatTanggal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_komisi, parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = list[position]
        holder.tvTipe.text    = r.tipe.replaceFirstChar { it.uppercase() }
        holder.tvJumlah.text  = "Rp ${"%,.0f".format(r.jumlah)}"
        holder.tvTanggal.text = r.createdAt?.take(10) ?: "-"
    }
}
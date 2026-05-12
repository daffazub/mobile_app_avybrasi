package com.example.vybrasiapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vybrasiapp.SupabaseManager.Affiliate

class AffiliateAdapter(
    private var list: List<Affiliate>,
    private val onClick: (Affiliate) -> Unit
) : RecyclerView.Adapter<AffiliateAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvNamaAffiliate)
        val tvKode: TextView = view.findViewById(R.id.tvKodeReferral)
        val tvKomisi: TextView = view.findViewById(R.id.tvTotalKomisi)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_affiliate, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvNama.text = item.nama_lengkap ?: "-"
        holder.tvKode.text = "Kode: ${item.kode_referal ?: "-"}"
        holder.tvKomisi.text = "Rp ${SupabaseManager.formatRupiah(item.total_komisi ?: 0.0)}"
        holder.tvStatus.text = if (item.status_affiliate == "active") "Aktif" else "Nonaktif"
        holder.tvStatus.setBackgroundColor(
            if (item.status_affiliate == "active") android.graphics.Color.parseColor("#C9A84C")
            else android.graphics.Color.parseColor("#888888")
        )
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Affiliate>) {
        list = newList
        notifyDataSetChanged()
    }
}
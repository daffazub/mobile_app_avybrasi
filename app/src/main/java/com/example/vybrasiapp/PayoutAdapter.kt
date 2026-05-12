package com.example.vybrasiapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class PayoutAdapter(
    private val items: List<PayoutItem>,
    private val onItemClick: (PayoutItem) -> Unit
) : RecyclerView.Adapter<PayoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPayoutName)
        val tvJumlah: TextView = view.findViewById(R.id.tvPayoutJumlah)
        val tvBank: TextView = view.findViewById(R.id.tvPayoutBank)
        val tvTanggal: TextView = view.findViewById(R.id.tvPayoutTanggal)
        // Tidak ada tombol lagi
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.namaAffiliate
        holder.tvJumlah.text = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(item.jumlah)
        holder.tvBank.text = item.bank
        holder.tvTanggal.text = item.tanggal?.substring(0, 10) ?: "-"

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}
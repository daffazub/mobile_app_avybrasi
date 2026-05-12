package com.example.vybrasiapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.vybrasiapp.model.Produk
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale

class KatalogAdapter(
    private val listKopi: List<Produk>,
    private val onClick: ((Produk) -> Unit)? = null
) : RecyclerView.Adapter<KatalogAdapter.KatalogViewHolder>() {

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    inner class KatalogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoto: ShapeableImageView = view.findViewById(R.id.ivItemFoto)
        val tvNama: TextView           = view.findViewById(R.id.tvItemNama)
        val tvKatKemasan: TextView     = view.findViewById(R.id.tvItemKategoriKemasan)
        val tvHarga: TextView          = view.findViewById(R.id.tvItemHarga)
        val tvStok: TextView           = view.findViewById(R.id.tvItemStok)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KatalogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kopi, parent, false)
        return KatalogViewHolder(view)
    }

    override fun onBindViewHolder(holder: KatalogViewHolder, position: Int) {
        val kopi = listKopi[position]

        // Nama
        holder.tvNama.text = kopi.nama ?: "-"

        // Deskripsi singkat → fallback ke deskripsi lengkap
        holder.tvKatKemasan.text = when {
            !kopi.deskripsi_singkat.isNullOrEmpty() -> kopi.deskripsi_singkat
            !kopi.deskripsi_lengkap.isNullOrEmpty() -> kopi.deskripsi_lengkap!!.take(50)
            else -> "Tanpa deskripsi"
        }

        // Harga
        holder.tvHarga.text = formatRupiah.format(kopi.harga ?: 0.0)

        // Stok badge dengan warna otomatis
        val stok = kopi.stok ?: 0
        holder.tvStok.text = "Stok: $stok"
        holder.tvStok.setBackgroundColor(Color.parseColor(when {
            stok < 5  -> "#F44336" // Kritis
            stok < 10 -> "#FF9800" // Waspada
            else      -> "#4CAF50" // Aman
        }))

        // Foto
        holder.ivFoto.load(kopi.gambar_utama) {
            crossfade(true)
            placeholder(R.mipmap.ic_launcher_round)
            error(R.mipmap.ic_launcher_round)
        }

        holder.itemView.setOnClickListener { onClick?.invoke(kopi) }
    }

    override fun getItemCount(): Int = listKopi.size
}
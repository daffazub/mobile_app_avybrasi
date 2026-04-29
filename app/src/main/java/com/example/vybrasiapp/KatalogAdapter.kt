package com.example.vybrasiapp

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.vybrasiapp.model.Produk
import java.text.NumberFormat
import java.util.Locale

class KatalogAdapter(
    private val listKopi: List<Produk>,
    private val onEdit: (Produk) -> Unit,
    private val onDelete: (Produk) -> Unit
) : RecyclerView.Adapter<KatalogAdapter.KatalogViewHolder>() {

    inner class KatalogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoto: ImageView = view.findViewById(R.id.ivItemFoto)
        val tvNama: TextView = view.findViewById(R.id.tvItemNama)
        val tvKatKemasan: TextView = view.findViewById(R.id.tvItemKategoriKemasan)
        val tvHarga: TextView = view.findViewById(R.id.tvItemHarga)
        val btnOpsi: ImageButton = view.findViewById(R.id.btnItemHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KatalogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kopi, parent, false)
        return KatalogViewHolder(view)
    }

    override fun onBindViewHolder(holder: KatalogViewHolder, position: Int) {
        val kopi = listKopi[position]
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        // PERBAIKAN: Gunakan pemanggilan variabel ERD yang baru
        holder.tvNama.text = kopi.nama
        val deskripsiText = kopi.deskripsi_lengkap ?: "Tanpa deskripsi"
        holder.tvKatKemasan.text = deskripsiText
        holder.tvHarga.text = formatRupiah.format(kopi.harga)

        holder.ivFoto.load(kopi.gambar_utama) {
            crossfade(true)
            placeholder(R.mipmap.ic_launcher_round)
            error(R.mipmap.ic_launcher_round)
        }

        holder.btnOpsi.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menu.add(0, 0, 0, "✏️ Edit Produk")
            popupMenu.menu.add(0, 1, 1, "🗑️ Hapus Produk")

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    0 -> {
                        onEdit(kopi)
                        true
                    }
                    1 -> {
                        val builder = AlertDialog.Builder(view.context)
                        builder.setTitle("Peringatan Hapus")
                        builder.setMessage("Apakah Anda sangat yakin ingin menghapus '${kopi.nama}'? Data yang dihapus tidak dapat dikembalikan.")
                        builder.setPositiveButton("Ya, Hapus") { _, _ -> onDelete(kopi) }
                        builder.setNegativeButton("Batal", null)
                        builder.show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    override fun getItemCount(): Int = listKopi.size
}
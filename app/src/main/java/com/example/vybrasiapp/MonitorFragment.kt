package com.example.vybrasiapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class ProdukMonitorDTO(
    val nama: String = "",
    val stok: Int = 0
)

class MonitorFragment : Fragment() {

    private lateinit var llContainerStokDetail: LinearLayout
    private lateinit var etCariProduk: EditText
    private lateinit var ivRefresh: ImageView
    private lateinit var llStokKritisHighlight: LinearLayout
    private lateinit var llItemKritis: LinearLayout

    private var listProdukFull = listOf<ProdukMonitorDTO>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_monitor, container, false)

        llContainerStokDetail = view.findViewById(R.id.llContainerStokDetail)
        etCariProduk = view.findViewById(R.id.etCariProduk)
        ivRefresh = view.findViewById(R.id.ivRefresh)
        llStokKritisHighlight = view.findViewById(R.id.llStokKritisHighlight)
        llItemKritis = view.findViewById(R.id.llItemKritis)

        ivRefresh.setOnClickListener {
            muatDataOperasional()
        }

        etCariProduk.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().lowercase()
                renderStok(
                    listProdukFull.filter {
                        it.nama.lowercase().contains(keyword)
                    }
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        muatDataOperasional()
    }

    private fun muatDataOperasional() {
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_refresh)
        ivRefresh.startAnimation(anim)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                listProdukFull = withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("produk")
                        .select()
                        .decodeList<ProdukMonitorDTO>()
                }

                // Cek fragment masih terattach sebelum update UI
                if (!isAdded) return@launch

                withContext(Dispatchers.Main) {
                    renderStok(listProdukFull)
                    updateStokKritisHighlight(listProdukFull.filter { it.stok < 5 })
                    cekStokKritis(listProdukFull)
                    ivRefresh.clearAnimation()
                }

            } catch (e: Exception) {
                if (isAdded) {
                    ivRefresh.clearAnimation()
                    Log.e("MONITOR_ERROR", "Gagal: ${e.message}")
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat data produk",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateStokKritisHighlight(kritis: List<ProdukMonitorDTO>) {
        if (!isAdded) return
        if (kritis.isEmpty()) {
            llStokKritisHighlight.visibility = View.GONE
            return
        }

        llStokKritisHighlight.visibility = View.VISIBLE
        llItemKritis.removeAllViews()

        kritis.forEach { produk ->
            val cardView = androidx.cardview.widget.CardView(requireContext()).apply {
                radius = 8f
                cardElevation = 2f
                setCardBackgroundColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
                try {
                    background = resources.getDrawable(R.drawable.bg_item_kritis, null)
                } catch (e: Exception) { /* fallback */ }
            }

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(14, 12, 14, 12)
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val iconWarn = TextView(requireContext()).apply {
                text = "⚠️"
                textSize = 16f
                setPadding(0, 0, 8, 0)
            }

            val tvNama = TextView(requireContext()).apply {
                text = produk.nama
                textSize = 14f
                setTextColor(Color.parseColor("#1A1A1A"))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val tvStok = TextView(requireContext()).apply {
                text = "Stok: ${produk.stok}"
                textSize = 13f
                setTextColor(Color.parseColor("#F44336"))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            row.addView(iconWarn)
            row.addView(tvNama)
            row.addView(tvStok)

            cardView.addView(row)
            llItemKritis.addView(cardView)
        }
    }

    private fun cekStokKritis(list: List<ProdukMonitorDTO>) {
        if (!isAdded) return
        val kritis = list.filter { it.stok < 5 }
        if (kritis.isEmpty()) return

        val prefs = requireContext().getSharedPreferences("stok_prefs", Context.MODE_PRIVATE)
        val lastNotified = prefs.getString("last_notified_date", null)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastNotified == today) return

        val namaProduk = kritis.joinToString("\n") { "• ${it.nama} (sisa ${it.stok})" }
        NotifikasiHelper.kirimNotifikasiStokKritis(requireContext(), namaProduk)

        prefs.edit().putString("last_notified_date", today).apply()
    }

    private fun renderStok(stokList: List<ProdukMonitorDTO>) {
        if (!isAdded) return
        llContainerStokDetail.removeAllViews()

        if (stokList.isEmpty()) {
            llContainerStokDetail.addView(TextView(requireContext()).apply {
                text = "Tidak ada data produk."
                setPadding(0, 8, 0, 8)
                setTextColor(Color.GRAY)
            })
            return
        }

        stokList.forEach { produk ->
            val cardView = androidx.cardview.widget.CardView(requireContext()).apply {
                radius = 10f
                cardElevation = 2f
                setCardBackgroundColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
            }

            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 14, 16, 14)
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val warna = when {
                produk.stok < 5 -> "#F44336"
                produk.stok <= 10 -> "#FF9800"
                else -> "#4CAF50"
            }

            val indikator = View(requireContext()).apply {
                setBackgroundColor(Color.parseColor(warna))
                layoutParams = LinearLayout.LayoutParams(
                    6,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(0, 0, 12, 0)
                    height = 40
                }
            }

            val tvNama = TextView(requireContext()).apply {
                text = produk.nama
                textSize = 14f
                setTextColor(Color.parseColor("#1A1A1A"))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val tvStok = TextView(requireContext()).apply {
                text = "${produk.stok} kemasan"
                textSize = 13f
                setTextColor(Color.parseColor(warna))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            row.addView(indikator)
            row.addView(tvNama)
            row.addView(tvStok)

            cardView.addView(row)
            llContainerStokDetail.addView(cardView)
        }
    }
}
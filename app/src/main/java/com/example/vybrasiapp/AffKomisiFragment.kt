package com.example.vybrasiapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vybrasiapp.model.Affiliate
import com.example.vybrasiapp.model.KeuanganAffiliate
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AffKomisiFragment : Fragment() {

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private var periodeSaatIni = "Bulan Ini"
    private var semuaRiwayat = listOf<KeuanganAffiliate>()
    private lateinit var barChart: BarChart

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_aff_komisi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = view.findViewById(R.id.barChartKomisi)
        val tvFilter = view.findViewById<TextView>(R.id.tvAffFilterPeriode)

        setupGrafik()

        tvFilter.setOnClickListener { v ->
            val popup = PopupMenu(requireContext(), v)
            listOf("Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini").forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { item ->
                periodeSaatIni = item.title.toString()
                tvFilter.text = "$periodeSaatIni ▾"
                prosesDanTampilkan(view)
                true
            }
            popup.show()
        }

        muatData(view)
    }

    private fun setupGrafik() {
        barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                textColor = Color.GRAY
                axisMinimum = 0f
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.GRAY
            }
        }
    }

    private fun muatData(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseManager.client.auth.currentUserOrNull()
                } ?: return@launch

                // Ambil id_affiliate dulu
                val affiliate = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest
                        .from("affiliate_profiles")
                        .select { filter { eq("user_id", user.id) }; limit(1) }
                        .decodeSingleOrNull<Affiliate>()
                } ?: return@launch

                semuaRiwayat = withContext(Dispatchers.IO) {
                    SupabaseManager.client.postgrest
                        .from("keuangan")
                        .select {
                            filter { eq("kode", affiliate.kodeReferal ?: "")}
                            order("created_at", Order.DESCENDING)
                        }
                        .decodeList<KeuanganAffiliate>()
                }

                prosesDanTampilkan(view)

            } catch (e: Exception) {
                android.util.Log.e("AFF_KOMISI", "Gagal: ${e.message}")
            }
        }
    }

    private fun prosesDanTampilkan(view: View) {
        val tvTotal     = view.findViewById<TextView>(R.id.tvAffTotalKomisiStat)
        val tvJml       = view.findViewById<TextView>(R.id.tvAffJmlTransaksi)
        val tvRata      = view.findViewById<TextView>(R.id.tvAffRataRata)
        val rvRiwayat   = view.findViewById<RecyclerView>(R.id.rvAffKomisiRiwayat)

        rvRiwayat.layoutManager = LinearLayoutManager(requireContext())

        val kalenderSekarang = Calendar.getInstance()
        val tahunSekarang  = kalenderSekarang.get(Calendar.YEAR)
        val bulanSekarang  = kalenderSekarang.get(Calendar.MONTH)
        val mingguSekarang = kalenderSekarang.get(Calendar.WEEK_OF_YEAR)
        val hariSekarang   = kalenderSekarang.get(Calendar.DAY_OF_YEAR)

        val labelGrafik = ArrayList<String>()
        var nilaiBalok = FloatArray(0)

        when (periodeSaatIni) {
            "Hari Ini" -> { labelGrafik.addAll(listOf("Pagi","Siang","Sore","Malam")); nilaiBalok = FloatArray(4) }
            "Minggu Ini" -> { labelGrafik.addAll(listOf("Sen","Sel","Rab","Kam","Jum","Sab","Min")); nilaiBalok = FloatArray(7) }
            "Bulan Ini" -> { labelGrafik.addAll(listOf("M1","M2","M3","M4")); nilaiBalok = FloatArray(4) }
            "Tahun Ini" -> { labelGrafik.addAll(listOf("Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des")); nilaiBalok = FloatArray(12) }
        }

        var totalKomisi = 0.0
        val listFiltered = mutableListOf<KeuanganAffiliate>()

        semuaRiwayat.forEach { r ->
            val tgl = parseTanggal(r.createdAt) ?: return@forEach
            val kal = Calendar.getInstance().apply { time = tgl }
            val jumlah = r.jumlah
            var masuk = false

            when (periodeSaatIni) {
                "Hari Ini" -> if (kal.get(Calendar.DAY_OF_YEAR) == hariSekarang && kal.get(Calendar.YEAR) == tahunSekarang) {
                    masuk = true
                    when (kal.get(Calendar.HOUR_OF_DAY)) {
                        in 0..10 -> nilaiBalok[0] += jumlah.toFloat()
                        in 11..14 -> nilaiBalok[1] += jumlah.toFloat()
                        in 15..17 -> nilaiBalok[2] += jumlah.toFloat()
                        else -> nilaiBalok[3] += jumlah.toFloat()
                    }
                }
                "Minggu Ini" -> if (kal.get(Calendar.WEEK_OF_YEAR) == mingguSekarang && kal.get(Calendar.YEAR) == tahunSekarang) {
                    masuk = true
                    var idx = kal.get(Calendar.DAY_OF_WEEK) - 2
                    if (idx < 0) idx = 6
                    nilaiBalok[idx] += jumlah.toFloat()
                }
                "Bulan Ini" -> if (kal.get(Calendar.MONTH) == bulanSekarang && kal.get(Calendar.YEAR) == tahunSekarang) {
                    masuk = true
                    when (kal.get(Calendar.DAY_OF_MONTH)) {
                        in 1..7 -> nilaiBalok[0] += jumlah.toFloat()
                        in 8..14 -> nilaiBalok[1] += jumlah.toFloat()
                        in 15..21 -> nilaiBalok[2] += jumlah.toFloat()
                        else -> nilaiBalok[3] += jumlah.toFloat()
                    }
                }
                "Tahun Ini" -> if (kal.get(Calendar.YEAR) == tahunSekarang) {
                    masuk = true
                    nilaiBalok[kal.get(Calendar.MONTH)] += jumlah.toFloat()
                }
            }

            if (masuk) {
                totalKomisi += jumlah
                listFiltered.add(r)
            }
        }

        val rata = if (listFiltered.isNotEmpty()) totalKomisi / listFiltered.size else 0.0

        tvTotal.text = formatRupiah.format(totalKomisi)
        tvJml.text   = "${listFiltered.size} transaksi"
        tvRata.text  = formatRupiah.format(rata)

        // Update grafik
        val entries = nilaiBalok.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val dataSet = BarDataSet(entries, "Komisi").apply {
            color = Color.parseColor("#C9A84C")
            setDrawValues(false)
        }
        barChart.data = BarData(dataSet).apply { barWidth = 0.5f }
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labelGrafik)
            labelCount = labelGrafik.size
        }
        barChart.animateY(1000)
        barChart.invalidate()

        // Update RecyclerView
        rvRiwayat.adapter = RiwayatKomisiAdapter(listFiltered)
    }

    private fun parseTanggal(str: String?): Date? {
        if (str.isNullOrEmpty()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(str)
        } catch (e: Exception) {
            try { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(str) }
            catch (e2: Exception) { null }
        }
    }
}
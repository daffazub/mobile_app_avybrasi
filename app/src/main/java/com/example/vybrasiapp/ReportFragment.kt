package com.example.vybrasiapp

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.vybrasiapp.model.WebOrderModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportFragment : Fragment() {

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    private val formatTanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private lateinit var tvTotalTransaksi: TextView
    private lateinit var tvRataRataOrder: TextView
    private lateinit var tvFilterPeriode: TextView
    private lateinit var barChart: BarChart
    private lateinit var barChartAffiliate: BarChart

    private var periodeSaatIni = "Bulan Ini"
    private var kalenderPilihanKustom: Calendar? = null
    private var semuaPesanan = listOf<WebOrderModel>()

    private var stringTotalTransaksi = "0 Order"
    private var stringRataRata = "Rp0"
    private var stringTotalOmset = "Rp0"

    private var topAffiliates = listOf<Pair<String, Long>>()
    private var stringTotalKomisiAffiliate = "Rp0"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report, container, false)

        tvTotalTransaksi  = view.findViewById(R.id.tvTotalTransaksi)
        tvRataRataOrder   = view.findViewById(R.id.tvRataRataOrder)
        tvFilterPeriode   = view.findViewById(R.id.tvFilterPeriode)
        barChart          = view.findViewById(R.id.barChartPenjualan)
        barChartAffiliate = view.findViewById(R.id.barChartAffiliate)

        val btnUnduhPdf      = view.findViewById<Button>(R.id.btnUnduhPdf)
        val btnShareWhatsapp = view.findViewById<Button>(R.id.btnShareWhatsapp)

        setupGrafik()
        setupGrafikAffiliate(barChartAffiliate)
        tarikDataDariSupabase()

        tvFilterPeriode.setOnClickListener { viewDiklik ->
            val popupMenu = PopupMenu(requireContext(), viewDiklik)
            popupMenu.menu.add("Hari Ini")
            popupMenu.menu.add("Minggu Ini")
            popupMenu.menu.add("Bulan Ini")
            popupMenu.menu.add("Tahun Ini")
            popupMenu.menu.add("Pilih Tanggal...")

            popupMenu.setOnMenuItemClickListener { menuItem ->
                val pilihan = menuItem.title.toString()
                if (pilihan == "Pilih Tanggal...") {
                    tampilkanDatePicker()
                } else {
                    kalenderPilihanKustom = null
                    periodeSaatIni = pilihan
                    tvFilterPeriode.text = "$periodeSaatIni ▾"
                    prosesDanTampilkanGrafik()
                }
                true
            }
            popupMenu.show()
        }

        btnUnduhPdf.setOnClickListener { cetakLaporanPDF() }
        btnShareWhatsapp.setOnClickListener { shareKeWhatsapp() }

        return view
    }

    private fun setupGrafik() {
        barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            axisLeft.apply {
                setDrawGridLines(true)
                textColor = Color.GRAY
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.GRAY
            }
        }
    }

    private fun setupGrafikAffiliate(chart: BarChart) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            axisRight.isEnabled = false
            setNoDataText("Belum ada data affiliate")
            setNoDataTextColor(Color.GRAY)
            axisLeft.apply {
                textColor = Color.GRAY
                axisMinimum = 0f
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.GRAY
            }
        }
    }

    private fun tarikDataDariSupabase() {
        tvTotalTransaksi.text = "Memuat..."
        tvRataRataOrder.text  = "Memuat..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                semuaPesanan = withContext(Dispatchers.IO) {
                    SupabaseManager.client.from("transaksi")
                        .select()
                        .decodeList<WebOrderModel>()
                }
                prosesDanTampilkanGrafik()
            } catch (e: Exception) {
                Log.e("REPORT_ERROR", "Gagal: ${e.message}")
                Toast.makeText(requireContext(), "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Proses & tampilkan grafik + hitung komisi affiliate ──
    private fun prosesDanTampilkanGrafik() {
        val kalenderSekarang = Calendar.getInstance()
        val tahunSekarang  = kalenderSekarang.get(Calendar.YEAR)
        val bulanSekarang  = kalenderSekarang.get(Calendar.MONTH)
        val mingguSekarang = kalenderSekarang.get(Calendar.WEEK_OF_YEAR)
        val hariSekarang   = kalenderSekarang.get(Calendar.DAY_OF_YEAR)

        var totalTransaksi = 0
        var totalOmset     = 0L
        val labelGrafik    = ArrayList<String>()
        var nilaiBalok     = FloatArray(0)

        // Map untuk komisi affiliate: id_affiliate -> total komisi
        val komisiMap = mutableMapOf<String, Long>()

        when {
            periodeSaatIni == "Hari Ini" || kalenderPilihanKustom != null -> {
                labelGrafik.addAll(listOf("Pagi", "Siang", "Sore", "Malam"))
                nilaiBalok = FloatArray(4)
            }
            periodeSaatIni == "Minggu Ini" -> {
                labelGrafik.addAll(listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"))
                nilaiBalok = FloatArray(7)
            }
            periodeSaatIni == "Bulan Ini" -> {
                labelGrafik.addAll(listOf("M1", "M2", "M3", "M4"))
                nilaiBalok = FloatArray(4)
            }
            periodeSaatIni == "Tahun Ini" -> {
                labelGrafik.addAll(listOf("Jan","Feb","Mar","Apr","Mei","Jun",
                    "Jul","Agu","Sep","Okt","Nov","Des"))
                nilaiBalok = FloatArray(12)
            }
        }

        for (pesanan in semuaPesanan) {
            val tanggalPesanan = parseTanggalSupabase(pesanan.created_at) ?: continue
            val kalenderPesanan = Calendar.getInstance().apply { time = tanggalPesanan }
            val harga = pesanan.total_harga?.toLong() ?: 0L
            var masukPeriodeIni = false

            when {
                kalenderPilihanKustom != null -> {
                    if (kalenderPesanan.get(Calendar.DAY_OF_YEAR) == kalenderPilihanKustom!!.get(Calendar.DAY_OF_YEAR) &&
                        kalenderPesanan.get(Calendar.YEAR) == kalenderPilihanKustom!!.get(Calendar.YEAR)) {
                        masukPeriodeIni = true
                        when (kalenderPesanan.get(Calendar.HOUR_OF_DAY)) {
                            in 0..10 -> nilaiBalok[0] += harga.toFloat()
                            in 11..14 -> nilaiBalok[1] += harga.toFloat()
                            in 15..17 -> nilaiBalok[2] += harga.toFloat()
                            else -> nilaiBalok[3] += harga.toFloat()
                        }
                    }
                }
                periodeSaatIni == "Hari Ini" -> {
                    if (kalenderPesanan.get(Calendar.DAY_OF_YEAR) == hariSekarang &&
                        kalenderPesanan.get(Calendar.YEAR) == tahunSekarang) {
                        masukPeriodeIni = true
                        when (kalenderPesanan.get(Calendar.HOUR_OF_DAY)) {
                            in 0..10 -> nilaiBalok[0] += harga.toFloat()
                            in 11..14 -> nilaiBalok[1] += harga.toFloat()
                            in 15..17 -> nilaiBalok[2] += harga.toFloat()
                            else -> nilaiBalok[3] += harga.toFloat()
                        }
                    }
                }
                periodeSaatIni == "Minggu Ini" -> {
                    if (kalenderPesanan.get(Calendar.WEEK_OF_YEAR) == mingguSekarang &&
                        kalenderPesanan.get(Calendar.YEAR) == tahunSekarang) {
                        masukPeriodeIni = true
                        var hariIndex = kalenderPesanan.get(Calendar.DAY_OF_WEEK) - 2
                        if (hariIndex < 0) hariIndex = 6
                        nilaiBalok[hariIndex] += harga.toFloat()
                    }
                }
                periodeSaatIni == "Bulan Ini" -> {
                    if (kalenderPesanan.get(Calendar.MONTH) == bulanSekarang &&
                        kalenderPesanan.get(Calendar.YEAR) == tahunSekarang) {
                        masukPeriodeIni = true
                        when (kalenderPesanan.get(Calendar.DAY_OF_MONTH)) {
                            in 1..7   -> nilaiBalok[0] += harga.toFloat()
                            in 8..14  -> nilaiBalok[1] += harga.toFloat()
                            in 15..21 -> nilaiBalok[2] += harga.toFloat()
                            else      -> nilaiBalok[3] += harga.toFloat()
                        }
                    }
                }
                periodeSaatIni == "Tahun Ini" -> {
                    if (kalenderPesanan.get(Calendar.YEAR) == tahunSekarang) {
                        masukPeriodeIni = true
                        nilaiBalok[kalenderPesanan.get(Calendar.MONTH)] += harga.toFloat()
                    }
                }
            }

            if (masukPeriodeIni) {
                totalTransaksi++
                totalOmset += harga

                // Kumpulkan komisi affiliate
                if (!pesanan.id_affiliate.isNullOrBlank()) {
                    val komisi = (harga * 5) / 100
                    komisiMap[pesanan.id_affiliate!!] =
                        (komisiMap[pesanan.id_affiliate] ?: 0L) + komisi
                }
            }
        }

        // Update ringkasan
        val rataRata = if (totalTransaksi > 0) totalOmset / totalTransaksi else 0L
        stringTotalTransaksi = "$totalTransaksi Order"
        stringRataRata       = formatRupiah.format(rataRata)
        stringTotalOmset     = formatRupiah.format(totalOmset)

        tvTotalTransaksi.text = stringTotalTransaksi
        tvRataRataOrder.text  = stringRataRata
        view?.findViewById<TextView>(R.id.tvTotalOmsetReport)?.text = stringTotalOmset

        // Update chart pendapatan
        val entries = nilaiBalok.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val dataSet = BarDataSet(entries, "Pendapatan").apply {
            color = Color.parseColor("#D4AF37")
            setDrawValues(false)
        }
        barChart.data = BarData(dataSet).apply { barWidth = 0.5f }
        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labelGrafik)
            labelCount = labelGrafik.size
        }
        barChart.animateY(1000)
        barChart.invalidate()

        // Update chart affiliate menggunakan komisiMap
        viewLifecycleOwner.lifecycleScope.launch {
            updateAffiliateFromKomisiMap(komisiMap)
        }
    }

    private suspend fun updateAffiliateFromKomisiMap(komisiMap: Map<String, Long>) {
        if (komisiMap.isEmpty()) {
            topAffiliates = emptyList()
            stringTotalKomisiAffiliate = "Rp0"
            withContext(Dispatchers.Main) {
                barChartAffiliate.data = null
                barChartAffiliate.setNoDataText("Belum ada data affiliate")
                barChartAffiliate.invalidate()
            }
            return
        }

        val ids = komisiMap.keys.toList()
        try {
            val semuaProfiles = withContext(Dispatchers.IO) {
                SupabaseManager.client.from("affiliate_profiles")
                    .select()
                    .decodeList<AffiliateProfile>()
            }
            val profiles = semuaProfiles.filter { it.id_affiliate in ids }

            val namaMap = mutableMapOf<String, String>()
            profiles.forEach { namaMap[it.id_affiliate] = it.nama_lengkap ?: "-" }

            topAffiliates = komisiMap.entries
                .map { (id, komisi) -> (namaMap[id] ?: id) to komisi }
                .sortedByDescending { it.second }
                .take(5)

            stringTotalKomisiAffiliate = formatRupiah.format(topAffiliates.sumOf { it.second })
        } catch (e: Exception) {
            Log.e("REPORT_ERROR", "Gagal ambil nama affiliate: ${e.message}")
            topAffiliates = emptyList()
            stringTotalKomisiAffiliate = "Rp0"
        }

        // Update chart di UI thread
        withContext(Dispatchers.Main) {
            if (topAffiliates.isEmpty()) {
                barChartAffiliate.data = null
                barChartAffiliate.setNoDataText("Belum ada data affiliate")
                barChartAffiliate.invalidate()
                return@withContext
            }

            // Hapus teks "Belum ada data affiliate"
            barChartAffiliate.setNoDataText(null)

            val entries = topAffiliates.mapIndexed { i, (_, komisi) ->
                BarEntry(i.toFloat(), komisi.toFloat())
            }
            val labels = topAffiliates.map { it.first }

            val dataSet = BarDataSet(entries, "Komisi").apply {
                color = Color.parseColor("#C9A84C")
                valueTextColor = Color.WHITE
                valueTextSize = 10f
                setDrawValues(false)
            }

            val barData = BarData(dataSet)
            barData.barWidth = 0.5f

            barChartAffiliate.apply {
                data = barData
                xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                xAxis.labelCount = labels.size
                xAxis.granularity = 1f
                animateY(800)
                invalidate()
            }

            Log.d("AFF_DEBUG", "Chart affiliate diupdate, entries: ${entries.size}")
        }
    }

    // ── Parse Tanggal ─────────────────────────────────────
    private fun parseTanggalSupabase(tanggalStr: String?): Date? {
        if (tanggalStr.isNullOrEmpty()) return null

        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd HH:mm:ss.SSSSSSX",
            "yyyy-MM-dd HH:mm:ss.SSS XXX",
            "yyyy-MM-dd HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ss"
        )

        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(tanggalStr)
            } catch (e: Exception) {
                // lanjut
            }
        }
        return null
    }

    private fun tampilkanDatePicker() {
        val kalender = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            kalenderPilihanKustom = Calendar.getInstance().apply { set(year, month, day) }
            periodeSaatIni = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                .format(kalenderPilihanKustom!!.time)
            tvFilterPeriode.text = "$periodeSaatIni ▾"
            prosesDanTampilkanGrafik()
        }, kalender.get(Calendar.YEAR), kalender.get(Calendar.MONTH),
            kalender.get(Calendar.DAY_OF_MONTH)).show()
    }

    // ── PDF & Share (sama seperti sebelumnya) ──────────────
    private fun cetakLaporanPDF() {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        titlePaint.apply {
            textAlign = Paint.Align.CENTER
            textSize = 24f
            isFakeBoldText = true
            color = Color.BLACK
        }
        canvas.drawText("LAPORAN PENJUALAN VYBRASI", 595f / 2, 80f, titlePaint)

        paint.apply {
            textAlign = Paint.Align.CENTER
            textSize = 14f
            color = Color.DKGRAY
        }
        val currentDate = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())
        canvas.drawText("Dicetak pada: $currentDate", 595f / 2, 110f, paint)
        canvas.drawLine(50f, 130f, 545f, 130f, paint)

        paint.apply {
            textAlign = Paint.Align.LEFT
            textSize = 16f
            isFakeBoldText = true
            color = Color.BLACK
        }
        canvas.drawText("Ringkasan Performa ($periodeSaatIni):", 50f, 180f, paint)

        paint.textSize = 14f; paint.isFakeBoldText = false
        val perfData = listOf(
            "Total Omset" to stringTotalOmset,
            "Total Transaksi" to stringTotalTransaksi,
            "Rata-rata Order" to stringRataRata
        )
        var yPos = 220f
        for ((label, value) in perfData) {
            canvas.drawText("$label : $value", 50f, yPos, paint)
            yPos += 30f
        }

        if (topAffiliates.isNotEmpty()) {
            yPos += 20f
            paint.isFakeBoldText = true
            canvas.drawText("Top Affiliate (Komisi):", 50f, yPos, paint)
            paint.isFakeBoldText = false
            yPos += 30f
            topAffiliates.forEachIndexed { i, (nama, komisi) ->
                canvas.drawText("${i+1}. $nama - ${formatRupiah.format(komisi)}", 50f, yPos, paint)
                yPos += 24f
            }
            canvas.drawText("Total Komisi: $stringTotalKomisiAffiliate", 50f, yPos, paint)
        }

        paint.apply {
            textAlign = Paint.Align.CENTER
            textSize = 12f
            color = Color.GRAY
        }
        canvas.drawText(
            "Dokumen ini dihasilkan secara otomatis oleh Vybrasi App.",
            595f / 2, 800f, paint
        )

        pdfDocument.finishPage(page)

        val file = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "Laporan_Vybrasi_${System.currentTimeMillis()}.pdf"
        )

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(requireContext(), "✅ PDF disimpan di Documents!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal simpan PDF", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun shareKeWhatsapp() {
        val tanggalSekarang = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())

        val affiliateText = if (topAffiliates.isNotEmpty()) {
            buildString {
                appendLine("━━━━━━━━━━━━━━━━━━━━━━")
                appendLine("🌟 *TOP 5 AFFILIATE*")
                topAffiliates.forEachIndexed { i, (nama, komisi) ->
                    appendLine("${i+1}. $nama → ${formatRupiah.format(komisi)}")
                }
                appendLine("💸 Total Komisi: $stringTotalKomisiAffiliate")
            }
        } else ""

        val pesan = """
🏪 *LAPORAN VYBRASI COFFEE*
━━━━━━━━━━━━━━━━━━━━━━
📅 Periode  : $periodeSaatIni
🕐 Dibuat   : $tanggalSekarang
━━━━━━━━━━━━━━━━━━━━━━

📊 *RINGKASAN PENJUALAN*
💰 Total Omset     : $stringTotalOmset
📦 Total Transaksi : $stringTotalTransaksi
📈 Rata-rata Order : $stringRataRata

$affiliateText
━━━━━━━━━━━━━━━━━━━━━━
_Dikirim otomatis dari Vybrasi App_ ☕
    """.trimIndent()

        try {
            startActivity(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, pesan)
            })
        } catch (e: Exception) {
            startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, pesan)
                    putExtra(Intent.EXTRA_SUBJECT, "Laporan Vybrasi - $periodeSaatIni")
                }, "Bagikan Laporan Via"
            ))
        }
    }
}

@Serializable
data class AffiliateProfile(
    val id_affiliate: String = "",
    val nama_lengkap: String? = null
)
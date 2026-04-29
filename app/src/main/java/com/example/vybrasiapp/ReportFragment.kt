package com.example.vybrasiapp

import android.app.DatePickerDialog
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
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReportFragment : Fragment() {

    private val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    private lateinit var tvTotalTransaksi: TextView
    private lateinit var tvRataRataOrder: TextView
    private lateinit var tvFilterPeriode: TextView
    private lateinit var barChart: BarChart

    private var periodeSaatIni = "Bulan Ini"
    private var kalenderPilihanKustom: Calendar? = null // Menyimpan tanggal kustom jika dipilih
    private var semuaPesanan = listOf<WebOrderModel>()

    private var stringTotalTransaksi = "0 Order"
    private var stringRataRata = "Rp0"
    private var stringTotalOmset = "Rp0"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_report, container, false)

        tvTotalTransaksi = view.findViewById(R.id.tvTotalTransaksi)
        tvRataRataOrder = view.findViewById(R.id.tvRataRataOrder)
        tvFilterPeriode = view.findViewById(R.id.tvFilterPeriode)
        barChart = view.findViewById(R.id.barChartPenjualan)
        val btnUnduhPdf = view.findViewById<Button>(R.id.btnUnduhPdf)

        setupGrafik()
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

        btnUnduhPdf.setOnClickListener {
            cetakLaporanPDF()
        }

        return view
    }

    private fun tampilkanDatePicker() {
        val kalender = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                kalenderPilihanKustom = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }

                // Format teks di tombol "25 Apr 2026"
                val formatTombolUI = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                periodeSaatIni = formatTombolUI.format(kalenderPilihanKustom!!.time)

                tvFilterPeriode.text = "$periodeSaatIni ▾"
                prosesDanTampilkanGrafik()
            },
            kalender.get(Calendar.YEAR),
            kalender.get(Calendar.MONTH),
            kalender.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setupGrafik() {
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawGridBackground(false)
        barChart.setDrawBorders(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = Color.GRAY

        barChart.axisLeft.setDrawGridLines(true)
        barChart.axisLeft.textColor = Color.GRAY
        barChart.axisLeft.axisMinimum = 0f

        barChart.axisRight.isEnabled = false
    }

    private fun tarikDataDariSupabase() {
        tvTotalTransaksi.text = "Memuat..."
        tvRataRataOrder.text = "Memuat..."

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                semuaPesanan = withContext(Dispatchers.IO) {
                    // UBAH NAMA TABEL DARI "web_orders" MENJADI "transaksi"
                    SupabaseManager.client.from("transaksi").select().decodeList<WebOrderModel>()
                }
                prosesDanTampilkanGrafik()
            } catch (e: Exception) {
                Log.e("REPORT_ERROR", "Gagal: ${e.message}")
                Toast.makeText(requireContext(), "Gagal memuat laporan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prosesDanTampilkanGrafik() {
        val kalenderSekarang = Calendar.getInstance()
        val tahunSekarang = kalenderSekarang.get(Calendar.YEAR)
        val bulanSekarang = kalenderSekarang.get(Calendar.MONTH)
        val mingguSekarang = kalenderSekarang.get(Calendar.WEEK_OF_YEAR)
        val hariSekarang = kalenderSekarang.get(Calendar.DAY_OF_YEAR)

        var totalTransaksi = 0
        var totalOmset = 0L

        val labelGrafik = ArrayList<String>()
        var nilaiBalok = FloatArray(0)

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
                labelGrafik.addAll(listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"))
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
                    if (kalenderPesanan.get(Calendar.DAY_OF_YEAR) == hariSekarang && kalenderPesanan.get(Calendar.YEAR) == tahunSekarang) {
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
                    if (kalenderPesanan.get(Calendar.WEEK_OF_YEAR) == mingguSekarang && kalenderPesanan.get(Calendar.YEAR) == tahunSekarang) {
                        masukPeriodeIni = true
                        var hariIndex = kalenderPesanan.get(Calendar.DAY_OF_WEEK) - 2
                        if (hariIndex < 0) hariIndex = 6
                        nilaiBalok[hariIndex] += harga.toFloat()
                    }
                }
                periodeSaatIni == "Bulan Ini" -> {
                    if (kalenderPesanan.get(Calendar.MONTH) == bulanSekarang && kalenderPesanan.get(Calendar.YEAR) == tahunSekarang) {
                        masukPeriodeIni = true
                        val hariKe = kalenderPesanan.get(Calendar.DAY_OF_MONTH)
                        when (hariKe) {
                            in 1..7 -> nilaiBalok[0] += harga.toFloat()
                            in 8..14 -> nilaiBalok[1] += harga.toFloat()
                            in 15..21 -> nilaiBalok[2] += harga.toFloat()
                            else -> nilaiBalok[3] += harga.toFloat()
                        }
                    }
                }
                periodeSaatIni == "Tahun Ini" -> {
                    if (kalenderPesanan.get(Calendar.YEAR) == tahunSekarang) {
                        masukPeriodeIni = true
                        val indexBulan = kalenderPesanan.get(Calendar.MONTH)
                        nilaiBalok[indexBulan] += harga.toFloat()
                    }
                }
            }

            if (masukPeriodeIni) {
                totalTransaksi++
                totalOmset += harga
            }
        }

        val rataRata = if (totalTransaksi > 0) totalOmset / totalTransaksi else 0L

        stringTotalTransaksi = "$totalTransaksi Order"
        stringRataRata = formatRupiah.format(rataRata)
        stringTotalOmset = formatRupiah.format(totalOmset)

        tvTotalTransaksi.text = stringTotalTransaksi
        tvRataRataOrder.text = stringRataRata

        val entries = ArrayList<BarEntry>()
        for (i in nilaiBalok.indices) {
            entries.add(BarEntry(i.toFloat(), nilaiBalok[i]))
        }

        val dataSet = BarDataSet(entries, "Pendapatan")
        dataSet.color = Color.parseColor("#D4AF37")
        dataSet.setDrawValues(false)

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labelGrafik)
        barChart.xAxis.labelCount = labelGrafik.size

        barChart.animateY(1000)
        barChart.invalidate()
    }

    private fun parseTanggalSupabase(tanggalStr: String?): Date? {
        if (tanggalStr.isNullOrEmpty()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(tanggalStr)
        } catch (e: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(tanggalStr)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun cetakLaporanPDF() {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        val myPageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val myPage = pdfDocument.startPage(myPageInfo)
        val canvas: Canvas = myPage.canvas

        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.textSize = 24f
        titlePaint.isFakeBoldText = true
        titlePaint.color = Color.BLACK
        canvas.drawText("LAPORAN PENJUALAN VYBRASI", 595f / 2, 80f, titlePaint)

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 14f
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
        val currentDate = sdf.format(Date())
        canvas.drawText("Dicetak pada: $currentDate", 595f / 2, 110f, paint)

        canvas.drawLine(50f, 130f, 545f, 130f, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("Ringkasan Performa ($periodeSaatIni):", 50f, 180f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false

        val kolomKiri = 50f
        val kolomTengah = 200f
        val kolomKanan = 220f

        canvas.drawText("Total Omset", kolomKiri, 220f, paint)
        canvas.drawText(":", kolomTengah, 220f, paint)
        canvas.drawText(stringTotalOmset, kolomKanan, 220f, paint)

        canvas.drawText("Total Transaksi", kolomKiri, 250f, paint)
        canvas.drawText(":", kolomTengah, 250f, paint)
        canvas.drawText(stringTotalTransaksi, kolomKanan, 250f, paint)

        canvas.drawText("Rata-rata Order", kolomKiri, 280f, paint)
        canvas.drawText(":", kolomTengah, 280f, paint)
        canvas.drawText(stringRataRata, kolomKanan, 280f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 12f
        paint.color = Color.GRAY
        canvas.drawText("Dokumen ini dihasilkan secara otomatis oleh Vybrasi App.", 595f / 2, 800f, paint)

        pdfDocument.finishPage(myPage)

        val fileName = "Laporan_Vybrasi_${System.currentTimeMillis()}.pdf"
        val folderAman = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(folderAman, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(requireContext(), "PDF Disimpan di folder Aplikasi/Documents!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("PDF_ERROR", "Gagal menyimpan: ${e.message}")
            Toast.makeText(requireContext(), "Gagal menyimpan PDF", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }
}
package com.example.vybrasiapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.vybrasiapp.model.ProdukInsert
import com.google.android.material.card.MaterialCardView
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FormKatalogActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private lateinit var ivFotoKopi: ImageView
    private lateinit var tvHintUpload: TextView
    private lateinit var btnSimpan: Button

    // Variabel untuk Mode Edit
    private var isEditMode = false
    private var editId: String = "" // Sudah benar menggunakan String untuk UUID
    private var oldImageUrl: String = ""

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            ivFotoKopi.setImageURI(uri)
            tvHintUpload.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_katalog)

        val btnBack = findViewById<TextView>(R.id.btnBackForm)
        btnBack.setOnClickListener { finish() }

        ivFotoKopi = findViewById(R.id.ivFotoKopi)
        tvHintUpload = findViewById(R.id.tvHintUpload)
        val cvUploadFoto = findViewById<MaterialCardView>(R.id.cvUploadFoto)
        btnSimpan = findViewById(R.id.btnSimpanKatalog)

        cvUploadFoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSimpan.setOnClickListener {
            if (isEditMode) {
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Konfirmasi Update")
                builder.setMessage("Apakah Anda yakin ingin menyimpan perubahan pada produk ini?")
                builder.setPositiveButton("Ya, Simpan") { _, _ -> siapkanData() }
                builder.setNegativeButton("Batal", null)
                builder.show()
            } else {
                siapkanData()
            }
        }

        cekModeEdit()
    }

    private fun cekModeEdit() {
        val idDariIntent = intent.getStringExtra("EXTRA_ID")
        if (!idDariIntent.isNullOrEmpty()) {
            isEditMode = true
            editId = idDariIntent
            btnSimpan.text = "Update Produk"

            val nama = intent.getStringExtra("EXTRA_NAMA") ?: ""
            val harga = intent.getIntExtra("EXTRA_HARGA", 0)
            val deskripsiLengkap = intent.getStringExtra("EXTRA_DESKRIPSI") ?: ""
            oldImageUrl = intent.getStringExtra("EXTRA_GAMBAR") ?: ""

            findViewById<EditText>(R.id.etNamaKopi).setText(nama)
            findViewById<EditText>(R.id.etHargaKopi).setText(harga.toString())

            try {
                val parts = deskripsiLengkap.split(" • ")
                if (parts.size >= 3) {
                    val kategori = parts[0]
                    val kemasan = parts[1].replace("Kemasan: ", "")
                    val deskripsiAwal = parts[2]

                    findViewById<EditText>(R.id.etKemasan).setText(kemasan)
                    findViewById<EditText>(R.id.etDeskripsi).setText(deskripsiAwal)

                    if (kategori == "Robusta") {
                        findViewById<RadioGroup>(R.id.rgKategoriKopi).check(R.id.rbRobusta)
                    } else {
                        findViewById<RadioGroup>(R.id.rgKategoriKopi).check(R.id.rbArabica)
                    }
                } else {
                    findViewById<EditText>(R.id.etDeskripsi).setText(deskripsiLengkap)
                }
            } catch (e: Exception) {
                findViewById<EditText>(R.id.etDeskripsi).setText(deskripsiLengkap)
            }

            if (oldImageUrl.isNotEmpty()) {
                ivFotoKopi.load(oldImageUrl)
                tvHintUpload.visibility = View.GONE
            }
        }
    }

    private fun siapkanData() {
        val namaKopi = findViewById<EditText>(R.id.etNamaKopi).text.toString()
        val kemasan = findViewById<EditText>(R.id.etKemasan).text.toString()
        val hargaStr = findViewById<EditText>(R.id.etHargaKopi).text.toString()
        val deskripsiAwal = findViewById<EditText>(R.id.etDeskripsi).text.toString()
        val rgKategori = findViewById<RadioGroup>(R.id.rgKategoriKopi)

        if (namaKopi.isEmpty() || kemasan.isEmpty() || hargaStr.isEmpty()) {
            Toast.makeText(this, "Nama, Kemasan, dan Harga wajib diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val kategori = if (rgKategori.checkedRadioButtonId == R.id.rbArabica) "Arabica" else "Robusta"
        val harga = hargaStr.toDouble()
        val deskripsiLengkap = "$kategori • Kemasan: $kemasan • $deskripsiAwal"

        btnSimpan.isEnabled = false
        Toast.makeText(this, "Menyimpan data... ⏳", Toast.LENGTH_SHORT).show()

        var imageByteArray: ByteArray? = null
        if (imageUri != null) {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            imageByteArray = inputStream?.readBytes()
            inputStream?.close()
        }

        simpanKeSupabase(namaKopi, harga, deskripsiLengkap, imageByteArray)
    }

    private fun simpanKeSupabase(namaKopi: String, hargaKopi: Double, deskripsiLengkap: String, imageByteArray: ByteArray?) {
        lifecycleScope.launch {
            try {
                var imageUrlToSave = oldImageUrl

                if (imageByteArray != null) {
                    val fileName = "produk_${System.currentTimeMillis()}.jpg"
                    val bucket = SupabaseManager.client.storage.from("produk_images")

                    withContext(Dispatchers.IO) {
                        bucket.upload(fileName, imageByteArray)
                    }
                    imageUrlToSave = bucket.publicUrl(fileName)
                }

                val produkData = ProdukInsert(
                    nama = namaKopi,
                    harga = hargaKopi,
                    deskripsi_lengkap = deskripsiLengkap,
                    gambar_utama = imageUrlToSave
                )

                withContext(Dispatchers.IO) {
                    if (isEditMode) {
                        SupabaseManager.client.from("produk").update(produkData) {
                            filter { eq("id_produk", editId) } // Sesuai dengan kolom UUID di ERD
                        }
                    } else {
                        SupabaseManager.client.from("produk").insert(produkData)
                    }
                }

                val pesanSukses = if (isEditMode) "Produk diupdate! ✅" else "Produk ditambahkan! ✅"
                Toast.makeText(this@FormKatalogActivity, pesanSukses, Toast.LENGTH_SHORT).show()
                finish()

            } catch (e: Exception) {
                Log.e("SUPABASE_ERROR", "Gagal menyimpan: ${e.message}")
                Toast.makeText(this@FormKatalogActivity, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                btnSimpan.isEnabled = true
            }
        }
    }
}
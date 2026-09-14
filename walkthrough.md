# Walkthrough: Migrasi Aplikasi Avybrasi ke Flutter

## Ringkasan Proyek
Aplikasi mobile Affiliate **Avybrasi** telah berhasil dimigrasikan secara penuh dari kode Android Native Kotlin ke framework **Flutter 3.29.0** (Dart). Seluruh kebutuhan bisnis, keamanan data, kepatuhan struktur database Supabase asli, dan standar UI/UX modern (Material 3 & WCAG AA) telah dipenuhi.

---

## 1. Lokasi Berkas & APK yang Siap Diinstal

- **Direktori Proyek Flutter:**
  d:\KULIAH\Semester 4\Avybrasi App\avybrasi_flutter

- **Berkas APK Siap Pakai di HP Android:**
  d:\KULIAH\Semester 4\Avybrasi App\avybrasi_flutter\build\app\outputs\flutter-apk\app-debug.apk *(Ukuran: ~95 MB)*

---

## 2. Cara Menjalankan & Memasang Aplikasi di HP

### Opsi 1: Pasang File APK Langsung ke HP (Paling Praktis)
1. Sambungkan HP ke Laptop via kabel USB (atau kirim berkas APK via WhatsApp Web / Google Drive).
2. Salin file pp-debug.apk yang ada di:
   d:\KULIAH\Semester 4\Avybrasi App\avybrasi_flutter\build\app\outputs\flutter-apk\app-debug.apk
3. Di HP Android, buka file manager dan ketuk pp-debug.apk untuk melakukan instalasi (*Izinkan install dari sumber tidak dikenal jika diminta*).
4. Buka aplikasi **Avybrasi App** langsung dari layar HP Anda.

---

### Opsi 2: Menjalankan dengan USB Debugging & Hot Reload (Pengembangan Aktif)
Jika Anda ingin mengetes sambil koding dan menikmati fitur **Hot Reload** (perubahan kode langsung terlihat seketika):
1. Di HP Android Anda:
   - Buka **Pengaturan (Settings)** -> **Tentang Ponsel (About Phone)**.
   - Ketuk **Nomor Bentukan (Build Number)** sebanyak 7 kali hingga muncul pesan *Anda adalah seorang pengembang*.
   - Masuk ke **Opsi Pengembang (Developer Options)** dan aktifkan **USB Debugging**.
2. Hubungkan HP ke Laptop dengan kabel data USB.
3. Di layar HP, jika muncul pop-up *Izinkan USB Debugging dari komputer ini?*, centang selalu izinkan lalu tekan **OK**.
4. Buka terminal di folder proyek dan jalankan perintah:
   `ash
   cd d:\KULIAH\Semester 4\Avybrasi App\avybrasi_flutter
   D:\src\flutter\bin\flutter.bat run
   `
5. Aplikasi akan otomatis terpasang dan terbuka di HP Anda. Saat Anda mengubah kode dan menekan tombol  di terminal, tampilan di HP akan terbarui seketika!

---

## 3. Fitur yang Telah Diimplementasikan

### 1. Sistem Keamanan & Kepatuhan Database
- **Struktur Database 100% Utuh:** Tidak ada pengubahan tabel, penambahan kolom, maupun penghapusan skema database Supabase.
- **Role-Based Access Gatekeeping:** Memverifikasi hierarki akun (dmin_profiles vs ffiliate_profiles). Role non-affiliate dicegah mengakses dashboard secara aman.
- **Relasi Foreign Key Terjaga:** Semua relasi penarikan dana (payout_requests) secara presisi mengacu ke id_affiliate dari tabel ffiliate_profiles.

### 2. Antarmuka Pengguna Modern & Aksesibel
- **Tab 1: Home (Dashboard Ringkasan)**
  - Kartu ucapan selamat datang dan badge status kemitraan (Aktif / Non-aktif).
  - Kontainer Kode Referal dengan fungsi *Copy to Clipboard* sekali ketuk.
  - Kartu Saldo Komisi Tersedia dengan tombol cepat pencairan dana.
  - Statistik ringkas jumlah transaksi affiliate yang sukses.
- **Tab 2: Transaksi**
  - Pilihan Filter status: *Semua, Pending, Dibayar, Proses, Dikirim, Selesai*.
  - Menghitung komisi per transaksi secara otomatis.
  - Tombol aksi *Cairkan Komisi Sekarang* yang terhubung langsung dengan validasi minimum saldo Rp 50.000 / Rp 100.000.
  - *Empty State* ramah pengguna dengan tombol copy link referral ketika belum ada transaksi.
- **Tab 3: Analisis Komisi (Grafik Interaktif)**
  - Kartu ringkasan: Total Komisi, Komisi Menunggu Pencairan, dan Saldo Siap Ditarik.
  - Grafik batang interaktif menggunakan l_chart dengan indikator tooltip dan label hari.
  - Filter rentang waktu (*7 Hari, 30 Hari, Semua*).
  - Kartu tips optimasi penghasilan untuk mitra.
- **Tab 4: Profil Mitra & Rekening Pencairan**
  - Identitas mitra affiliate (avatar, nama lengkap, username, nomor telepon).
  - Pengaturan Rekening Bank / E-Wallet (payment_method jsonb) lengkap dengan formulir modal bottom sheet.
  - Riwayat lengkap penarikan dana dengan status badge warna (*Pending*, *Berhasil*, *Ditolak*) beserta catatan admin jika ada.
  - Konfirmasi dialog logout yang aman.

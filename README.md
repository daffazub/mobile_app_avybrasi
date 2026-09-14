# Vybrasi Mobile App ☕

> **Official Mobile Affiliate & Partner Portal for Vybrasi Specialty Roastery**

Aplikasi mobile berbasis **Flutter** yang dirancang khusus untuk mitra affiliate **Vybrasi Specialty Roastery**. Aplikasi ini memungkinkan mitra affiliate memantau performa penjualan produk specialty coffee secara real-time, melacak komisi, mengajukan pencairan saldo (payout), serta mengelola kode dan tautan referral dengan antarmuka modern, elegan, dan intuitif.

---

## ☕ Tentang Aplikasi

Vybrasi Mobile App mengusung konsep visual *Specialty Coffee Culture* dengan palet warna **Espresso Dark**, **Crema Amber**, dan tipografi modern **Plus Jakarta Sans**. Aplikasi terhubung langsung dengan backend **Supabase** secara real-time untuk menjamin integritas data transaksi dan status komisi.

### 🌟 Fitur Utama

- **🔐 Multi-Role Authentication & Gatekeeper**
  - Autentikasi aman melalui Supabase Auth.
  - Role-based access control (`affiliate`, `admin`, `owner`).
  - Fitur lupa kata sandi dengan verifikasi email.

- **📊 Dashboard Kinerja Affiliate**
  - Ringkasan saldo aktif, total komisi terkumpul, total klik/referral, dan total pesanan sukses.
  - Grafik tren komisi interaktif dengan visualisasi data performa.
  - Quick share link & kode referral untuk mempermudah promosi di media sosial.

- **💼 Riwayat Komisi & Transaksi**
  - Pelacakan setiap transaksi yang menggunakan kode referral mitra.
  - Filter status komisi: *Menunggu*, *Disetujui*, *Dicairkan*, dan *Ditolak*.
  - Detail transaksi lengkap (ID order, nominal produk, persentase komisi, tanggal transaksi).

- **💳 Manajemen Pencairan Dana (Payouts)**
  - Pengajuan pencairan komisi (withdrawal) ke rekening bank atau e-wallet mitra.
  - Validasi batas minimum penarikan dan saldo mencukupi.
  - Pelacakan status pencairan (*Pending*, *Diproses*, *Selesai*, *Ditolak*) dengan bukti transfer.

- **👤 Profil & Pengaturan Mitra**
  - Pengaturan informasi profil mitra dan data rekening pencairan.
  - Pengaturan keamanan akun dan opsi keluar sistem (logout).

---

## 🛠️ Tech Stack & Dependencies

- **Framework:** [Flutter](https://flutter.dev/) (SDK ^3.7.0) / Dart
- **Backend / Database:** [Supabase](https://supabase.com/) (PostgreSQL, Row Level Security, Auth, Storage)
- **State Management & Architecture:** Clean Layered Architecture (Presentation, Data, Core)
- **UI & Design System:**
  - Material 3 dengan Kustomisasi Tema *Specialty Roastery* (`AppColors`, `AppTheme`)
  - [Google Fonts (Plus Jakarta Sans)](https://pub.dev/packages/google_fonts)
  - [Cupertino Icons](https://pub.dev/packages/cupertino_icons)
- **Charts & Formatting:**
  - [fl_chart](https://pub.dev/packages/fl_chart) untuk grafik komisi interaktif
  - [intl](https://pub.dev/packages/intl) untuk format mata uang Rupiah (`IDR`) dan tanggal lokal

---

## 📁 Struktur Direktori

```text
avybrasi_flutter/
├── android/                   # Konfigurasi native Android & Gradle
├── ios/                       # Konfigurasi native iOS
├── web/                       # Konfigurasi Flutter Web
├── lib/
│   ├── core/
│   │   ├── constants/         # Palet warna (app_colors.dart) & tema (app_theme.dart)
│   │   ├── services/          # Supabase Client & Service initialization
│   │   └── utils/             # Helper format mata uang Rupiah & tanggal
│   ├── data/
│   │   └── models/            # Data Models (affiliate, payout, profile, transaksi)
│   ├── presentation/
│   │   ├── auth/              # Screen Login, Forgot Password, & Role Gatekeeper
│   │   ├── affiliate/         # Dashboard Affiliate, Tab Transaksi, Komisi, Profil
│   │   └── common/            # Shared widgets (VybrasiLogo, ResponsiveContainer)
│   └── main.dart              # Entry point aplikasi Flutter
├── test/                      # Pengujian unit & widget
└── pubspec.yaml               # Manajemen package dan dependencies
```

---

## 🚀 Panduan Memulai (Getting Started)

### Prasyarat
Pastikan environment lokal telah terpasang:
- **Flutter SDK** (versi >= 3.7.0)
- **Dart SDK**
- **Android Studio** atau **VS Code** (dengan ekstensi Flutter & Dart)
- **Android SDK** / Perangkat fisik dengan USB Debugging aktif

### Instalasi & Menjalankan Aplikasi

1. **Clone Repository**
   ```bash
   git clone https://github.com/daffazub/mobile_app_avybrasi.git
   cd mobile_app_avybrasi
   ```

2. **Masuk ke Direktori Flutter**
   ```bash
   cd avybrasi_flutter
   ```

3. **Install Dependencies**
   ```bash
   flutter pub get
   ```

4. **Konfigurasi Supabase**
   Pastikan kredensial URL dan Anon Key pada berkas:
   `lib/core/services/supabase_service.dart` sudah sesuai dengan proyek Supabase Anda.

5. **Jalankan Aplikasi**
   ```bash
   flutter run
   ```

6. **Build APK Android (Opsional)**
   - Debug APK:
     ```bash
     flutter build apk --debug
     ```
   - Release APK:
     ```bash
     flutter build apk --release
     ```
   Hasil build APK akan berada di folder `avybrasi_flutter/build/app/outputs/flutter-apk/`.

---

## 🗄️ Skema Database (Supabase)

Aplikasi terhubung dengan entitas database Supabase berikut:
- **`profiles`**: Menyimpan identitas pengguna, nama lengkap, role (`affiliate`/`admin`/`owner`), no. telepon, dan avatar.
- **`affiliates`**: Menyimpan data spesifik mitra affiliate, kode referral unik, saldo komisi aktif, dan rekening bank penarikan.
- **`transaksi`**: Mencatat transaksi e-commerce yang terafiliasi beserta persentase dan nilai nominal komisi mitra.
- **`payouts`**: Mencatat riwayat permintaan pencairan saldo komisi, status verifikasi, dan URL bukti transfer admin.

---

## 👥 Kontributor & Lisensi

Dikembangkan untuk ekosistem digital **Vybrasi Specialty Roastery**.  
Hak cipta dilindungi undang-undang.

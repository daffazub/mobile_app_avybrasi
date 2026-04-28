plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")

    // TAMBAHKAN PLUGIN INI AGAR SERIALIZATION BEKERJA
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.vybrasiapp"

    // SUDAH DIPERBAIKI MENJADI 36
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vybrasiapp"
        minSdk = 24

        // SUDAH DIPERBAIKI MENJADI 36
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // FIREBASE
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")

    // GLIDE
    implementation("com.github.bumptech.glide:glide:4.15.1")

    // SUPABASE (Sudah menggunakan jan-tennert yang benar)
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.0")

    // KTOR (Mesin koneksi Supabase)
    implementation("io.ktor:ktor-client-android:2.3.10")

    // SUPABASE
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.0") // Ini untuk Auth
    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.0") // TAMBAHKAN INI UNTUK STORAGE

    // Library untuk menampilkan gambar dari URL Internet
    implementation("io.coil-kt:coil:2.6.0")

    // KOTLINX SERIALIZATION
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // MPAndroidChart untuk Grafik Laporan
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
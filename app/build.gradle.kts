// =============================================
// FILE: app/build.gradle.kts
// VERSI: Final - No Error, Hanya Warning kotlinOptions
// =============================================

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.vybrasiapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.vybrasiapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // =============================================
        // BuildConfig untuk Supabase
        // =============================================
        buildConfigField("String", "SUPABASE_URL", "\"https://YOUR-PROJECT-ID.supabase.co\"")
        buildConfigField("String", "SUPABASE_KEY", "\"YOUR_ANON_KEY_HERE\"")
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
        buildConfig = true
    }

    // Biarkan kotlinOptions (hanya warning, bukan error)
    kotlinOptions {
        jvmTarget = "11"
    }
}

// =============================================
// FORCE compatible version of androidx.browser
// =============================================
configurations.all {
    resolutionStrategy {
        force("androidx.browser:browser:1.8.0")
    }
}

dependencies {
    // =============================================
    // ANDROIDX CORE & LIFECYCLE
    // =============================================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // =============================================
    // RECYCLERVIEW & CARDVIEW (LANGSUNG TANPA libs)
    // =============================================
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // =============================================
    // COMPOSE
    // =============================================
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // =============================================
    // TESTING
    // =============================================
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // =============================================
    // FIREBASE
    // =============================================
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.1")

    // =============================================
    // IMAGE LOADING
    // =============================================
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("io.coil-kt:coil:2.7.0")

    // =============================================
    // SUPABASE SDK 3.5.0
    // =============================================
    val supabase_version = "3.5.0"
    implementation("io.github.jan.supabase:postgrest-kt:$supabase_version")
    implementation("io.github.jan.supabase:auth-kt:$supabase_version")
    implementation("io.github.jan.supabase:storage-kt:$supabase_version")
    implementation("io.github.jan.supabase:realtime-kt:$supabase_version")

    // =============================================
    // KTOR CLIENT
    // =============================================
    implementation("io.ktor:ktor-client-android:3.0.1")
    implementation("io.ktor:ktor-client-core:3.0.1")
    implementation("io.ktor:ktor-client-logging:3.0.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")

    // =============================================
    // KOTLIN SERIALIZATION & COROUTINES
    // =============================================
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // =============================================
    // CHARTS
    // =============================================
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
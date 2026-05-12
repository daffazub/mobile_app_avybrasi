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

        buildConfigField("String", "SUPABASE_URL", "\"https://jrgoxsxvccbcowqqrgxl.supabase.co\"")
        buildConfigField("String", "SUPABASE_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImpyZ294c3h2Y2NiY293cXFyZ3hsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgxMTEyODIsImV4cCI6MjA5MzY4NzI4Mn0.4ZUI4YKsTSOZBGAbb25bQl1n4AX0U_f5GqW1JjoWw6s\"")
    }

    buildTypes {
        debug {}
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

    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.browser:browser:1.8.0")
    }
}

dependencies {
    // CORE
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // UI COMPONENTS
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // COMPOSE
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // TESTING
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // GOOGLE SIGN IN
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // FIREBASE
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")

    // SUPABASE 2.5.0
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // MATERIAL DESIGN
    implementation("com.google.android.material:material:1.9.0")

    // KTOR 2.3.12
    val ktor_v = "2.3.12"
    implementation("io.ktor:ktor-client-okhttp:$ktor_v")
    implementation("io.ktor:ktor-client-core:$ktor_v")
    implementation("io.ktor:ktor-client-logging:$ktor_v")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_v")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_v")

    // SERIALIZATION & COROUTINES
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // IMAGE LOADING (cukup satu versi Glide, gunakan yang 4.16.0)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // annotationProcessor tidak diperlukan di runtime, opsional
    // annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // COIL (opsional, kalau mau pakai selain Glide)
    implementation("io.coil-kt:coil:2.7.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // CHARTS
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
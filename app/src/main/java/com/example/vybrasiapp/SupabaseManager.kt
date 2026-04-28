package com.example.vybrasiapp

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth // untuk SSO
import io.github.jan.supabase.postgrest.Postgrest // untuk Database
import io.github.jan.supabase.storage.Storage // untuk Upload Gambar

object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = "https://dtjvahuoxwtagdwziibh.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR0anZhaHVveHd0YWdkd3ppaWJoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY3NDE1ODcsImV4cCI6MjA5MjMxNzU4N30.a9g_HuWbydLp-N9OAK5bYhPrTXOsxEPDtvEuEfDnJCU"
    ) {
        // Modul Database
        install(Postgrest) {
            defaultSchema = "jualan_kopi"
        }

        // Modul Login (SSO)
        install(Auth)

        // Modul Penyimpanan Gambar
        install(Storage)
    }
}
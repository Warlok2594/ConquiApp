package com.conqui.app

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClient {
    // Usa le stesse credenziali del progetto iOS
    private const val SUPABASE_URL = "https://bssahmmedqnjxbwnlwcd.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzc2FobW1lZHFuanhid25sd2NkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTg4MDA5OTEsImV4cCI6MjA3NDM3Njk5MX0.u0Xkhspvlo-DZiTt2xkZyt-TNjuk5isECwZqTmPlmFg" // Inserisci la tua chiave

    // Configura JSON per essere molto permissivo
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
        allowSpecialFloatingPointValues = true
        allowStructuredMapKeys = true
    }

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        // Imposta il serializer globale
        defaultSerializer = KotlinXSerializer(jsonConfig)

        // Configura Auth
        install(Auth.Companion)

        // Configura Postgrest
        install(Postgrest.Companion)

        // Installa Realtime se necessario
        install(Realtime.Companion)
    }
}
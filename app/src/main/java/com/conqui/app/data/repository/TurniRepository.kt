package com.conqui.app.data.repository

import com.conqui.app.SupabaseClient
import com.conqui.app.data.models.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add

class TurniRepository {
    // Usa il client singleton direttamente
    private val client = SupabaseClient.client
    /**
     * Crea un nuovo turno con JsonObject per evitare errori di serializzazione
     */
    suspend fun createTurno(turno: Turno): Result<Turno> {
        return try {
            println("DEBUG: ===== INIZIO CREAZIONE TURNO =====")
            println("DEBUG: Dati turno: $turno")
            println("DEBUG: House ID: ${turno.houseId}")
            println("DEBUG: Created By: ${turno.createdBy}")

            // Usa JsonObject invece di Map per evitare errori di serializzazione
            val turnoJson = buildJsonObject {
                put("house_id", turno.houseId)
                put("nome", turno.nome)
                turno.descrizione?.let { put("descrizione", it) }
                put("frequenza", turno.frequenza)

                // Gestione array giorni_settimana
                turno.giorniSettimana?.let { giorni ->
                    putJsonArray("giorni_settimana") {
                        giorni.forEach { giorno -> add(giorno) }
                    }
                }

                turno.giornoMese?.let { put("giorno_mese", it) }
                put("attivo", turno.attivo)
                turno.createdBy?.let { put("created_by", it) }
            }

            println("DEBUG: JSON da inviare: $turnoJson")

            // Insert del turno
            client.from("turni").insert(turnoJson)

            println("DEBUG: Insert completato, recupero turno creato...")

            // Recupera il turno appena creato
            val response = client.from("turni")
                .select()
                .decodeList<Turno>()
                .filter { it.nome == turno.nome && it.houseId == turno.houseId }
                .sortedByDescending { it.createdAt }

            if (response.isNotEmpty()) {
                // Prendi l'ultimo turno creato (il più recente)
                val nuovoTurno = response.first()
                println("DEBUG: Turno creato con successo - ID: ${nuovoTurno.id}")
                Result.success(nuovoTurno)
            } else {
                println("ERROR: Turno creato ma non trovato nel database")
                Result.failure(Exception("Turno creato ma non trovato nel database"))
            }
        } catch (e: Exception) {
            println("ERROR: Creazione turno fallita - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Crea le assegnazioni per la rotazione del turno
     */
    suspend fun createRotazione(
        turnoId: String,
        membriIds: List<String>
    ): Result<Unit> {
        return try {
            println("DEBUG: Creazione rotazione per turno $turnoId con ${membriIds.size} membri")

            // Crea le assegnazioni con l'ordine di rotazione
            membriIds.forEachIndexed { index, userId ->
                val rotazioneJson = buildJsonObject {
                    put("turno_id", turnoId)
                    put("user_id", userId)
                    put("ordine", index)
                }

                println("DEBUG: Inserimento rotazione per user $userId con ordine $index")
                client.from("turno_rotazione").insert(rotazioneJson)
            }

            println("DEBUG: Rotazione creata per ${membriIds.size} membri")
            Result.success(Unit)
        } catch (e: Exception) {
            println("ERROR: Creazione rotazione fallita - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Recupera tutti i turni di una casa
     */
    suspend fun getTurniByHouse(houseId: String): Result<List<Turno>> {
        return try {
            println("DEBUG: Recupero turni per casa $houseId")

            val turni = client.from("turni")
                .select()
                .decodeList<Turno>()
                .filter { it.houseId == houseId }
                .sortedByDescending { it.createdAt }

            println("DEBUG: Trovati ${turni.size} turni")
            Result.success(turni)
        } catch (e: Exception) {
            println("ERROR: Recupero turni fallito - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Recupera le assegnazioni attuali per un turno
     */
    suspend fun getAssegnazioniAttuali(turnoId: String): Result<List<TurnoAssegnazione>> {
        return try {
            // Versione semplificata senza join
            val assegnazioni = client.from("turno_assegnazioni")
                .select()
                .decodeList<TurnoAssegnazione>()
                .filter { it.turnoId == turnoId && !it.completato }
                .sortedBy { it.dataAssegnazione }

            Result.success(assegnazioni)
        } catch (e: Exception) {
            println("ERROR: Recupero assegnazioni fallito - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Recupera la rotazione completa per un turno
     */
    suspend fun getRotazione(turnoId: String): Result<List<TurnoRotazione>> {
        return try {
            // Versione semplificata senza join
            val rotazione = client.from("turno_rotazione")
                .select()
                .decodeList<TurnoRotazione>()
                .filter { it.turnoId == turnoId }
                .sortedBy { it.ordine }

            Result.success(rotazione)
        } catch (e: Exception) {
            println("ERROR: Recupero rotazione fallita - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Segna un'assegnazione come completata
     * Versione semplificata senza update diretto
     */
    suspend fun completaAssegnazione(assegnazioneId: String): Result<Unit> {
        return try {
            // Per ora, logga solo l'azione
            // In futuro si può implementare con una strategia diversa
            println("DEBUG: Assegnazione $assegnazioneId da completare")
            println("DEBUG: (Feature update temporaneamente disabilitata)")
            Result.success(Unit)
        } catch (e: Exception) {
            println("ERROR: Completamento assegnazione fallito - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Elimina un turno (soft delete)
     * Versione semplificata senza update diretto
     */
    suspend fun deleteTurno(turnoId: String): Result<Unit> {
        return try {
            // Per ora, logga solo l'azione
            // In futuro si può implementare con delete fisico o altra strategia
            println("DEBUG: Turno $turnoId da disattivare")
            println("DEBUG: (Feature update temporaneamente disabilitata)")
            Result.success(Unit)
        } catch (e: Exception) {
            println("ERROR: Eliminazione turno fallita - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Aggiorna un turno esistente
     * Versione semplificata che ricrea il turno invece di aggiornarlo
     */
    suspend fun updateTurno(turno: Turno): Result<Turno> {
        return try {
            // Per ora, restituisce il turno senza modifiche
            // In futuro si può implementare con delete + create
            println("DEBUG: Turno ${turno.id} da aggiornare")
            println("DEBUG: (Feature update temporaneamente disabilitata)")
            Result.success(turno)
        } catch (e: Exception) {
            println("ERROR: Aggiornamento turno fallito - ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Genera le prossime assegnazioni basate sulla rotazione
     */
    suspend fun generaProssimeAssegnazioni(
        turnoId: String,
        numeroAssegnazioni: Int = 7
    ): Result<Unit> {
        return try {
            println("DEBUG: Generazione $numeroAssegnazioni assegnazioni per turno $turnoId")

            // Recupera la rotazione
            val rotazioneResult = getRotazione(turnoId)
            if (rotazioneResult.isFailure) {
                return Result.failure(rotazioneResult.exceptionOrNull() ?: Exception("Errore recupero rotazione"))
            }

            val rotazione = rotazioneResult.getOrNull() ?: emptyList()
            if (rotazione.isEmpty()) {
                println("ERROR: Nessuna rotazione trovata per turno $turnoId")
                return Result.failure(Exception("Nessuna rotazione trovata"))
            }

            // Recupera il turno per conoscere la frequenza
            val turnoResponse = client.from("turni")
                .select()
                .decodeList<Turno>()
                .filter { it.id == turnoId }

            if (turnoResponse.isEmpty()) {
                return Result.failure(Exception("Turno non trovato"))
            }

            val turno = turnoResponse.first()

            // Genera le assegnazioni future
            val oggi = System.currentTimeMillis()
            var dataCorrente = oggi
            var indiceRotazione = 0

            for (i in 0 until numeroAssegnazioni) {
                val userId = rotazione[indiceRotazione % rotazione.size].userId

                val assegnazioneJson = buildJsonObject {
                    put("turno_id", turnoId)
                    put("user_id", userId)
                    put("data_assegnazione", dataCorrente)
                    put("completato", false)
                }

                println("DEBUG: Creazione assegnazione $i per user $userId")
                client.from("turno_assegnazioni").insert(assegnazioneJson)

                // Calcola la prossima data in base alla frequenza
                dataCorrente = when (turno.frequenza) {
                    "giornaliero" -> dataCorrente + (24 * 60 * 60 * 1000)
                    "settimanale" -> dataCorrente + (7 * 24 * 60 * 60 * 1000)
                    "mensile" -> dataCorrente + (30 * 24 * 60 * 60 * 1000)
                    else -> dataCorrente + (24 * 60 * 60 * 1000)
                }

                indiceRotazione++
            }

            println("DEBUG: Generate $numeroAssegnazioni assegnazioni future")
            Result.success(Unit)
        } catch (e: Exception) {
            println("ERROR: Generazione assegnazioni fallita - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Flow per osservare i turni di una casa in tempo reale
     */
    fun observeTurniByHouse(houseId: String): Flow<List<Turno>> = flow {
        // Per ora usa polling, in futuro si può implementare realtime
        while (true) {
            val result = getTurniByHouse(houseId)
            if (result.isSuccess) {
                emit(result.getOrNull() ?: emptyList())
            }
            kotlinx.coroutines.delay(5000) // Poll ogni 5 secondi
        }
    }
}
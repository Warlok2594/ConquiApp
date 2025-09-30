package com.conqui.app.data.repository

import com.conqui.app.SupabaseClient
import com.conqui.app.data.models.*
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class HouseRepository {
    // Usa il client singleton direttamente
    private val client = SupabaseClient.client
    /**
     * Crea una nuova casa
     */
    suspend fun createHouse(name: String, createdBy: String): Result<House> {
        return try {
            val code = generateHouseCode()

            val houseJson = buildJsonObject {
                put("name", name)
                put("code", code)
                put("created_by", createdBy)
            }

            client.from("houses").insert(houseJson)

            // Recupera la casa appena creata
            val houses = client.from("houses")
                .select()
                .decodeList<House>()
                .filter { it.code == code }

            if (houses.isNotEmpty()) {
                val house = houses.first()

                // Aggiungi il creatore come admin
                val memberJson = buildJsonObject {
                    put("house_id", house.id)
                    put("user_id", createdBy)
                    put("role", "admin")
                }
                client.from("house_members").insert(memberJson)

                Result.success(house)
            } else {
                Result.failure(Exception("Casa creata ma non trovata"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Unisciti a una casa esistente usando il codice
     */
    suspend fun joinHouse(code: String, userId: String): Result<House> {
        return try {
            // Trova la casa con il codice
            val houses = client.from("houses")
                .select()
                .decodeList<House>()
                .filter { it.code == code }

            if (houses.isEmpty()) {
                return Result.failure(Exception("Codice casa non valido"))
            }

            val house = houses.first()

            // Verifica se l'utente è già membro
            val existingMembers = client.from("house_members")
                .select()
                .decodeList<HouseMember>()
                .filter { it.houseId == house.id && it.userId == userId }

            if (existingMembers.isNotEmpty()) {
                return Result.failure(Exception("Sei già membro di questa casa"))
            }

            // Aggiungi come membro
            val memberJson = buildJsonObject {
                put("house_id", house.id)
                put("user_id", userId)
                put("role", "member")
            }
            client.from("house_members").insert(memberJson)

            Result.success(house)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ottieni la casa di un utente
     */
    suspend fun getUserHouse(userId: String): Result<House?> {
        return try {
            // Trova l'appartenenza alla casa
            val memberships = client.from("house_members")
                .select()
                .decodeList<HouseMember>()
                .filter { it.userId == userId }

            if (memberships.isEmpty()) {
                return Result.success(null)
            }

            val membership = memberships.first()

            // Ottieni i dettagli della casa
            val houses = client.from("houses")
                .select()
                .decodeList<House>()
                .filter { it.id == membership.houseId }

            if (houses.isNotEmpty()) {
                Result.success(houses.first())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ottieni i membri di una casa
     */
    suspend fun getHouseMembers(houseId: String): Result<List<User>> {
        return try {
            // Ottieni le membership
            val memberships = client.from("house_members")
                .select()
                .decodeList<HouseMember>()
                .filter { it.houseId == houseId }

            // Ottieni i profili degli utenti
            val userIds = memberships.map { it.userId }
            val profiles = client.from("profiles")
                .select()
                .decodeList<Profile>()
                .filter { it.userId in userIds }

            // Converti Profile in User
            val users = profiles.map { profile ->
                User(
                    id = profile.userId,
                    email = profile.email ?: "",
                    username = profile.username ?: "Utente"
                )
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lascia una casa
     */
    suspend fun leaveHouse(houseId: String, userId: String): Result<Unit> {
        return try {
            // Per ora solo log, in futuro implementare delete
            println("DEBUG: Utente $userId lascia casa $houseId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Genera un codice casa univoco di 6 caratteri
     */
    private fun generateHouseCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
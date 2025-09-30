package com.conqui.app.data.repository

import com.conqui.app.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {
    // Usa il client singleton direttamente
    private val client = SupabaseClient.client

    /**
     * Registra un nuovo utente
     */
    suspend fun signUp(email: String, password: String, username: String): Result<String> {
        return try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("username", username)
                }
            }
            Result.success("Registrazione completata! Controlla la tua email per verificare l'account.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login utente esistente
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success("Login effettuato con successo!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout utente
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Controlla se l'utente è autenticato
     */
    fun isUserAuthenticated(): Boolean {
        return client.auth.currentSessionOrNull() != null
    }

    /**
     * Ottieni l'ID dell'utente corrente
     */
    fun getCurrentUserId(): String? {
        return client.auth.currentUserOrNull()?.id
    }

    /**
     * Ottieni l'email dell'utente corrente
     */
    fun getCurrentUserEmail(): String? {
        return client.auth.currentUserOrNull()?.email
    }

    /**
     * Flow per osservare lo stato di autenticazione
     */
    fun observeAuthState(): Flow<Boolean> = flow {
        emit(isUserAuthenticated())
        // In futuro si può implementare un listener real-time
    }
}
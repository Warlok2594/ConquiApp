package com.conqui.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conqui.app.SupabaseClient
import com.conqui.app.data.repository.AuthRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUserEmail: String? = null,
    val currentUserId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoginMode: Boolean = true // true = login, false = register
)

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val supabaseClient = SupabaseClient.client

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * Controlla lo stato di autenticazione all'avvio
     */
    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                val session = supabaseClient.auth.currentSessionOrNull()
                if (session != null) {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        currentUserEmail = session.user?.email,
                        currentUserId = session.user?.id
                    )
                }
            } catch (e: Exception) {
                // Ignora errori di controllo sessione all'avvio
            }
        }
    }

    /**
     * Login utente
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            val result = authRepository.signIn(email, password)

            if (result.isSuccess) {
                val session = supabaseClient.auth.currentSessionOrNull()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    currentUserEmail = session?.user?.email,
                    currentUserId = session?.user?.id,
                    successMessage = result.getOrNull(),
                    errorMessage = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Errore di login",
                    successMessage = null
                )
            }
        }
    }

    /**
     * Registrazione nuovo utente
     */
    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            val result = authRepository.signUp(email, password, username)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = result.getOrNull(),
                    errorMessage = null,
                    isLoginMode = true // Torna alla modalità login dopo registrazione
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Errore di registrazione",
                    successMessage = null
                )
            }
        }
    }

    /**
     * Logout utente
     */
    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = authRepository.signOut()

            if (result.isSuccess) {
                _uiState.value = AuthUiState() // Reset completo dello stato
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Errore durante il logout"
                )
            }
        }
    }

    /**
     * Cambia modalità tra login e registrazione
     */
    fun toggleAuthMode() {
        _uiState.value = _uiState.value.copy(
            isLoginMode = !_uiState.value.isLoginMode,
            errorMessage = null,
            successMessage = null
        )
    }

    /**
     * Pulisci messaggi di errore/successo
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    /**
     * Ottieni l'ID dell'utente corrente
     */
    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }

    /**
     * Controlla se l'utente è autenticato
     */
    fun isAuthenticated(): Boolean {
        return authRepository.isUserAuthenticated()
    }
}
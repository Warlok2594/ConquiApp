package com.conqui.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conqui.app.SupabaseClient
import com.conqui.app.data.models.House
import com.conqui.app.data.models.User
import com.conqui.app.data.repository.AuthRepository
import com.conqui.app.data.repository.HouseRepository
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel per la gestione della casa e dei suoi membri
 */
class HouseViewModel : ViewModel() {
    // Repository instances
    private val authRepository = AuthRepository()
    private val houseRepository = HouseRepository()

    // State flows
    private val _currentHouse = MutableStateFlow<House?>(null)
    val currentHouse: StateFlow<House?> = _currentHouse

    private val _members = MutableStateFlow<List<User>>(emptyList())
    val members: StateFlow<List<User>> = _members  // Esplicitamente List<User>

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Callback per il logout
    private var logoutCallback: (() -> Unit)? = null

    init {
        loadUserHouse()
    }

    /**
     * Imposta il callback da chiamare dopo il logout
     */
    fun setLogoutCallback(callback: () -> Unit) {
        logoutCallback = callback
    }

    /**
     * Carica la casa dell'utente corrente
     */
    fun loadUserHouse() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    houseRepository.getUserHouse(userId).fold(
                        onSuccess = { house ->
                            _currentHouse.value = house
                            house?.let {
                                loadMembers(it.id)
                            }
                        },
                        onFailure = { exception ->
                            _error.value = exception.message
                        }
                    )
                } else {
                    _error.value = "Utente non autenticato"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crea una nuova casa
     */
    fun createHouse(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    houseRepository.createHouse(name, userId).fold(
                        onSuccess = { house ->
                            _currentHouse.value = house
                            loadMembers(house.id)
                        },
                        onFailure = { exception ->
                            _error.value = exception.message
                        }
                    )
                } else {
                    _error.value = "Utente non autenticato"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Unisciti a una casa esistente usando il codice
     */
    fun joinHouse(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    houseRepository.joinHouse(code, userId).fold(
                        onSuccess = { house ->
                            _currentHouse.value = house
                            loadMembers(house.id)
                        },
                        onFailure = { exception ->
                            _error.value = exception.message
                        }
                    )
                } else {
                    _error.value = "Utente non autenticato"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carica i membri della casa
     */
    private fun loadMembers(houseId: String) {
        viewModelScope.launch {
            try {
                houseRepository.getHouseMembers(houseId).fold(
                    onSuccess = { users ->
                        _members.value = users
                    },
                    onFailure = { exception ->
                        _error.value = "Errore nel caricamento dei membri: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                _error.value = "Errore nel caricamento dei membri: ${e.message}"
            }
        }
    }

    /**
     * Esegui il logout
     */
    fun performLogout() {
        viewModelScope.launch {
            try {
                // Usa direttamente il client Supabase per il logout
                SupabaseClient.client.auth.signOut()
                // Chiama il callback se impostato
                logoutCallback?.invoke()
            } catch (e: Exception) {
                _error.value = "Errore durante il logout: ${e.message}"
            }
        }
    }

    /**
     * Pulisci l'errore corrente
     */
    fun clearError() {
        _error.value = null
    }
}
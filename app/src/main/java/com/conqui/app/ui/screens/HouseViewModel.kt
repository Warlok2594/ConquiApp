package com.conqui.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conqui.app.data.models.House
import com.conqui.app.data.models.User
import com.conqui.app.data.repository.AuthRepository
import com.conqui.app.data.repository.HouseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HouseUiState(
    val currentHouse: House? = null,
    val members: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class HouseViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val houseRepository = HouseRepository()

    private val _uiState = MutableStateFlow(HouseUiState())
    val uiState: StateFlow<HouseUiState> = _uiState.asStateFlow()

    init {
        loadHouseData()
        fun logout() {
            viewModelScope.launch {
                try {
                    authRepository.signOut()
                    // Reset dello stato
                    _uiState.value = HouseUiState()
                } catch (e: Exception) {
                    println("ERROR: Logout fallito - ${e.message}")
                }
            }
        }

        private fun loadHouseData() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    val houseResult = houseRepository.getUserHouse(userId)
                    if (houseResult.isSuccess) {
                        val house = houseResult.getOrNull()
                        if (house != null) {
                            // Carica anche i membri
                            val membersResult = houseRepository.getHouseMembers(house.id)
                            val members = if (membersResult.isSuccess) {
                                membersResult.getOrNull() ?: emptyList()
                            } else {
                                emptyList()
                            }

                            _uiState.value = _uiState.value.copy(
                                currentHouse = house,
                                members = members,
                                isLoading = false
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                currentHouse = null,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Errore caricamento casa",
                            isLoading = false
                        )
                    }
                }
            }
        }

        fun createHouse(name: String) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    val result = houseRepository.createHouse(name, userId)
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Casa creata con successo!",
                            isLoading = false
                        )
                        loadHouseData() // Ricarica i dati
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.exceptionOrNull()?.message ?: "Errore creazione casa",
                            isLoading = false
                        )
                    }
                }
            }
        }

        fun joinHouse(code: String) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    val result = houseRepository.joinHouse(code, userId)
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Ti sei unito alla casa!",
                            isLoading = false
                        )
                        loadHouseData() // Ricarica i dati
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = result.exceptionOrNull()?.message ?: "Codice non valido",
                            isLoading = false
                        )
                    }
                }
            }
        }

        fun leaveHouse() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val userId = authRepository.getCurrentUserId()
                val houseId = _uiState.value.currentHouse?.id

                if (userId != null && houseId != null) {
                    val result = houseRepository.leaveHouse(houseId, userId)
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            currentHouse = null,
                            members = emptyList(),
                            successMessage = "Hai lasciato la casa",
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Errore nel lasciare la casa",
                            isLoading = false
                        )
                    }
                }
            }
        }

        fun logout() {
            viewModelScope.launch {
                try {
                    authRepository.signOut()
                    // Reset dello stato
                    _uiState.value = HouseUiState()
                } catch (e: Exception) {
                    println("ERROR: Logout fallito - ${e.message}")
                }
            }
        }
    }
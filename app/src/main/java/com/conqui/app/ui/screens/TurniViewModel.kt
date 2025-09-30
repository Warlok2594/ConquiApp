package com.conqui.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conqui.app.data.models.Turno
import com.conqui.app.data.models.User
import com.conqui.app.data.repository.AuthRepository
import com.conqui.app.data.repository.HouseRepository
import com.conqui.app.data.repository.TurniRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TurniUiState(
    val turni: List<Turno> = emptyList(),
    val membri: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentHouseId: String? = null,
    val currentUserId: String? = null,
    val showCreateDialog: Boolean = false,
    val isCreating: Boolean = false
)

class TurniViewModel : ViewModel() {
    // Inizializza i repository senza parametri
    private val authRepository = AuthRepository()
    private val houseRepository = HouseRepository()
    private val turniRepository = TurniRepository()

    private val _uiState = MutableStateFlow(TurniUiState())
    val uiState: StateFlow<TurniUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Ottieni l'ID dell'utente corrente
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    _uiState.value = _uiState.value.copy(currentUserId = userId)

                    // Ottieni la casa dell'utente
                    val houseResult = houseRepository.getUserHouse(userId)
                    if (houseResult.isSuccess) {
                        val house = houseResult.getOrNull()
                        if (house != null) {
                            _uiState.value = _uiState.value.copy(currentHouseId = house.id)

                            // Carica i membri della casa
                            loadHouseMembers(house.id)

                            // Carica i turni della casa
                            loadTurni(house.id)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Errore caricamento dati: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun loadHouseMembers(houseId: String) {
        val membersResult = houseRepository.getHouseMembers(houseId)
        if (membersResult.isSuccess) {
            val members = membersResult.getOrNull() ?: emptyList()
            _uiState.value = _uiState.value.copy(membri = members)
            println("DEBUG: Caricati ${members.size} membri dalla casa")
        }
    }

    private suspend fun loadTurni(houseId: String) {
        val turniResult = turniRepository.getTurniByHouse(houseId)
        if (turniResult.isSuccess) {
            val turni = turniResult.getOrNull() ?: emptyList()
            _uiState.value = _uiState.value.copy(
                turni = turni,
                isLoading = false
            )
            println("DEBUG: Caricati ${turni.size} turni")
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Errore caricamento turni",
                isLoading = false
            )
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = false,
            errorMessage = null
        )
    }

    fun createTurno(
        nome: String,
        descrizione: String,
        frequenza: String,
        membriSelezionati: List<String>,
        giorniSettimana: List<Int>? = null,
        giornoMese: Int? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreating = true, errorMessage = null)

            val houseId = _uiState.value.currentHouseId
            val userId = _uiState.value.currentUserId

            if (houseId == null || userId == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Errore: dati utente mancanti",
                    isCreating = false
                )
                return@launch
            }

            if (membriSelezionati.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Seleziona almeno un membro",
                    isCreating = false
                )
                return@launch
            }

            try {
                // Crea il turno
                val turno = Turno(
                    id = "", // Verrà generato dal database
                    houseId = houseId,
                    nome = nome,
                    descrizione = descrizione.ifEmpty { null },
                    frequenza = frequenza,
                    giorniSettimana = giorniSettimana,
                    giornoMese = giornoMese,
                    attivo = true,
                    createdBy = userId,
                    createdAt = System.currentTimeMillis()
                )

                println("DEBUG: Creando turno: $turno")
                println("DEBUG: Membri selezionati: $membriSelezionati")

                val result = turniRepository.createTurno(turno)

                if (result.isSuccess) {
                    val nuovoTurno = result.getOrNull()!!
                    println("DEBUG: Turno creato con successo, ID: ${nuovoTurno.id}")

                    // Crea la rotazione
                    val rotazioneResult = turniRepository.createRotazione(
                        turnoId = nuovoTurno.id,
                        membriIds = membriSelezionati
                    )

                    if (rotazioneResult.isSuccess) {
                        println("DEBUG: Rotazione creata con successo")

                        // Genera le prime assegnazioni
                        turniRepository.generaProssimeAssegnazioni(nuovoTurno.id, 7)

                        // Ricarica i turni
                        loadTurni(houseId)

                        // Chiudi il dialog
                        hideCreateDialog()

                        _uiState.value = _uiState.value.copy(
                            isCreating = false,
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Errore creazione rotazione: ${rotazioneResult.exceptionOrNull()?.message}",
                            isCreating = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Errore creazione turno: ${result.exceptionOrNull()?.message}",
                        isCreating = false
                    )
                }
            } catch (e: Exception) {
                println("ERROR: Eccezione durante creazione turno: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Errore: ${e.message}",
                    isCreating = false
                )
            }
        }
    }

    fun deleteTurno(turnoId: String) {
        viewModelScope.launch {
            val result = turniRepository.deleteTurno(turnoId)
            if (result.isSuccess) {
                _uiState.value.currentHouseId?.let { loadTurni(it) }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Errore eliminazione turno"
                )
            }
        }
    }

    fun refreshTurni() {
        viewModelScope.launch {
            _uiState.value.currentHouseId?.let { houseId ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                loadTurni(houseId)
            }
        }
    }
}
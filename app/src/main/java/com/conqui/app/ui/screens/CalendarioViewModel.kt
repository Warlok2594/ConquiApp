package com.conqui.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conqui.app.data.models.Turno
import com.conqui.app.data.models.TurnoAssegnazione
import com.conqui.app.data.models.User
import com.conqui.app.data.repository.AuthRepository
import com.conqui.app.data.repository.HouseRepository
import com.conqui.app.data.repository.TurniRepository
import com.conqui.app.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CalendarioUiState(
    val turni: List<Turno> = emptyList(),
    val assegnazioni: Map<String, List<TurnoAssegnazione>> = emptyMap(), // turnoId -> assegnazioni
    val membri: Map<String, User> = emptyMap(), // userId -> User
    val meseCorrente: YearMonth = YearMonth.now(),
    val giornoSelezionato: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CalendarioViewModel : ViewModel() {
    // Inizializza i repository senza parametri
    private val authRepository = AuthRepository()
    private val houseRepository = HouseRepository()
    private val turniRepository = TurniRepository()

    private val _uiState = MutableStateFlow(CalendarioUiState())
    val uiState: StateFlow<CalendarioUiState> = _uiState.asStateFlow()

    init {
        loadCalendarioData()
    }

    private fun loadCalendarioData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Ottieni l'ID dell'utente corrente
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    // Ottieni la casa dell'utente
                    val houseResult = houseRepository.getUserHouse(userId)
                    if (houseResult.isSuccess) {
                        val house = houseResult.getOrNull()
                        if (house != null) {
                            // Carica i turni della casa
                            val turniResult = turniRepository.getTurniByHouse(house.id)
                            if (turniResult.isSuccess) {
                                val turni = turniResult.getOrNull() ?: emptyList()

                                // Carica le assegnazioni per ogni turno
                                val assegnazioniMap = mutableMapOf<String, List<TurnoAssegnazione>>()
                                turni.forEach { turno ->
                                    val assegnazioniResult = turniRepository.getAssegnazioniAttuali(turno.id)
                                    if (assegnazioniResult.isSuccess) {
                                        assegnazioniMap[turno.id] = assegnazioniResult.getOrNull() ?: emptyList()
                                    }
                                }

                                // Carica i membri della casa
                                val membriResult = houseRepository.getHouseMembers(house.id)
                                val membri = if (membriResult.isSuccess) {
                                    membriResult.getOrNull()?.associateBy { it.id } ?: emptyMap()
                                } else {
                                    emptyMap()
                                }

                                _uiState.value = _uiState.value.copy(
                                    turni = turni,
                                    assegnazioni = assegnazioniMap,
                                    membri = membri,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Errore caricamento calendario: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun cambioMese(nuovoMese: YearMonth) {
        _uiState.value = _uiState.value.copy(meseCorrente = nuovoMese)
        // Ricarica le assegnazioni per il nuovo mese se necessario
        loadCalendarioData()
    }

    fun selezionaGiorno(giorno: LocalDate) {
        _uiState.value = _uiState.value.copy(
            giornoSelezionato = if (_uiState.value.giornoSelezionato == giorno) null else giorno
        )
    }

    fun completaAssegnazione(assegnazioneId: String) {
        viewModelScope.launch {
            val result = turniRepository.completaAssegnazione(assegnazioneId)
            if (result.isSuccess) {
                // Ricarica i dati
                loadCalendarioData()
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Errore completamento assegnazione"
                )
            }
        }
    }

    fun getAssegnazioniPerGiorno(giorno: LocalDate): List<Pair<Turno, TurnoAssegnazione>> {
        val risultato = mutableListOf<Pair<Turno, TurnoAssegnazione>>()

        _uiState.value.turni.forEach { turno ->
            _uiState.value.assegnazioni[turno.id]?.forEach { assegnazione ->
                // Converti la data dell'assegnazione in LocalDate
                val dataAssegnazione = java.time.Instant.ofEpochMilli(assegnazione.dataAssegnazione)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()

                if (dataAssegnazione == giorno) {
                    risultato.add(turno to assegnazione)
                }
            }
        }

        return risultato
    }

    fun getNomeMembro(userId: String): String {
        return _uiState.value.membri[userId]?.username ?: "Utente"
    }

    fun mesePrecedente() {
        _uiState.value = _uiState.value.copy(
            meseCorrente = _uiState.value.meseCorrente.minusMonths(1)
        )
        loadCalendarioData()
    }

    fun meseSuccessivo() {
        _uiState.value = _uiState.value.copy(
            meseCorrente = _uiState.value.meseCorrente.plusMonths(1)
        )
        loadCalendarioData()
    }
}
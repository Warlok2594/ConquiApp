package com.conqui.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarioTurniScreen(
    houseId: String,
    currentUserId: String,
    viewModel: CalendarioViewModel = viewModel()
) {
    // Lo stato viene gestito automaticamente dal ViewModel attraverso l'init block
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con navigazione mese
        HeaderCalendario(
            meseCorrente = uiState.meseCorrente,
            onPreviousMonth = { viewModel.mesePrecedente() },
            onNextMonth = { viewModel.meseSuccessivo() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Giorni della settimana
        GiorniSettimana()

        Spacer(modifier = Modifier.height(8.dp))

        // Griglia del calendario
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            CalendarioGriglia(
                meseCorrente = uiState.meseCorrente,
                giornoSelezionato = uiState.giornoSelezionato,
                onDayClick = { viewModel.selezionaGiorno(it) },
                getAssegnazioniPerGiorno = { viewModel.getAssegnazioniPerGiorno(it) }
            )
        }

        // Dettagli giorno selezionato
        uiState.giornoSelezionato?.let { giorno ->
            Spacer(modifier = Modifier.height(16.dp))
            DettagliGiorno(
                giorno = giorno,
                assegnazioni = viewModel.getAssegnazioniPerGiorno(giorno),
                getNomeMembro = { viewModel.getNomeMembro(it) },
                onCompleteTask = { viewModel.completaAssegnazione(it) }
            )
        }

        // Messaggio di errore
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun HeaderCalendario(
    meseCorrente: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Mese precedente"
            )
        }

        Text(
            text = meseCorrente.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ITALIAN)),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Mese successivo"
            )
        }
    }
}

@Composable
fun GiorniSettimana() {
    val giorni = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        giorni.forEach { giorno ->
            Text(
                text = giorno,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CalendarioGriglia(
    meseCorrente: YearMonth,
    giornoSelezionato: LocalDate?,
    onDayClick: (LocalDate) -> Unit,
    getAssegnazioniPerGiorno: (LocalDate) -> List<Pair<com.conqui.app.data.models.Turno, com.conqui.app.data.models.TurnoAssegnazione>>
) {
    val primoGiornoDelMese = meseCorrente.atDay(1)
    val ultimoGiornoDelMese = meseCorrente.atEndOfMonth()
    val primoGiornoSettimana = primoGiornoDelMese.dayOfWeek.value
    val giorniVuotiInizio = if (primoGiornoSettimana == 7) 6 else primoGiornoSettimana - 1
    val giorniTotali = giorniVuotiInizio + meseCorrente.lengthOfMonth()
    val righe = (giorniTotali + 6) / 7
    val oggi = LocalDate.now()

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(righe * 7) { index ->
            val giornoNumero = index - giorniVuotiInizio + 1

            if (giornoNumero in 1..meseCorrente.lengthOfMonth()) {
                val data = meseCorrente.atDay(giornoNumero)
                val assegnazioni = getAssegnazioniPerGiorno(data)
                val isOggi = data == oggi
                val isSelezionato = data == giornoSelezionato
                val hasTurni = assegnazioni.isNotEmpty()

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            when {
                                isSelezionato -> MaterialTheme.colorScheme.primaryContainer
                                isOggi -> MaterialTheme.colorScheme.secondaryContainer
                                hasTurni -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = if (isOggi) 2.dp else 1.dp,
                            color = if (isOggi) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                        )
                        .clickable { onDayClick(data) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = giornoNumero.toString(),
                            fontWeight = if (isOggi) FontWeight.Bold else FontWeight.Normal
                        )
                        if (hasTurni) {
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
fun DettagliGiorno(
    giorno: LocalDate,
    assegnazioni: List<Pair<com.conqui.app.data.models.Turno, com.conqui.app.data.models.TurnoAssegnazione>>,
    getNomeMembro: (String) -> String,
    onCompleteTask: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Turni del ${giorno.format(DateTimeFormatter.ofPattern("d MMMM", Locale.ITALIAN))}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (assegnazioni.isEmpty()) {
                Text(
                    text = "Nessun turno per questo giorno",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                assegnazioni.forEach { (turno, assegnazione) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = turno.nome,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = getNomeMembro(assegnazione.userId),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!assegnazione.completato) {
                            Button(
                                onClick = { onCompleteTask(assegnazione.id) },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Completa")
                            }
                        } else {
                            Text(
                                text = "✓",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                        }
                    }

                    if (assegnazioni.last() != turno to assegnazione) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
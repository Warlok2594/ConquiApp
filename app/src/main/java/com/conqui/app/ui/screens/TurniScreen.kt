package com.conqui.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurniScreen(
    viewModel: TurniViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.showCreateDialog() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Crea turno")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab per Lista/Calendario
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Lista") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Calendario") },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                )
            }

            // Contenuto basato sul tab selezionato
            when (selectedTab) {
                0 -> {
                    // Vista lista turni
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.turni.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Nessun turno creato",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Clicca + per crearne uno",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.turni) { turno ->
                                TurnoCard(
                                    turno = turno,
                                    onDelete = { viewModel.deleteTurno(turno.id) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Vista calendario con parametri corretti
                    val currentHouseId = uiState.currentHouseId ?: ""
                    val currentUserId = uiState.currentUserId ?: ""

                    CalendarioTurniScreen(
                        houseId = currentHouseId,
                        currentUserId = currentUserId
                    )
                }
            }
        }

        // Messaggio di errore
        uiState.errorMessage?.let { error ->
            SnackbarHost(
                hostState = remember { SnackbarHostState() }
            ) {
                LaunchedEffect(error) {
                    SnackbarResult.Dismissed
                }
            }
        }
    }

    // Dialog creazione turno
    if (uiState.showCreateDialog) {
        CreateTurnoDialog(
            membri = uiState.membri,
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { nome, descrizione, frequenza, membriSelezionati ->
                viewModel.createTurno(nome, descrizione, frequenza, membriSelezionati)
            },
            isCreating = uiState.isCreating
        )
    }
}

@Composable
fun TurnoCard(
    turno: com.conqui.app.data.models.Turno,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = turno.nome,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                turno.descrizione?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Frequenza: ${turno.frequenza}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Elimina",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTurnoDialog(
    membri: List<com.conqui.app.data.models.User>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, List<String>) -> Unit,
    isCreating: Boolean
) {
    var nome by remember { mutableStateOf("") }
    var descrizione by remember { mutableStateOf("") }
    var frequenza by remember { mutableStateOf("settimanale") }
    val membriSelezionati = remember { mutableStateListOf<String>() }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crea nuovo turno") },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome turno") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descrizione,
                    onValueChange = { descrizione = it },
                    label = { Text("Descrizione (opzionale)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown frequenza
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = frequenza,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequenza") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Giornaliero") },
                            onClick = {
                                frequenza = "giornaliero"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settimanale") },
                            onClick = {
                                frequenza = "settimanale"
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mensile") },
                            onClick = {
                                frequenza = "mensile"
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Membri in rotazione:",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Lista membri con checkbox
                Column {
                    membri.forEach { membro ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = membriSelezionati.contains(membro.id),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        membriSelezionati.add(membro.id)
                                    } else {
                                        membriSelezionati.remove(membro.id)
                                    }
                                }
                            )
                            Text(
                                text = membro.username,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                if (membriSelezionati.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ordine rotazione: ${membriSelezionati.size} membri selezionati",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(nome, descrizione, frequenza, membriSelezionati.toList())
                },
                enabled = nome.isNotEmpty() && membriSelezionati.isNotEmpty() && !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crea")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating
            ) {
                Text("Annulla")
            }
        }
    )
}
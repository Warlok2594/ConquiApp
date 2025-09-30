package com.conqui.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HouseScreen(
    viewModel: HouseViewModel = viewModel(),
    onLogout: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var houseName by remember { mutableStateOf("") }
    var houseCode by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.currentHouse == null) {
            // Nessuna casa - mostra opzioni per creare o unirsi
            Text(
                text = "Benvenuto in ConquiApp!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Non fai ancora parte di nessuna casa",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crea una nuova casa")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showJoinDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unisciti a una casa esistente")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pulsante logout temporaneo per testing
            TextButton(
                onClick = {
                    viewModel.performLogout()
                    onLogout?.invoke()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout (debug)", color = MaterialTheme.colorScheme.error)
            }
        } else {
            // Ha una casa - mostra dettagli
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "La tua casa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.currentHouse!!.name,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Codice: ${uiState.currentHouse!!.code}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Membri: ${uiState.members.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista membri
                    if (uiState.members.isNotEmpty()) {
                        Column {
                            uiState.members.forEach { member ->
                                Text(
                                    text = "• ${member.username}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.leaveHouse() }
                        ) {
                            Text("Lascia casa")
                        }

                        // Logout anche quando hai una casa
                        TextButton(
                            onClick = {
                                viewModel.performLogout()
                                onLogout?.invoke()
                            }
                        ) {
                            Text("Logout", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }

    // Dialog per creare casa
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crea nuova casa") },
            text = {
                Column {
                    OutlinedTextField(
                        value = houseName,
                        onValueChange = { houseName = it },
                        label = { Text("Nome casa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createHouse(houseName)
                        showCreateDialog = false
                        houseName = ""
                    },
                    enabled = houseName.isNotEmpty() && !uiState.isLoading
                ) {
                    Text("Crea")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        houseName = ""
                    }
                ) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog per unirsi a casa
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Unisciti a una casa") },
            text = {
                Column {
                    OutlinedTextField(
                        value = houseCode,
                        onValueChange = { houseCode = it.uppercase() },
                        label = { Text("Codice casa (6 caratteri)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.joinHouse(houseCode)
                        showJoinDialog = false
                        houseCode = ""
                    },
                    enabled = houseCode.length == 6 && !uiState.isLoading
                ) {
                    Text("Unisciti")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showJoinDialog = false
                        houseCode = ""
                    }
                ) {
                    Text("Annulla")
                }
            }
        )
    }
}
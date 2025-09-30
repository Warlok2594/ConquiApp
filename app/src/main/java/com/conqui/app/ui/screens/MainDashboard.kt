package com.conqui.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.conqui.app.SupabaseClient
import com.conqui.app.data.repository.AuthRepository
import io.github.jan.supabase.gotrue.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    onLogout: () -> Unit,
    houseViewModel: HouseViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val houseUiState by houseViewModel.uiState.collectAsState()

    // Se l'utente fa logout, naviga alla schermata login
    LaunchedEffect(Unit) {
        houseViewModel.setLogoutCallback(onLogout)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ConquiApp",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Casa") },
                    label = { Text("Casa") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Turni") },
                    label = { Text("Turni") },
                    enabled = houseUiState.currentHouse != null
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Calendario") },
                    label = { Text("Calendario") },
                    enabled = houseUiState.currentHouse != null
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HouseScreen()
                1 -> {
                    if (houseUiState.currentHouse != null) {
                        TurniScreen()
                    } else {
                        NoCasaMessage()
                    }
                }
                2 -> {
                    if (houseUiState.currentHouse != null) {
                        val authRepository = AuthRepository()
                        val currentUserId = authRepository.getCurrentUserId() ?: ""
                        CalendarioTurniScreen(
                            houseId = houseUiState.currentHouse!!.id,
                            currentUserId = currentUserId
                        )
                    } else {
                        NoCasaMessage()
                    }
                }
            }
        }
    }
}

@Composable
fun NoCasaMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Devi prima creare o unirti a una casa",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Vai alla sezione Casa per iniziare",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
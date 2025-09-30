package com.conqui.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List

@Composable
fun MainDashboard(
    navController: NavHostController
) {
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Crea il ViewModel qui per condividerlo tra le schermate
    val houseViewModel: HouseViewModel = viewModel()

    // Imposta il callback per il logout
    LaunchedEffect(Unit) {
        houseViewModel.setLogoutCallback {
            navController.navigate("login") {
                popUpTo("main") { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Casa") },
                    label = { Text("Casa") },
                    selected = currentRoute == "house",
                    onClick = {
                        mainNavController.navigate("house") {
                            popUpTo("house") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Turni") },
                    label = { Text("Turni") },
                    selected = currentRoute == "turni",
                    onClick = {
                        mainNavController.navigate("turni") {
                            popUpTo("house") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = "Calendario") },
                    label = { Text("Calendario") },
                    selected = currentRoute == "calendario",
                    onClick = {
                        mainNavController.navigate("calendario") {
                            popUpTo("house") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = "house",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("house") {
                HouseScreen(
                    viewModel = houseViewModel,
                    onNavigateToTurni = {
                        mainNavController.navigate("turni")
                    },
                    onNavigateToCalendario = {
                        mainNavController.navigate("calendario")
                    }
                )
            }
            composable("turni") {
                TurniScreen(
                    onNavigateBack = {
                        mainNavController.navigateUp()
                    }
                )
            }
            composable("calendario") {
                CalendarioTurniScreen(
                    onNavigateBack = {
                        mainNavController.navigateUp()
                    }
                )
            }
        }
    }
}
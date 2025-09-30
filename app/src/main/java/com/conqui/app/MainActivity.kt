package com.conqui.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.conqui.app.ui.screens.*
import com.conqui.app.ui.theme.ConquiAppTheme
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConquiAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConquiApp()
                }
            }
        }
    }
}

@Composable
fun ConquiApp() {
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf("login") }

    // Controlla se l'utente è già autenticato
    LaunchedEffect(Unit) {
        val isAuthenticated = SupabaseClient.client.auth.currentSessionOrNull() != null
        startDestination = if (isAuthenticated) "main" else "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Schermata di login (gestisce anche registrazione)
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Schermata principale con bottom navigation
        composable("main") {
            MainDashboard(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        // Schermata gestione casa
        composable("house") {
            HouseScreen()
        }

        // Schermata turni
        composable("turni") {
            TurniScreen()
        }

        // Schermata calendario
        composable("calendario") {
            val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
            CalendarioTurniScreen(
                houseId = "", // Verrà caricato dal ViewModel
                currentUserId = currentUserId
            )
        }
    }
}
package com.example.rinconsur.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rinconsur.ui.screens.LoginScreen

@Composable
fun AppNavigation(
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Rutas.Login.ruta
    ) {
        composable(Rutas.Login.ruta) {
            LoginScreen(
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onIngresoObservador = {
                    navController.navegarDesdeLogin(
                        destino = Rutas.MenuPrincipal.ruta
                    )
                },
                onIngresoSupervisor = {
                    navController.navegarDesdeLogin(
                        destino = Rutas.MenuPrincipal.ruta
                    )
                }
            )
        }

        composable(Rutas.MenuPrincipal.ruta) {
            // Reemplaza este Text por tu pantalla principal.
            Text(text = "Menú principal")
        }
    }
}

private fun NavHostController.navegarDesdeLogin(
    destino: String
) {
    navigate(destino) {
        popUpTo(Rutas.Login.ruta) {
            inclusive = true
        }

        launchSingleTop = true
    }
}
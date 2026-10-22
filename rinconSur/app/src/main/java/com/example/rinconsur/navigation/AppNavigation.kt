package com.example.rinconsur.navigation

import androidx.compose.runtime.Composable
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
                onLoginCorrecto = {
                    navController.navigate(
                        Rutas.MenuPrincipal.ruta
                    ) {
                        popUpTo(Rutas.Login.ruta) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
package com.example.rinconsur.navigation


import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.ui.screens.LoginScreen
import com.example.rinconsur.ui.screens.MenuPrincipalScreen
import com.example.rinconsur.ui.screens.PerfilScreen
import com.example.rinconsur.ui.screens.RecuperarContrasenaScreen

@Composable
fun AppNavigation(
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit
) {
    val navController = rememberNavController()
    var rolActivo by rememberSaveable {
        mutableStateOf(RolUsuario.OBSERVADOR)
    }
    var correoLogueado by rememberSaveable {
        mutableStateOf("observador@rinconsur.cl")
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.Login.ruta
    ) {
        composable(Rutas.Login.ruta) {
            LoginScreen(
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onOlvidoContrasena = {
                    navController.navigate(
                        Rutas.RecuperarContrasena.ruta
                    )
                },
                onIngresoObservador = { correo ->
                    correoLogueado = correo
                    navController.navegarDesdeLogin(
                        destino = Rutas.MenuPrincipal.ruta
                    )
                },
                onIngresoSupervisor = { correo ->
                    correoLogueado = correo
                    navController.navegarDesdeLogin(
                        destino = Rutas.MenuPrincipal.ruta
                    )
                }
            )
        }
        composable(Rutas.RecuperarContrasena.ruta) {
            RecuperarContrasenaScreen(
                onVolver = {
                    navController.popBackStack()
                },
                onContrasenaCreada = {
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.MenuPrincipal.ruta) {
            MenuPrincipalScreen(
                correoUsuario = correoLogueado,
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onNavegarPerfil = {
                    navController.navigate(Rutas.Perfil.ruta)
                }
            )
        }

        composable(Rutas.Perfil.ruta) {
            PerfilScreen(
                correoUsuario = correoLogueado,
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onCerrarSesion = {
                    navController.navigate(Rutas.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onIrAInicio = {
                    navController.navigate(Rutas.MenuPrincipal.ruta)
                }
            )
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
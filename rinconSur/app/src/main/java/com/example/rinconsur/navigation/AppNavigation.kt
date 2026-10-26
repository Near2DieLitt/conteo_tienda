package com.example.rinconsur.navigation

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
import com.example.rinconsur.ui.components.DestinoBarra
import com.example.rinconsur.ui.screens.LoginScreen
import com.example.rinconsur.ui.screens.MenuPrincipalScreen
import com.example.rinconsur.ui.screens.PerfilScreen
import com.example.rinconsur.ui.screens.RecuperarContrasenaScreen
import com.example.rinconsur.ui.screens.SesionesScreen
import com.example.rinconsur.ui.screens.AnaliticaScreen

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
                },
                    onNavegarAnalitica = {
                    navController.irADestinoBarra(DestinoBarra.ANALITICA)
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
                    navController.popBackStack()
                }
            )
        }

        composable(Rutas.Sesiones.ruta) {
            SesionesScreen(
                puedeEditar = true,
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onNavegar = { destino -> navController.irADestinoBarra(destino) }
            )
        }

        composable(Rutas.MenuPrincipal.ruta) {
            MenuPrincipalScreen(
                correoUsuario = correoLogueado,
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onNavegarSesiones = {
                    navController.navigate(Rutas.Sesiones.ruta)
                },
                onNavegarPerfil = {
                    navController.navigate(Rutas.Perfil.ruta)
                }
            )
        }

        composable(Rutas.Analitica.ruta) {
            AnaliticaScreen(
                puedeEditar = true,
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onNavegar = { destino -> navController.irADestinoBarra(destino) }
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

private fun NavHostController.irADestinoBarra(destino: DestinoBarra) {
    val ruta = when (destino) {
        DestinoBarra.INICIO -> Rutas.MenuPrincipal.ruta
        DestinoBarra.SESIONES -> Rutas.Sesiones.ruta
        DestinoBarra.ANALITICA -> Rutas.Analitica.ruta
        DestinoBarra.PERFIL -> Rutas.Perfil.ruta

    }
    navigate(ruta) {
        popUpTo(Rutas.MenuPrincipal.ruta)
        launchSingleTop = true
    }
}
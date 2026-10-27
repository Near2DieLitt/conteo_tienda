package com.example.rinconsur.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rinconsur.data.UsuarioDbHelper
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.ui.components.DestinoBarra
import com.example.rinconsur.ui.screens.LoginScreen
import com.example.rinconsur.ui.screens.MenuPrincipalScreen
import com.example.rinconsur.ui.screens.PerfilScreen
import com.example.rinconsur.ui.screens.RecuperarContrasenaScreen
import com.example.rinconsur.ui.screens.SesionesScreen
import com.example.rinconsur.ui.screens.AnaliticaScreen
import com.example.rinconsur.ui.screens.EquipoScreen
import com.example.rinconsur.ui.screens.EscanearBoletaScreen

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
        mutableStateOf("")
    }

    val contexto = LocalContext.current
    val puedeEditar = remember(correoLogueado, rolActivo) {
        rolActivo == RolUsuario.ADMIN &&
                correoLogueado.isNotBlank() &&
                UsuarioDbHelper(contexto).esAdminActivo(correoLogueado)
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
                    rolActivo = RolUsuario.OBSERVADOR
                    correoLogueado = correo
                    navController.navegarDesdeLogin(
                        destino = Rutas.MenuPrincipal.ruta
                    )
                },
                onIngresoSupervisor = { correo ->
                    rolActivo = RolUsuario.SUPERVISOR
                    correoLogueado = correo
                    navController.navegarDesdeLogin(
                        destino = Rutas.MenuPrincipal.ruta
                    )
                },
                onIngresoAdmin = { correo ->
                    rolActivo = RolUsuario.ADMIN
                    correoLogueado = correo
                    navController.navegarDesdeLogin(
                        destino = Rutas.MenuPrincipal.ruta
                    )
                }
            )
        }
        composable(Rutas.RecuperarContrasena.ruta) {
            RecuperarContrasenaScreen(
                modoOscuro = modoOscuro,
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
                onNavegarSesiones = {
                    navController.navigate(Rutas.Sesiones.ruta)
                },
                onNavegarAnalitica = {
                    navController.irADestinoBarra(DestinoBarra.ANALITICA)
                },
                onNavegarEquipo = {
                    navController.irADestinoBarra(DestinoBarra.EQUIPO)
                },
                onNavegarPerfil = {
                    navController.navigate(Rutas.Perfil.ruta)
                },
                onEscanearBoleta = {
                    navController.navigate(Rutas.EscanearBoleta.ruta)
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
                puedeEditar = puedeEditar,
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onNavegar = { destino -> navController.irADestinoBarra(destino) }
            )
        }

        composable(Rutas.Analitica.ruta) {
            AnaliticaScreen(
                puedeEditar = puedeEditar,
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onNavegar = { destino -> navController.irADestinoBarra(destino) }
            )
        }

        composable(Rutas.Equipo.ruta) {
            EquipoScreen(
                correoUsuario = correoLogueado,
                modoOscuro = modoOscuro,
                onCambiarTema = onCambiarTema,
                onNavegar = { destino -> navController.irADestinoBarra(destino) }
            )
        }

        composable(Rutas.EscanearBoleta.ruta) {
            EscanearBoletaScreen(
                onVolver = {
                    navController.popBackStack()
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

private fun NavHostController.irADestinoBarra(destino: DestinoBarra) {
    val ruta = when (destino) {
        DestinoBarra.INICIO -> Rutas.MenuPrincipal.ruta
        DestinoBarra.SESIONES -> Rutas.Sesiones.ruta
        DestinoBarra.ANALITICA -> Rutas.Analitica.ruta
        DestinoBarra.EQUIPO -> Rutas.Equipo.ruta
        DestinoBarra.PERFIL -> Rutas.Perfil.ruta

    }
    navigate(ruta) {
        popUpTo(Rutas.MenuPrincipal.ruta)
        launchSingleTop = true
    }
}
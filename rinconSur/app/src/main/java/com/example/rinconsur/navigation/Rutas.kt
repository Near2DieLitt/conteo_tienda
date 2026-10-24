package com.example.rinconsur.navigation

sealed class Rutas(val ruta: String) {
    data object Login : Rutas("login")
    data object RecuperarContrasena : Rutas("recuperar_contrasena")
    data object MenuPrincipal : Rutas("menu_principal")
    data object RegistroPersona : Rutas("registro_persona")
    data object ProgramarSesion : Rutas("programar_sesion")
    data object RechazarSesion : Rutas("rechazar_sesion")
    data object Perfil : Rutas("perfil")
}
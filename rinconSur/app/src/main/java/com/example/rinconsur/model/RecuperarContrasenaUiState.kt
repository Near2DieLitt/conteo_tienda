package com.example.rinconsur.model

data class RecuperarContrasenaUiState (
    val correo: String = "",
    val errorCorreo: String? = null,

    val codigoSmsIngresado: String = "",
    val errorCodigoSms: String? = null,
    val codigoGenerado: String = "",
    val pasoActual: Int = 1,

    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val nuevaContrasenaVisible: Boolean = false,
    val confirmarContrasenaVisible: Boolean = false,
    val errorNuevaContrasena: String? = null,
    val errorConfirmacion: String? = null,

    val recuperacionExitosa: Boolean = false
)
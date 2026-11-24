package com.example.rinconsur.model

data class RecuperarContrasenaUiState(
    val correo: String = "",
    val codigoSmsIngresado: String = "",
    val codigoGenerado: String = "",
    val expiraEnMillis: Long = 0L,
    val intentosRestantes: Int = 3,
    val correoRegistradoReal: Boolean = false,
    val mensajeNeutroEnvio: String? = null,
    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val nuevaContrasenaVisible: Boolean = false,
    val confirmarContrasenaVisible: Boolean = false,
    val pasoActual: Int = 1,
    val errorCorreo: String? = null,
    val errorCodigoSms: String? = null,
    val errorNuevaContrasena: String? = null,
    val errorConfirmacion: String? = null,
    val recuperacionExitosa: Boolean = false
)
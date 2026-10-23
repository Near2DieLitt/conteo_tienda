package com.example.rinconsur.model

data class RecuperarContrasenaUiState (
    val correo: String = "",
    val nuevaContrasena: String = "",
    val confirmarContrasena: String = "",
    val nuevaContrasenaVisible: Boolean = false,
    val confirmarContrasenaVisible: Boolean = false,
    val errorCorreo: String? = null,
    val errorNuevaContrasena: String? = null,
    val errorConfirmacion: String? = null,
    val recuperacionExitosa: Boolean = false
)
package com.example.rinconsur.model

enum class RolUsuario {
    OBSERVADOR,
    SUPERVISOR,
    ADMIN;

    val puedeGestionarUsuarios: Boolean
        get() = this == ADMIN

    val puedeCrearSesiones: Boolean
        get() = this == SUPERVISOR

    val puedeHacerConteos: Boolean
        get() = this == OBSERVADOR
}
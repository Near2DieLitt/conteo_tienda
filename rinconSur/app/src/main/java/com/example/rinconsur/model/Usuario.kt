package com.example.rinconsur.model


data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val passwordHash: String,
    val rol: RolUsuario,
    val avatar: ByteArray? = null,
    val descripcion: String? = null,
    val rut: String = "",
    val perfil: String = Perfiles.OBSERVADOR.codigo,
    val activo: Boolean = true
)
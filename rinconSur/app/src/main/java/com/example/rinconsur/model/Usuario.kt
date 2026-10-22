package com.example.rinconsur.model


data class Usuario(
    val correo: String,
    val passwordHash: String,
    val nombre: String,
    val rol: RolUsuario
)
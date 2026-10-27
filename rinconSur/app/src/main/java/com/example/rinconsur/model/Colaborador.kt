package com.example.rinconsur.model

data class Colaborador(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rut: String,
    val perfil: PerfilAcceso,
    val rol: RolUsuario,
    val activo: Boolean
)

data class PerfilAcceso(
    val codigo: String,
    val nombre: String,
    val rol: RolUsuario
)

object Perfiles {
    val ADMINISTRADOR = PerfilAcceso("P-01", "Administrador", RolUsuario.ADMIN)
    val SUPERVISOR_PISO = PerfilAcceso("P-02", "Supervisor de piso", RolUsuario.SUPERVISOR)
    val SUPERVISOR_CAJA = PerfilAcceso("P-03", "Supervisor de caja", RolUsuario.SUPERVISOR)
    val OBSERVADOR = PerfilAcceso("P-04", "Observador", RolUsuario.OBSERVADOR)

    val todos = listOf(ADMINISTRADOR, SUPERVISOR_PISO, SUPERVISOR_CAJA, OBSERVADOR)

    fun deCodigo(codigo: String?): PerfilAcceso =
        todos.firstOrNull { it.codigo == codigo } ?: OBSERVADOR
}

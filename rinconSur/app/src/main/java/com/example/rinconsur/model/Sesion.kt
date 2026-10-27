package com.example.rinconsur.model

data class ConteoSesion(
    val id: Long,
    val nombre: String,
    val horario: String,
    val completado: Boolean,
    val activo: Boolean
)

data class SesionAcceso(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val fecha: String,
    val activo: Boolean,
    val usuarioAsignado: String? = null,
    val transitoJoven: Int = 0,
    val transitoAdulto: Int = 0,
    val transitoMayor: Int = 0,
    val totalIngresos: Int = 0,
    val conteos: List<ConteoSesion>
){
    val avance: Int
        get(){
            val vigentes = conteos.filter { it.activo }
            return if (vigentes.isEmpty()) 0
            else vigentes.count { it.completado } * 100 / vigentes.size
        }
}
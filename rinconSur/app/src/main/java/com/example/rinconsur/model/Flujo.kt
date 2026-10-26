package com.example.rinconsur.model


data class RegistroFlujo(
    val id: Long,
    val acceso: String,
    val fecha: String,
    val ingresos: Int,
    val compradores: Int,
    val transitoJoven: Int,
    val transitoAdulto: Int,
    val transitoMayor: Int,
    val activo: Boolean
) {
    val transito: Int get() = transitoJoven + transitoAdulto + transitoMayor
}

data class ResumenFlujo(
    val ingresos: Int = 0,
    val compradores: Int = 0,
    val transitoJoven: Int = 0,
    val transitoAdulto: Int = 0,
    val transitoMayor: Int = 0
) {
    val transito: Int get() = transitoJoven + transitoAdulto + transitoMayor
    val porcentajeTransito: Float get() = if (ingresos == 0) 0f else transito * 100f / ingresos
    val porcentajeConversion: Float get() = if (ingresos == 0) 0f else compradores * 100f / ingresos
}

object Accesos {
    val lista = listOf("A1", "A2", "A3", "A4")

    fun nombre(codigo: String): String = when (codigo) {
        "A1" -> "Principal"
        "A2" -> "Lateral"
        "A3" -> "Estacionamiento"
        "A4" -> "Conexión"
        else -> codigo
    }
}
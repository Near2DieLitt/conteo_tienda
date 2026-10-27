package com.example.rinconsur.model

data class ResumenVisitantes(
    val enTienda: Int = 0,
    val compro: Int = 0,
    val noCompro: Int = 0,
    val total: Int = 0
)

sealed class ResultadoBoleta {
    data object FormatoInvalido : ResultadoBoleta()
    data object YaProcesada : ResultadoBoleta()
    data object SinVisitanteActivo : ResultadoBoleta()
    data class Exito(val idVisitante: Long, val folio: String) : ResultadoBoleta()
}

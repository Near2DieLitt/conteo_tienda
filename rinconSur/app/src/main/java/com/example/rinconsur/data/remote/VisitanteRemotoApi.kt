package com.example.rinconsur.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VisitanteRemotoApi {
    @GET("api/visitantes/en-tienda")
    suspend fun listarEnTienda(
        @Query("fecha") fecha: String,
        @Query("idSesion") idSesion: Long? = null
    ): List<VisitanteRemotoDto>

    @GET("api/visitantes/resumen")
    suspend fun obtenerResumen(
        @Query("fecha") fecha: String,
        @Query("idSesion") idSesion: Long? = null
    ): ResumenVisitantesDto

    @POST("api/visitantes/entrada")
    suspend fun registrarEntrada(@Body visitante: VisitanteRemotoDto): VisitanteRemotoDto

    @PATCH("api/visitantes/{id}/salida-sin-compra")
    suspend fun registrarSalidaSinCompra(@Path("id") id: Long): VisitanteRemotoDto

    @POST("api/visitantes/{id}/boleta")
    suspend fun vincularBoleta(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): VisitanteRemotoDto

    @POST("api/visitantes/cierre-diario")
    suspend fun ejecutarCierreDiario(
        @Query("fecha") fecha: String,
        @Query("idSesion") idSesion: Long? = null
    ): Map<String, Int>
}

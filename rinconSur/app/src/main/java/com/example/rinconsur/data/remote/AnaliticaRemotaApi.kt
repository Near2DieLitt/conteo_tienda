package com.example.rinconsur.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnaliticaRemotaApi {
    @GET("api/analitica/flujo")
    suspend fun obtenerPorRango(
        @Query("desde") desde: String,
        @Query("hasta") hasta: String,
        @Query("incluirInactivos") incluirInactivos: Boolean = false
    ): List<RegistroFlujoRemotoDto>

    @POST("api/analitica/flujo")
    suspend fun registrarFlujo(@Body registro: RegistroFlujoRemotoDto): RegistroFlujoRemotoDto

    @POST("api/analitica/flujo/lote")
    suspend fun registrarLote(@Body registros: List<RegistroFlujoRemotoDto>): Map<String, Int>

    @PATCH("api/analitica/flujo/{id}/activo")
    suspend fun cambiarActivo(
        @Path("id") id: Long,
        @Query("activo") activo: Boolean
    ): RegistroFlujoRemotoDto
}

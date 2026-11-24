package com.example.rinconsur.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SesionRemotaApi {
    @GET("api/sesiones")
    suspend fun listarSesiones(
        @Query("fecha") fecha: String? = null,
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null,
        @Query("incluirInactivas") incluirInactivas: Boolean = true,
        @Query("idUsuario") idUsuario: Long? = null
    ): List<SesionRemotaDto>

    @POST("api/sesiones")
    suspend fun crearSesion(
        @Body sesion: SesionRemotaDto,
        @Query("rolSolicitante") rolSolicitante: String? = null
    ): SesionRemotaDto

    @PATCH("api/sesiones/{id}/activo")
    suspend fun cambiarActivo(
        @Path("id") id: Long,
        @Query("activo") activo: Boolean
    ): SesionRemotaDto

    @PATCH("api/sesiones/{id}/completada")
    suspend fun cambiarCompletada(
        @Path("id") id: Long,
        @Query("completada") completada: Boolean,
        @Query("rolSolicitante") rolSolicitante: String? = null
    ): SesionRemotaDto

    @PATCH("api/sesiones/{id}/finalizar")
    suspend fun finalizarSesion(@Path("id") id: Long): SesionRemotaDto
}

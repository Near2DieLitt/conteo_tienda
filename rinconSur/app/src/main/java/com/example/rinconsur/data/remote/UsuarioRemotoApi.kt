package com.example.rinconsur.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuarioRemotoApi {
    @GET("api/usuarios")
    suspend fun listarUsuarios(): List<UsuarioRemotoDto>

    @GET("api/usuarios/correo")
    suspend fun buscarPorCorreo(@Query("correo") correo: String): UsuarioRemotoDto

    @POST("api/usuarios/login")
    suspend fun login(@Body body: Map<String, String>): UsuarioRemotoDto

    @POST("api/usuarios/colaboradores")
    suspend fun crearColaborador(@Body body: Map<String, String>): Map<String, Any>

    @POST("api/usuarios/cambiar-clave")
    suspend fun cambiarClave(@Body body: Map<String, @JvmSuppressWildcards Any>): Map<String, String>

    @PATCH("api/usuarios/{id}/activo")
    suspend fun cambiarActivo(
        @Path("id") id: Long,
        @Query("idSolicitante") idSolicitante: Long,
        @Query("activo") activo: Boolean
    ): UsuarioRemotoDto

    @POST("api/usuarios/{id}/clave-temporal")
    suspend fun generarClaveTemporal(
        @Path("id") id: Long,
        @Query("idSolicitante") idSolicitante: Long
    ): Map<String, Any>

    @PATCH("api/usuarios/{id}/perfil")
    suspend fun cambiarPerfil(
        @Path("id") id: Long,
        @Query("idSolicitante") idSolicitante: Long,
        @Query("perfil") perfil: String,
        @Query("rol") rol: String
    ): UsuarioRemotoDto

    @PATCH("api/usuarios/{id}/datos")
    suspend fun actualizarDatos(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): UsuarioRemotoDto

    @PATCH("api/usuarios/perfil-propio")
    suspend fun actualizarPerfilPropio(@Body body: Map<String, String>): UsuarioRemotoDto

    @POST("api/usuarios/recuperar/solicitar")
    suspend fun solicitarRecuperacion(@Body body: Map<String, String>): Map<String, String>

    @POST("api/usuarios/recuperar/verificar")
    suspend fun verificarRecuperacion(@Body body: Map<String, String>): Map<String, String>

    @POST("api/usuarios/recuperar/confirmar")
    suspend fun confirmarRecuperacion(@Body body: Map<String, String>): Map<String, String>
}

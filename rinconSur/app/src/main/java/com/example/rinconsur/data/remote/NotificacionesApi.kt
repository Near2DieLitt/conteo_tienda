package com.example.rinconsur.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface NotificacionesApi {
    @POST("api/notificaciones/correo")
    suspend fun enviarCorreo(@Body datos: CorreoRequest): CorreoResponse
}

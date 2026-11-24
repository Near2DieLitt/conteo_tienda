package com.example.rinconsur.data

import com.example.rinconsur.data.remote.CorreoRequest
import com.example.rinconsur.data.remote.CorreoResponse
import com.example.rinconsur.data.remote.NotificacionesApi
import com.example.rinconsur.data.remote.RetrofitClient
import com.example.rinconsur.data.remote.RolApi
import com.example.rinconsur.data.remote.RolDto
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class NotificacionesRepository(
    private val notificacionesApi: NotificacionesApi = RetrofitClient.notificacionesApi,
    private val rolApi: RolApi = RetrofitClient.rolApi
) {
    suspend fun enviarClaveTemporal(correo: String, nombre: String, claveTemporal: String): CorreoResponse {
        val correoLimpio = correo.trim()
        val request = CorreoRequest(
            destinatario = correoLimpio,
            asunto = "Rincón Sur - Clave temporal de acceso",
            nombreColaborador = nombre.trim(),
            codigoOClave = claveTemporal,
            tipo = "CLAVE_TEMPORAL"
        )
        return llamar { notificacionesApi.enviarCorreo(request) }
    }

    suspend fun enviarCodigoRecuperacion(correo: String, codigo: String): CorreoResponse {
        val correoLimpio = correo.trim()
        val request = CorreoRequest(
            destinatario = correoLimpio,
            asunto = "Rincón Sur - Código de verificación",
            nombreColaborador = "",
            codigoOClave = codigo,
            tipo = "RECUPERACION"
        )
        return llamar { notificacionesApi.enviarCorreo(request) }
    }

    suspend fun listarRolesRemotos(): List<RolDto> =
        llamar { rolApi.listarRoles() }

    private suspend fun <T> llamar(peticion: suspend () -> T): T = try {
        peticion()
    } catch (e: HttpException) {
        throw Exception(mensajeDelServidor(e))
    } catch (e: IOException) {
        throw Exception("No hay conexión con el servicio de correos")
    }

    private fun mensajeDelServidor(e: HttpException): String {
        val cuerpo = e.response()?.errorBody()?.string()
        val mensaje = try {
            JSONObject(cuerpo ?: "").optString("mensaje").ifBlank {
                JSONObject(cuerpo ?: "").optString("message")
            }
        } catch (ex: Exception) {
            ""
        }
        val peticion = e.response()?.raw()?.request
        val detalle = if (peticion != null) " en ${peticion.method} ${peticion.url.encodedPath}" else ""
        return mensaje.ifBlank { "Error del servidor (${e.code()})$detalle" }
    }
}
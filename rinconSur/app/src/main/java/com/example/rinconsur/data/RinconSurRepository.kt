package com.example.rinconsur.data

import com.example.rinconsur.data.remote.AnaliticaRemotaApi
import com.example.rinconsur.data.remote.RegistroFlujoRemotoDto
import com.example.rinconsur.data.remote.RetrofitClient
import com.example.rinconsur.data.remote.SesionRemotaApi
import com.example.rinconsur.data.remote.SesionRemotaDto
import com.example.rinconsur.data.remote.UsuarioRemotoApi
import com.example.rinconsur.data.remote.UsuarioRemotoDto
import com.example.rinconsur.data.remote.VisitanteRemotoApi
import com.example.rinconsur.data.remote.VisitanteRemotoDto
import com.example.rinconsur.model.ResumenVisitantes
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

data class VisitanteEnTiendaItem(
    val id: Long,
    val codigo: String,
    val hora: String,
    val acceso: String,
    val rangoEtario: String
)

class RinconSurRepository(
    private val usuarioApi: UsuarioRemotoApi = RetrofitClient.usuarioRemotoApi,
    private val sesionApi: SesionRemotaApi = RetrofitClient.sesionRemotaApi,
    private val visitanteApi: VisitanteRemotoApi = RetrofitClient.visitanteRemotoApi,
    private val analiticaApi: AnaliticaRemotaApi = RetrofitClient.analiticaRemotaApi
) {

    suspend fun login(correo: String, password: String): UsuarioRemotoDto =
        llamar { usuarioApi.login(mapOf("correo" to correo.trim(), "password" to password)) }

    suspend fun listarUsuarios(): List<UsuarioRemotoDto> =
        llamar { usuarioApi.listarUsuarios() }

    suspend fun buscarUsuarioPorCorreo(correo: String): UsuarioRemotoDto =
        llamar { usuarioApi.buscarPorCorreo(correo.trim()) }

    suspend fun crearColaborador(nombre: String, correo: String, rut: String, perfil: String, rol: String): String {
        val resp = llamar {
            usuarioApi.crearColaborador(
                mapOf(
                    "nombre" to nombre.trim(),
                    "correo" to correo.trim().lowercase(),
                    "rut" to rut.trim().uppercase(),
                    "perfil" to perfil,
                    "rol" to rol
                )
            )
        }
        return resp["mensaje"]?.toString()
            ?: "Se ha enviado un mensaje con la clave temporal al propietario del correo ($correo)"
    }

    suspend fun cambiarClave(correo: String, claveActual: String, claveNueva: String, esPrimerInicio: Boolean) {
        llamar {
            usuarioApi.cambiarClave(
                mapOf(
                    "correo" to correo.trim(),
                    "claveActual" to claveActual,
                    "claveNueva" to claveNueva,
                    "esPrimerInicio" to esPrimerInicio
                )
            )
        }
    }

    suspend fun cambiarActivoColaborador(idObjetivo: Long, idSolicitante: Long, activo: Boolean): UsuarioRemotoDto =
        llamar { usuarioApi.cambiarActivo(idObjetivo, idSolicitante, activo) }

    suspend fun cambiarPerfilColaborador(idObjetivo: Long, idSolicitante: Long, perfil: String, rol: String): UsuarioRemotoDto =
        llamar { usuarioApi.cambiarPerfil(idObjetivo, idSolicitante, perfil, rol) }

    suspend fun generarClaveTemporal(idObjetivo: Long, idSolicitante: Long): Map<String, Any> =
        llamar { usuarioApi.generarClaveTemporal(idObjetivo, idSolicitante) }

    suspend fun actualizarDatosColaborador(idObjetivo: Long, nombre: String, correo: String): UsuarioRemotoDto =
        llamar { usuarioApi.actualizarDatos(idObjetivo, mapOf("nombre" to nombre.trim(), "correo" to correo.trim().lowercase())) }

    suspend fun actualizarPerfilPropio(correo: String, descripcion: String? = null, avatarBase64: String? = null): UsuarioRemotoDto {
        val body = mutableMapOf("correo" to correo.trim())
        if (descripcion != null) body["descripcion"] = descripcion
        if (avatarBase64 != null) body["avatarBase64"] = avatarBase64
        return llamar { usuarioApi.actualizarPerfilPropio(body) }
    }

    suspend fun solicitarRecuperacion(correo: String): String {
        val resp = llamar { usuarioApi.solicitarRecuperacion(mapOf("correo" to correo.trim().lowercase())) }
        return resp["mensaje"] ?: "Si el correo está registrado, recibirás un código de verificación."
    }

    suspend fun verificarCodigoRecuperacion(correo: String, codigo: String) {
        llamar { usuarioApi.verificarRecuperacion(mapOf("correo" to correo.trim().lowercase(), "codigo" to codigo.trim())) }
    }

    suspend fun confirmarRecuperacion(correo: String, codigo: String, nuevaClave: String) {
        llamar {
            usuarioApi.confirmarRecuperacion(
                mapOf(
                    "correo" to correo.trim().lowercase(),
                    "codigo" to codigo.trim(),
                    "nuevaClave" to nuevaClave
                )
            )
        }
    }

    suspend fun listarSesiones(
        fecha: String? = null,
        desde: String? = null,
        hasta: String? = null,
        incluirInactivas: Boolean = true,
        idUsuario: Long? = null
    ): List<SesionRemotaDto> =
        llamar { sesionApi.listarSesiones(fecha, desde, hasta, incluirInactivas, idUsuario) }

    suspend fun crearSesion(codigo: String, nombre: String, fecha: String, idUsuario: Long?): SesionRemotaDto =
        llamar {
            sesionApi.crearSesion(
                SesionRemotaDto(
                    codigo = codigo,
                    nombre = nombre,
                    fecha = fecha,
                    idUsuarioAsignado = idUsuario
                ),
                rolSolicitante = "SUPERVISOR"
            )
        }

    suspend fun cambiarActivoSesion(idSesion: Long, activo: Boolean): SesionRemotaDto =
        llamar { sesionApi.cambiarActivo(idSesion, activo) }

    suspend fun cambiarCompletadaSesion(idSesion: Long, completada: Boolean): SesionRemotaDto =
        llamar { sesionApi.cambiarCompletada(idSesion, completada, rolSolicitante = "OBSERVADOR") }

    suspend fun finalizarSesion(idSesion: Long): SesionRemotaDto =
        llamar { sesionApi.finalizarSesion(idSesion) }

    suspend fun listarVisitantesEnTienda(fecha: String, idSesion: Long? = null): List<VisitanteEnTiendaItem> =
        llamar { visitanteApi.listarEnTienda(fecha, idSesion) }.map {
            VisitanteEnTiendaItem(
                id = it.id ?: 0L,
                codigo = it.codigo ?: "VIS-000",
                hora = it.hora,
                acceso = it.acceso,
                rangoEtario = it.rangoEtario
            )
        }

    suspend fun obtenerResumenVisitantes(fecha: String, idSesion: Long? = null): ResumenVisitantes =
        llamar { visitanteApi.obtenerResumen(fecha, idSesion) }.aResumen()

    suspend fun registrarEntradaVisitante(fecha: String, hora: String, acceso: String, rangoEtario: String, idSesion: Long?): VisitanteRemotoDto =
        llamar {
            visitanteApi.registrarEntrada(
                VisitanteRemotoDto(
                    fecha = fecha,
                    hora = hora,
                    acceso = acceso,
                    rangoEtario = rangoEtario,
                    idSesion = idSesion
                )
            )
        }

    suspend fun registrarSalidaSinCompra(idVisitante: Long): VisitanteRemotoDto =
        llamar { visitanteApi.registrarSalidaSinCompra(idVisitante) }

    suspend fun vincularBoleta(idVisitante: Long, folio: String): VisitanteRemotoDto =
        llamar { visitanteApi.vincularBoleta(idVisitante, mapOf("folio" to folio.trim())) }

    suspend fun ejecutarCierreDiario(fecha: String, idSesion: Long? = null): Int =
        llamar { visitanteApi.ejecutarCierreDiario(fecha, idSesion) }["actualizados"] ?: 0

    suspend fun listarFlujo(desde: String, hasta: String, incluirInactivos: Boolean = false): List<RegistroFlujoRemotoDto> =
        llamar { analiticaApi.obtenerPorRango(desde, hasta, incluirInactivos) }

    suspend fun registrarFlujo(
        acceso: String,
        fecha: String,
        ingresos: Int,
        compradores: Int,
        joven: Int,
        adulto: Int,
        mayor: Int
    ): RegistroFlujoRemotoDto =
        llamar {
            analiticaApi.registrarFlujo(
                RegistroFlujoRemotoDto(
                    acceso = acceso,
                    fecha = fecha,
                    ingresos = ingresos,
                    compradores = compradores,
                    transitoJoven = joven,
                    transitoAdulto = adulto,
                    transitoMayor = mayor
                )
            )
        }

    suspend fun registrarLoteFlujo(registros: List<RegistroFlujoRemotoDto>): Int =
        llamar { analiticaApi.registrarLote(registros) }["insertados"] ?: 0

    suspend fun cambiarActivoFlujo(idRegistro: Long, activo: Boolean): RegistroFlujoRemotoDto =
        llamar { analiticaApi.cambiarActivo(idRegistro, activo) }

    private suspend fun <T> llamar(peticion: suspend () -> T): T = try {
        peticion()
    } catch (e: HttpException) {
        throw Exception(mensajeDelServidor(e))
    } catch (e: IOException) {
        throw Exception("No hay conexión con el servidor. Revisa que los microservicios estén iniciados y tu conexión a internet.")
    }

    private fun mensajeDelServidor(e: HttpException): String {
        val cuerpo = e.response()?.errorBody()?.string()
        val mensaje = try {
            JSONObject(cuerpo ?: "").optString("mensaje").ifBlank {
                JSONObject(cuerpo ?: "").optString("message")
            }
        } catch (_: Throwable) {
            ""
        }
        val peticion = e.response()?.raw()?.request
        val detalle = if (peticion != null) " en ${peticion.method} ${peticion.url.encodedPath}" else ""
        return mensaje.ifBlank { "Error del servidor (${e.code()})$detalle" }
    }
}
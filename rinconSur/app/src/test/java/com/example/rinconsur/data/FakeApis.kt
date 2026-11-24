package com.example.rinconsur.data

import com.example.rinconsur.data.remote.*
import java.io.IOException

// APIs falsas (como FakeUsuarioApi del profe): simulan los microservicios sin red ni servidor
class FakeUsuarioRemotoApi : UsuarioRemotoApi {
    var sinConexion = false
    val usuarios = mutableListOf(
        UsuarioRemotoDto(1L, "Daniela Gonzales", "rvaldes.4419@gmail.com", "20123456-5", "P-01", "ADMIN", true, false),
        UsuarioRemotoDto(4L, "Matías Olavarría", "mati.acevedo.05@gmail.com", "20456789-1", "P-04", "OBSERVADOR", true, false)
    )

    private fun checkRed() { if (sinConexion) throw IOException("Sin red") }

    override suspend fun listarUsuarios(): List<UsuarioRemotoDto> { checkRed(); return usuarios }
    override suspend fun buscarPorCorreo(correo: String): UsuarioRemotoDto {
        checkRed(); return usuarios.first { it.correo.equals(correo, true) }
    }
    override suspend fun login(body: Map<String, String>): UsuarioRemotoDto {
        checkRed(); return usuarios.first { it.correo.equals(body["correo"], true) }
    }
    override suspend fun crearColaborador(body: Map<String, String>): Map<String, Any> {
        checkRed()
        val nuevo = UsuarioRemotoDto(10L, body["nombre"]!!, body["correo"]!!, body["rut"]!!, body["perfil"]!!, body["rol"]!!, true, true)
        usuarios.add(nuevo)
        return mapOf("id" to 10L, "mensaje" to "Enviado a ${nuevo.correo}")
    }
    override suspend fun cambiarClave(body: Map<String, Any>): Map<String, String> { checkRed(); return mapOf("mensaje" to "OK") }
    override suspend fun cambiarActivo(id: Long, idSolicitante: Long, activo: Boolean): UsuarioRemotoDto { checkRed(); return usuarios.first().copy(activo = activo) }
    override suspend fun generarClaveTemporal(id: Long, idSolicitante: Long): Map<String, Any> {
        checkRed()
        val u = usuarios.first { it.id == id }
        return mapOf("correo" to u.correo, "mensaje" to "Se envió una clave temporal al correo de ${u.nombre}")
    }
    override suspend fun cambiarPerfil(id: Long, idSolicitante: Long, perfil: String, rol: String): UsuarioRemotoDto { checkRed(); return usuarios.first().copy(perfil = perfil, rol = rol) }
    override suspend fun actualizarDatos(id: Long, body: Map<String, String>): UsuarioRemotoDto { checkRed(); return usuarios.first().copy(nombre = body["nombre"]!!, correo = body["correo"]!!) }
    override suspend fun actualizarPerfilPropio(body: Map<String, String>): UsuarioRemotoDto { checkRed(); return usuarios.first().copy(descripcion = body["descripcion"]) }
    override suspend fun solicitarRecuperacion(body: Map<String, String>): Map<String, String> { checkRed(); return mapOf("mensaje" to "Codigo enviado") }
    override suspend fun verificarRecuperacion(body: Map<String, String>): Map<String, String> { checkRed(); return mapOf("mensaje" to "OK") }
    override suspend fun confirmarRecuperacion(body: Map<String, String>): Map<String, String> { checkRed(); return mapOf("mensaje" to "OK") }
}

class FakeSesionRemotaApi : SesionRemotaApi {
    val sesiones = mutableListOf(SesionRemotaDto(1L, "A1", "Acceso Principal", "2026-11-19", true, false, 4L))
    override suspend fun listarSesiones(fecha: String?, desde: String?, hasta: String?, incluirInactivas: Boolean, idUsuario: Long?) = sesiones
    override suspend fun crearSesion(sesion: SesionRemotaDto, rolSolicitante: String?): SesionRemotaDto {
        val guardada = sesion.copy(id = 2L); sesiones.add(guardada); return guardada
    }
    override suspend fun cambiarActivo(id: Long, activo: Boolean) = sesiones.first().copy(activo = activo)
    override suspend fun cambiarCompletada(id: Long, completada: Boolean, rolSolicitante: String?) = sesiones.first().copy(completada = completada)
    override suspend fun finalizarSesion(id: Long) = sesiones.first().copy(activo = false, completada = true)
}

class FakeVisitanteRemotoApi : VisitanteRemotoApi {
    override suspend fun listarEnTienda(fecha: String, idSesion: Long?) =
        listOf(VisitanteRemotoDto(5L, "VIS-001", fecha, "10:00:00", "A1", "ADULTO", "En_Tienda", null, idSesion))
    override suspend fun obtenerResumen(fecha: String, idSesion: Long?) = ResumenVisitantesDto(1, 2, 1, 4)
    override suspend fun registrarEntrada(visitante: VisitanteRemotoDto) = visitante.copy(id = 6L, codigo = "VIS-002")
    override suspend fun registrarSalidaSinCompra(id: Long) = VisitanteRemotoDto(id, "VIS-001", "2026-11-19", "10:00:00", "A1", "ADULTO", "No_Compro")
    override suspend fun vincularBoleta(id: Long, body: Map<String, String>) = VisitanteRemotoDto(id, "VIS-001", "2026-11-19", "10:00:00", "A1", "ADULTO", "Compro", body["folio"])
    override suspend fun ejecutarCierreDiario(fecha: String, idSesion: Long?) = mapOf("actualizados" to 1)
}

class FakeAnaliticaRemotaApi : AnaliticaRemotaApi {
    override suspend fun obtenerPorRango(desde: String, hasta: String, incluirInactivos: Boolean) =
        listOf(RegistroFlujoRemotoDto(1L, "A1", desde, 100, 30, 10, 20, 10, true))
    override suspend fun registrarFlujo(registro: RegistroFlujoRemotoDto) = registro.copy(id = 2L)
    override suspend fun registrarLote(registros: List<RegistroFlujoRemotoDto>) = mapOf("insertados" to registros.size)
    override suspend fun cambiarActivo(id: Long, activo: Boolean) = RegistroFlujoRemotoDto(id, "A1", "2026-11-19", 100, 30, 10, 20, 10, activo)
}

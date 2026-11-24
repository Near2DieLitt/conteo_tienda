package com.example.rinconsur.data.remote

import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.Perfiles
import com.example.rinconsur.model.RegistroFlujo
import com.example.rinconsur.model.ResumenVisitantes
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.model.Usuario

data class CorreoRequest(
    val destinatario: String,
    val asunto: String,
    val nombreColaborador: String,
    val codigoOClave: String,
    val tipo: String
)

data class CorreoResponse(
    val enviado: Boolean,
    val mensaje: String
)

data class RolDto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val rolSistema: String
)

data class UsuarioRemotoDto(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rut: String,
    val perfil: String,
    val rol: String,
    val activo: Boolean,
    val debeCambiarClave: Boolean,
    val descripcion: String? = null,
    val avatarBase64: String? = null
) {
    fun aRolUsuario(): RolUsuario = when (rol.uppercase()) {
        "ADMIN" -> RolUsuario.ADMIN
        "SUPERVISOR" -> RolUsuario.SUPERVISOR
        else -> RolUsuario.OBSERVADOR
    }

    fun aUsuario(): Usuario = Usuario(
        id = id.toInt(),
        nombre = nombre,
        correo = correo,
        passwordHash = "",
        rol = aRolUsuario(),
        avatar = avatarBase64?.let {
            try { android.util.Base64.decode(it, android.util.Base64.DEFAULT) } catch (_: Throwable) { null }
        },
        descripcion = descripcion,
        rut = rut,
        perfil = perfil,
        activo = activo
    )

    fun aColaborador(): Colaborador = Colaborador(
        id = id.toInt(),
        nombre = nombre,
        correo = correo,
        rut = rut,
        perfil = Perfiles.deCodigo(perfil),
        rol = aRolUsuario(),
        activo = activo
    )
}

data class SesionRemotaDto(
    val id: Long? = null,
    val codigo: String,
    val nombre: String,
    val fecha: String,
    val activo: Boolean = true,
    val completada: Boolean = false,
    val idUsuarioAsignado: Long? = null
)

data class VisitanteRemotoDto(
    val id: Long? = null,
    val codigo: String? = null,
    val fecha: String,
    val hora: String,
    val acceso: String,
    val rangoEtario: String,
    val estado: String = "En_Tienda",
    val folioBoleta: String? = null,
    val idSesion: Long? = null
)

data class ResumenVisitantesDto(
    val enTienda: Int = 0,
    val compro: Int = 0,
    val noCompro: Int = 0,
    val total: Int = 0
) {
    fun aResumen(): ResumenVisitantes = ResumenVisitantes(
        enTienda = enTienda,
        compro = compro,
        noCompro = noCompro,
        total = total
    )
}

data class RegistroFlujoRemotoDto(
    val id: Long? = null,
    val acceso: String,
    val fecha: String,
    val ingresos: Int,
    val compradores: Int,
    val transitoJoven: Int,
    val transitoAdulto: Int,
    val transitoMayor: Int,
    val activo: Boolean = true
) {
    fun aRegistroFlujo(): RegistroFlujo = RegistroFlujo(
        id = id ?: 0L,
        acceso = acceso,
        fecha = fecha,
        ingresos = ingresos,
        compradores = compradores,
        transitoJoven = transitoJoven,
        transitoAdulto = transitoAdulto,
        transitoMayor = transitoMayor,
        activo = activo
    )
}

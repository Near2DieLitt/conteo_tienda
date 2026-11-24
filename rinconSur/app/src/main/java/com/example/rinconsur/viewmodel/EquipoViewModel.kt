package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.RinconSurRepository
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.PerfilAcceso
import com.example.rinconsur.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object RutUtil {
    fun normalizar(texto: String): String? {
        val limpio = texto.replace(".", "").replace(" ", "").uppercase()
        val m = Regex("^(\\d{7,8})-?([0-9K])$").matchEntire(limpio) ?: return null
        return "${m.groupValues[1]}-${m.groupValues[2]}"
    }

    fun esValido(texto: String): Boolean {
        val rut = normalizar(texto) ?: return false
        val (cuerpo, dv) = rut.split("-")
        var suma = 0
        var mult = 2
        for (d in cuerpo.reversed()) {
            suma += d.digitToInt() * mult
            mult = if (mult == 7) 2 else mult + 1
        }
        val r = 11 - suma % 11
        val esperado = when (r) { 11 -> "0"; 10 -> "K"; else -> r.toString() }
        return dv == esperado
    }
}

data class ErroresColaborador(
    val nombre: String? = null,
    val correo: String? = null,
    val rut: String? = null
) {
    val hayErrores get() = nombre != null || correo != null || rut != null
}

object ValidadorColaborador {
    private val REGEX_CORREO_REAL = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarNombre(nombre: String): String? {
        val n = nombre.trim().replace(Regex("\\s+"), " ")
        return when {
            n.isEmpty() -> "Ingresa el nombre completo"
            !Regex("^[\\p{L}][\\p{L} .'-]*$").matches(n) -> "Solo letras y espacios"
            n.split(" ").size < 2 -> "Ingresa nombre y apellido"
            n.length > 60 -> "Máximo 60 caracteres"
            else -> null
        }
    }

    fun validarCorreo(correo: String): String? {
        val c = correo.trim().lowercase()
        return when {
            c.isEmpty() -> "Ingresa el correo"
            !REGEX_CORREO_REAL.matches(c) -> "Ingresa un correo electrónico real válido (ej: usuario@gmail.com)"
            c.length > 100 -> "Correo demasiado largo"
            else -> null
        }
    }

    fun validarRut(rut: String): String? = when {
        rut.isBlank() -> "Ingresa el RUT"
        !RutUtil.esValido(rut) -> "RUT inválido"
        else -> null
    }
}

data class EquipoUiState(
    val colaboradores: List<Colaborador> = emptyList(),
    val esAdmin: Boolean = false,
    val miId: Int = -1,
    val busqueda: String = "",
    val filtroPerfil: String? = null,
    val errores: ErroresColaborador = ErroresColaborador(),
    val mensaje: String? = null
) {
    val visibles: List<Colaborador>
        get() = colaboradores.filter {
            (filtroPerfil == null || it.perfil.codigo == filtroPerfil) &&
                    (busqueda.isBlank() ||
                            it.nombre.contains(busqueda, ignoreCase = true) ||
                            it.correo.contains(busqueda, ignoreCase = true))
        }
    val activos get() = colaboradores.count { it.activo }
}

class EquipoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RinconSurRepository()
    private val _uiState = MutableStateFlow(EquipoUiState())
    val uiState: StateFlow<EquipoUiState> = _uiState.asStateFlow()

    private var correoActual = ""

    fun cargar(correo: String) {
        correoActual = correo
        refrescar()
    }

    private fun refrescar() {
        viewModelScope.launch {
            try {
                val lista = repository.listarUsuarios().map { it.aColaborador() }
                val yo = lista.firstOrNull { it.correo.equals(correoActual.trim(), ignoreCase = true) }
                _uiState.update {
                    it.copy(
                        colaboradores = lista,
                        esAdmin = yo != null && yo.activo && yo.rol == RolUsuario.ADMIN,
                        miId = yo?.id ?: -1
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = e.message ?: "No se pudo cargar el equipo") }
            }
        }
    }

    private fun autorizado(): Boolean {
        val ok = _uiState.value.esAdmin
        if (!ok) {
            _uiState.update { it.copy(mensaje = "Solo un administrador puede hacer cambios") }
        }
        return ok
    }

    fun actualizarBusqueda(texto: String) = _uiState.update { it.copy(busqueda = texto.take(60)) }
    fun filtrarPerfil(codigo: String?) = _uiState.update { it.copy(filtroPerfil = codigo) }
    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }
    fun limpiarErrores() = _uiState.update { it.copy(errores = ErroresColaborador()) }

    fun validarCampo(campo: String, valor: String) {
        _uiState.update {
            val e = it.errores
            it.copy(
                errores = when (campo) {
                    "nombre" -> e.copy(nombre = ValidadorColaborador.validarNombre(valor))
                    "correo" -> e.copy(correo = ValidadorColaborador.validarCorreo(valor))
                    "rut" -> e.copy(rut = ValidadorColaborador.validarRut(valor))
                    else -> e
                }
            )
        }
    }

    fun crearColaborador(nombre: String, correo: String, rut: String, perfil: PerfilAcceso): Boolean {
        if (!autorizado()) return false

        var errores = ErroresColaborador(
            nombre = ValidadorColaborador.validarNombre(nombre),
            correo = ValidadorColaborador.validarCorreo(correo),
            rut = ValidadorColaborador.validarRut(rut)
        )
        val correoLimpio = correo.trim().lowercase()
        val rutNormal = RutUtil.normalizar(rut)
        val actuales = _uiState.value.colaboradores

        if (errores.correo == null && actuales.any { it.correo.equals(correoLimpio, ignoreCase = true) }) {
            errores = errores.copy(correo = "Este correo ya está registrado")
        }
        if (errores.rut == null && rutNormal != null && actuales.any { it.rut.equals(rutNormal, ignoreCase = true) }) {
            errores = errores.copy(rut = "Este RUT ya está registrado")
        }
        if (errores.hayErrores || rutNormal == null) {
            _uiState.update { it.copy(errores = errores) }
            return false
        }

        val nombreLimpio = nombre.trim().replace(Regex("\\s+"), " ")
        viewModelScope.launch {
            try {
                val msg = repository.crearColaborador(
                    nombre = nombreLimpio,
                    correo = correoLimpio,
                    rut = rutNormal,
                    perfil = perfil.codigo,
                    rol = perfil.rol.name
                )
                _uiState.update { it.copy(errores = ErroresColaborador(), mensaje = msg) }
                refrescar()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = e.message ?: "No se pudo crear el colaborador") }
            }
        }
        return true
    }

    fun cambiarActivo(colaborador: Colaborador, activar: Boolean) {
        if (!autorizado()) return
        if (!activar && colaborador.id == _uiState.value.miId) {
            _uiState.update { it.copy(mensaje = "No puedes deshabilitar tu propia cuenta") }
            return
        }
        viewModelScope.launch {
            try {
                repository.cambiarActivoColaborador(
                    idObjetivo = colaborador.id.toLong(),
                    idSolicitante = _uiState.value.miId.toLong(),
                    activo = activar
                )
                _uiState.update {
                    it.copy(mensaje = if (activar) "${colaborador.nombre} habilitado(a)" else "${colaborador.nombre} deshabilitado(a)")
                }
                refrescar()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = e.message ?: "No se pudo cambiar el estado") }
            }
        }
    }

    fun generarClaveTemporal(colaborador: Colaborador) {
        if (!autorizado()) return
        if (colaborador.id == _uiState.value.miId) {
            _uiState.update { it.copy(mensaje = "Para tu cuenta usa Cambiar contraseña en tu perfil") }
            return
        }
        viewModelScope.launch {
            try {
                val resp = repository.generarClaveTemporal(
                    idObjetivo = colaborador.id.toLong(),
                    idSolicitante = _uiState.value.miId.toLong()
                )
                _uiState.update {
                    it.copy(mensaje = resp["mensaje"]?.toString() ?: "Se envió una clave temporal al correo de ${colaborador.nombre}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = e.message ?: "No se pudo generar la clave temporal") }
            }
        }
    }

    fun cambiarPerfil(colaborador: Colaborador, perfil: PerfilAcceso) {
        if (!autorizado()) return
        if (perfil.codigo == colaborador.perfil.codigo) return
        if (colaborador.id == _uiState.value.miId) {
            _uiState.update { it.copy(mensaje = "No puedes cambiar tu propio perfil") }
            return
        }
        viewModelScope.launch {
            try {
                repository.cambiarPerfilColaborador(
                    idObjetivo = colaborador.id.toLong(),
                    idSolicitante = _uiState.value.miId.toLong(),
                    perfil = perfil.codigo,
                    rol = perfil.rol.name
                )
                _uiState.update { it.copy(mensaje = "Perfil de ${colaborador.nombre}: ${perfil.nombre}") }
                refrescar()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = e.message ?: "No se pudo cambiar el perfil") }
            }
        }
    }

    fun actualizarDatosColaborador(
        colaborador: Colaborador,
        nuevoNombre: String,
        nuevoCorreo: String,
        onCorreoPropioCambiado: (String) -> Unit = {}
    ): Boolean {
        if (!autorizado()) return false

        var errores = ErroresColaborador(
            nombre = ValidadorColaborador.validarNombre(nuevoNombre),
            correo = ValidadorColaborador.validarCorreo(nuevoCorreo)
        )
        val nombreLimpio = nuevoNombre.trim().replace(Regex("\\s+"), " ")
        val correoLimpio = nuevoCorreo.trim().lowercase()

        if (errores.correo == null && _uiState.value.colaboradores.any {
                it.id != colaborador.id && it.correo.equals(correoLimpio, ignoreCase = true)
            }) {
            errores = errores.copy(correo = "Este correo ya está registrado por otro usuario")
        }
        if (errores.hayErrores) {
            _uiState.update { it.copy(errores = errores) }
            return false
        }

        viewModelScope.launch {
            try {
                repository.actualizarDatosColaborador(colaborador.id.toLong(), nombreLimpio, correoLimpio)
                if (colaborador.id == _uiState.value.miId) {
                    correoActual = correoLimpio
                    onCorreoPropioCambiado(correoLimpio)
                }
                _uiState.update {
                    it.copy(
                        errores = ErroresColaborador(),
                        mensaje = "Datos de $nombreLimpio actualizados ($correoLimpio)"
                    )
                }
                refrescar()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = e.message ?: "No se pudieron actualizar los datos") }
            }
        }
        return true
    }
}

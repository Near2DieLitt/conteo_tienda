package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.rinconsur.data.UsuarioDbHelper
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.PerfilAcceso
import com.example.rinconsur.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.security.SecureRandom


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
    const val DOMINIO = "@rinconsur.cl"

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
            !Regex("^[a-z0-9._-]+@rinconsur\\.cl$").matches(c) -> "Debe ser nombre$DOMINIO"
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

data class CredencialTemporal(val correo: String, val clave: String)

data class EquipoUiState(
    val colaboradores: List<Colaborador> = emptyList(),
    val esAdmin: Boolean = false,
    val miId: Int = -1,
    val busqueda: String = "",
    val filtroPerfil: String? = null,
    val errores: ErroresColaborador = ErroresColaborador(),
    val mensaje: String? = null,
    val credencial: CredencialTemporal? = null
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
    private val db = UsuarioDbHelper(application)
    private val _uiState = MutableStateFlow(EquipoUiState())
    val uiState: StateFlow<EquipoUiState> = _uiState.asStateFlow()

    private var correoActual = ""

    fun cargar(correo: String) {
        correoActual = correo
        refrescar()
    }

    private fun refrescar() {
        val yo = db.obtenerColaboradorPorCorreo(correoActual)
        _uiState.update {
            it.copy(
                colaboradores = db.obtenerColaboradores(),
                esAdmin = db.esAdminActivo(correoActual),
                miId = yo?.id ?: -1
            )
        }
    }


    private fun autorizado(): Boolean {
        val ok = db.esAdminActivo(correoActual)
        if (!ok) {
            _uiState.update { it.copy(esAdmin = false, mensaje = "Solo un administrador puede hacer cambios") }
        }
        return ok
    }

    fun actualizarBusqueda(texto: String) = _uiState.update { it.copy(busqueda = texto.take(60)) }
    fun filtrarPerfil(codigo: String?) = _uiState.update { it.copy(filtroPerfil = codigo) }
    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }
    fun cerrarCredencial() = _uiState.update { it.copy(credencial = null) }
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

        if (errores.correo == null && db.existeCorreoRegistrado(correoLimpio)) {
            errores = errores.copy(correo = "Este correo ya está registrado")
        }
        if (errores.rut == null && rutNormal != null && db.existeRut(rutNormal)) {
            errores = errores.copy(rut = "Este RUT ya está registrado")
        }
        if (errores.hayErrores || rutNormal == null) {
            _uiState.update { it.copy(errores = errores) }
            return false
        }

        val clave = generarClaveTemporal()
        val hash = BCrypt.withDefaults().hashToString(12, clave.toCharArray())
        val nombreLimpio = nombre.trim().replace(Regex("\\s+"), " ")
        val id = db.insertarColaborador(nombreLimpio, correoLimpio, rutNormal, hash, perfil)

        if (id == -1L) {
            _uiState.update { it.copy(mensaje = "No se pudo registrar a la persona") }
            return false
        }
        _uiState.update {
            it.copy(errores = ErroresColaborador(), credencial = CredencialTemporal(correoLimpio, clave))
        }
        refrescar()
        return true
    }

    fun cambiarActivo(colaborador: Colaborador, activar: Boolean) {
        if (!autorizado()) return
        if (!activar) {
            if (colaborador.id == _uiState.value.miId) {
                _uiState.update { it.copy(mensaje = "No puedes deshabilitar tu propia cuenta") }
                return
            }
            if (colaborador.rol == RolUsuario.ADMIN && colaborador.activo && db.contarAdminsActivos() <= 1) {
                _uiState.update { it.copy(mensaje = "Debe quedar al menos un administrador activo") }
                return
            }
        }
        db.cambiarActivoColaborador(colaborador.id, activar)
        _uiState.update {
            it.copy(mensaje = if (activar) "${colaborador.nombre} habilitado(a)" else "${colaborador.nombre} deshabilitado(a)")
        }
        refrescar()
    }

    fun cambiarPerfil(colaborador: Colaborador, perfil: PerfilAcceso) {
        if (!autorizado()) return
        if (perfil.codigo == colaborador.perfil.codigo) return
        if (colaborador.id == _uiState.value.miId) {
            _uiState.update { it.copy(mensaje = "No puedes cambiar tu propio perfil") }
            return
        }
        if (colaborador.rol == RolUsuario.ADMIN && colaborador.activo &&
            perfil.rol != RolUsuario.ADMIN && db.contarAdminsActivos() <= 1
        ) {
            _uiState.update { it.copy(mensaje = "Debe quedar al menos un administrador activo") }
            return
        }
        db.cambiarPerfilColaborador(colaborador.id, perfil)
        _uiState.update { it.copy(mensaje = "Perfil de ${colaborador.nombre}: ${perfil.nombre}") }
        refrescar()
    }

    private fun generarClaveTemporal(): String {
        val r = SecureRandom()
        val mayus = "ABCDEFGHJKMNPQRSTUVWXYZ"
        val minus = "abcdefghjkmnpqrstuvwxyz"
        val nums = "23456789"
        val todos = mayus + minus + nums
        val chars = mutableListOf(
            mayus[r.nextInt(mayus.length)],
            minus[r.nextInt(minus.length)],
            nums[r.nextInt(nums.length)]
        )
        repeat(7) { chars.add(todos[r.nextInt(todos.length)]) }
        chars.shuffle(r)
        return chars.joinToString("")
    }
}

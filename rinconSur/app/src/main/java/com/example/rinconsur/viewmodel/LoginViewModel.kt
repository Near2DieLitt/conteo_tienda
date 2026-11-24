package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.RinconSurRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object ValidadorLogin {
    private val REGEX_CORREO = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarCorreo(correo: String): String? {
        val c = correo.trim()
        return when {
            c.isBlank() -> "Ingresa tu correo electrónico."
            !REGEX_CORREO.matches(c) -> "Correo incorrecto."
            else -> null
        }
    }

    fun validarContrasena(password: String): String? {
        if (password.isBlank()) return "Ingresa tu contraseña."
        return null
    }
}

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val cargando: Boolean = false,
    val errorCorreo: String? = null,
    val errorPassword: String? = null,
    val mensajeError: String? = null,
    val usuarioAutenticado: Usuario? = null,
    val requiereCambioClave: Boolean = false
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val repository = RinconSurRepository()
    private val sesionPrefs = SesionPreferencias(application)

    fun actualizarCorreo(nuevoCorreo: String) {
        val errorEnVivo = if (nuevoCorreo.isEmpty()) null else ValidadorLogin.validarCorreo(nuevoCorreo)
        _uiState.update { estado ->
            estado.copy(
                correo = nuevoCorreo,
                errorCorreo = errorEnVivo,
                mensajeError = null
            )
        }
    }

    fun actualizarContrasena(nuevaContrasena: String) {
        val errorEnVivo = if (nuevaContrasena.isEmpty()) null else ValidadorLogin.validarContrasena(nuevaContrasena)
        _uiState.update { estado ->
            estado.copy(
                password = nuevaContrasena,
                errorPassword = errorEnVivo,
                mensajeError = null
            )
        }
    }

    fun alternarVisibilidadPassword() {
        _uiState.update { estado ->
            estado.copy(passwordVisible = !estado.passwordVisible)
        }
    }

    fun iniciarSesion() {
        val correo = _uiState.value.correo.trim()
        val password = _uiState.value.password

        val errCorreo = ValidadorLogin.validarCorreo(correo)
        val errPass = ValidadorLogin.validarContrasena(password)

        if (errCorreo != null || errPass != null) {
            _uiState.update {
                it.copy(
                    errorCorreo = errCorreo,
                    errorPassword = errPass,
                    mensajeError = null,
                    usuarioAutenticado = null
                )
            }
            return
        }

        _uiState.update { it.copy(cargando = true, errorCorreo = null, errorPassword = null, mensajeError = null) }

        viewModelScope.launch {
            try {
                val dto = repository.login(correo, password)
                sesionPrefs.guardar(dto.id)
                _uiState.update { estado ->
                    estado.copy(
                        cargando = false,
                        errorCorreo = null,
                        errorPassword = null,
                        mensajeError = null,
                        usuarioAutenticado = dto.aUsuario(),
                        requiereCambioClave = dto.debeCambiarClave
                    )
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Error al iniciar sesión."
                val esErrorPass = msg.contains("contraseña", ignoreCase = true)
                val esErrorCorreo = msg.contains("correo", ignoreCase = true) || msg.contains("deshabilitada", ignoreCase = true)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorCorreo = if (esErrorCorreo) msg else null,
                        errorPassword = if (esErrorPass) msg else null,
                        mensajeError = if (!esErrorCorreo && !esErrorPass) msg else null,
                        usuarioAutenticado = null
                    )
                }
            }
        }
    }

    fun navegacionRealizada() {
        _uiState.update { estado ->
            estado.copy(usuarioAutenticado = null, requiereCambioClave = false)
        }
    }

    fun cerrarSesion() {
        sesionPrefs.borrar()
        _uiState.value = LoginUiState()
    }
}
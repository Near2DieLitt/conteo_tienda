package com.example.rinconsur.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val mensajeError: String? = null,
    val usuarioAutenticado: Usuario? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()


    private fun crearHash(password: String): String {
        return BCrypt.withDefaults()
            .hashToString(
                12,
                password.toCharArray()
            )
    }

    private fun verificarPassword(
        passwordIngresada: String,
        passwordHash: String
    ): Boolean {
        return BCrypt.verifyer()
            .verify(
                passwordIngresada.toCharArray(),
                passwordHash
            )
            .verified
    }
    private val usuarios: List<Usuario> by lazy {
        listOf(
            Usuario(
                nombre = "Observador rincón sur",
                correo = "observador@rinconsur.cl",
                passwordHash = crearHash("Observador123"),
                rol = RolUsuario.OBSERVADOR
            ),
            Usuario(
                nombre = "Supervisor rincón sur",
                correo = "supervisor@rinconsur.cl",
                passwordHash = crearHash("Supervisor123"),
                rol = RolUsuario.SUPERVISOR
            )

        )
    }
    fun actualizarCorreo(nuevoCorreo: String) {
        _uiState.update {
            estado ->
            estado.copy(
                correo = nuevoCorreo,
                mensajeError = null
            )
        }
    }
    fun actualizarContrasena(nuevaContrasena: String) {
        _uiState.update {
                estado ->
            estado.copy(
                password = nuevaContrasena,
                mensajeError = null
            )
        }
    }

    fun alternarVisibilidadPassword() {
        _uiState.update { estado ->
            estado.copy(
                passwordVisible = !estado.passwordVisible
            )
        }
    }

    private fun mostrarError(mensaje: String) {
        _uiState.update { estado ->
            estado.copy(
                mensajeError = mensaje,
                usuarioAutenticado = null
            )
        }
    }

    fun iniciarSesion() {
        val correo = _uiState.value.correo.trim()
        val password = _uiState.value.password

        if(correo.isBlank()) {
            mostrarError("Ingresa tu correo electrónico.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarError("Ingresa un correo electrónico válido.")
            return
        }

        val usuarioEncontrado = usuarios.find { usuario ->
            usuario.correo.equals(
                other = correo,
                ignoreCase = true
            )
        }

        if (usuarioEncontrado == null) {
            mostrarError("Correo o contraseña incorrectos.")
            return
        }


        val passwordCorrecta = verificarPassword(
            passwordIngresada = password,
            passwordHash = usuarioEncontrado.passwordHash
        )

        if (!passwordCorrecta) {
            mostrarError("Correo o contrseña incorrectos.")
            return
        }

        _uiState.update { estado ->
            estado.copy(
                mensajeError = null,
                usuarioAutenticado = usuarioEncontrado
            )
        }
    }

    fun navegacionRealizada() {
        _uiState.update { estado ->
            estado.copy(usuarioAutenticado = null)
        }
    }


    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }



}
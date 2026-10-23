package com.example.rinconsur.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.rinconsur.model.RecuperarContrasenaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecuperarContrasenaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarContrasenaUiState())

    val uiState: StateFlow<RecuperarContrasenaUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(
                correo = valor.trim(),
                errorCorreo = null,
                recuperacionExitosa = false
            )
        }
    }

    fun onNuevaContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                nuevaContrasena = valor,
                errorConfirmacion = null,
                recuperacionExitosa = false
            )
        }
    }
    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                confirmarContrasena = valor,
                errorConfirmacion = null,
                recuperacionExitosa = false
            )
        }
    }

    fun onAlternanConfirmacionVisible() {
        _uiState.update {
            it.copy(
                nuevaContrasenaVisible =
                    !it.nuevaContrasenaVisible
            )
        }
    }

    fun onCrearContrasena(): Boolean {
        val estado = _uiState.value

        val errorCorreo = when {
            estado.correo.isBlank() ->
                "El correo es obligatorio"

            !Patterns.EMAIL_ADDRESS
                .matcher(estado.correo)
                .matches() -> "Ingresa un correo valido"

            else -> null

        }
        val errorNuevaContrasena = when {
            estado.nuevaContrasena.isBlank() ->
                "La contraseña es obligatoria"
            estado.nuevaContrasena.length < 8 ->
                "La contraseña debe tener almenos 8 caracteres"
            else -> null
        }

        val errorConfirmacion = when {
            estado.confirmarContrasena.isBlank() ->
                "Debes confirmar la contraseña"
            estado.nuevaContrasena != estado.confirmarContrasena ->
                "Las contraseñas no coinciden"
            else -> null
        }

        val hayErrores = listOf(
            errorCorreo,
            errorNuevaContrasena,
            errorConfirmacion
        ).any { it != null }
        _uiState.update {
            it.copy(
                errorCorreo = errorCorreo,
                errorNuevaContrasena = errorNuevaContrasena,
                errorConfirmacion = errorConfirmacion,
                recuperacionExitosa = !hayErrores
            )
        }
        return !hayErrores
    }

    fun onLimpiar() {
        _uiState.update {
            it.copy(recuperacionExitosa = false)
        }
    }
}
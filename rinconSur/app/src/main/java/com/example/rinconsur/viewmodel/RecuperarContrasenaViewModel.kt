package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.RinconSurRepository
import com.example.rinconsur.model.RecuperarContrasenaUiState
import com.example.rinconsur.model.ValidadorClave
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecuperarContrasenaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RinconSurRepository()
    private val _uiState = MutableStateFlow(RecuperarContrasenaUiState())
    val uiState: StateFlow<RecuperarContrasenaUiState> = _uiState.asStateFlow()

    companion object {
        private val REGEX_CORREO = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")    }

    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null) }
    }

    fun onCodigoSmsCambia(valor: String) {
        val soloDigitos = valor.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(codigoSmsIngresado = soloDigitos, errorCodigoSms = null) }
    }

    fun onNuevaContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(
                nuevaContrasena = valor,
                errorConfirmacion = null,
                errorNuevaContrasena = null
            )
        }
    }

    fun onConfirmarContrasenaCambia(valor: String) {
        _uiState.update { it.copy(confirmarContrasena = valor, errorConfirmacion = null) }
    }

    fun onAlternarNuevaContrasenaVisible() {
        _uiState.update { it.copy(nuevaContrasenaVisible = !it.nuevaContrasenaVisible) }
    }

    fun onAlternarConfirmacionVisible() {
        _uiState.update { it.copy(confirmarContrasenaVisible = !it.confirmarContrasenaVisible) }
    }

    fun onEnviarCodigoPorCorreo() {
        val correoLimpio = _uiState.value.correo.trim().lowercase()
        if (correoLimpio.isBlank() || !REGEX_CORREO.matches(correoLimpio)) {
            _uiState.update { it.copy(errorCorreo = "Ingresa un correo electrónico válido") }
            return
        }

        viewModelScope.launch {
            try {
                val msg = repository.solicitarRecuperacion(correoLimpio)
                _uiState.update {
                    it.copy(
                        correo = correoLimpio,
                        pasoActual = 2,
                        codigoSmsIngresado = "",
                        errorCorreo = null,
                        errorCodigoSms = null,
                        mensajeNeutroEnvio = msg
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorCorreo = e.message ?: "No hay conexión con el servicio de correos")
                }
            }
        }
    }

    fun onVerificarCodigoSms() {
        val estado = _uiState.value
        if (estado.codigoSmsIngresado.length != 6) {
            _uiState.update { it.copy(errorCodigoSms = "Ingresa el código de 6 dígitos") }
            return
        }

        viewModelScope.launch {
            try {
                repository.verificarCodigoRecuperacion(estado.correo, estado.codigoSmsIngresado)
                _uiState.update { it.copy(pasoActual = 3, errorCodigoSms = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorCodigoSms = e.message ?: "Código incorrecto") }
            }
        }
    }

    fun volverAPasoCorreo() {
        _uiState.update {
            it.copy(
                pasoActual = 1,
                codigoSmsIngresado = "",
                errorCodigoSms = null
            )
        }
    }

    fun onCrearContrasena() {
        val estado = _uiState.value
        val errorNueva = ValidadorClave.validar(estado.nuevaContrasena)
        val errorConfirmacion = when {
            estado.confirmarContrasena.isBlank() -> "Debes confirmar la contraseña"
            estado.nuevaContrasena != estado.confirmarContrasena -> "Las contraseñas no coinciden"
            else -> null
        }

        if (errorNueva != null || errorConfirmacion != null) {
            _uiState.update {
                it.copy(
                    errorNuevaContrasena = errorNueva,
                    errorConfirmacion = errorConfirmacion
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                repository.confirmarRecuperacion(estado.correo, estado.codigoSmsIngresado, estado.nuevaContrasena)
                _uiState.update { it.copy(recuperacionExitosa = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorNuevaContrasena = e.message ?: "No se pudo restablecer la contraseña") }
            }
        }
    }
}

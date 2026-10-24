package com.example.rinconsur.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.rinconsur.data.UsuarioDbHelper
import com.example.rinconsur.model.RecuperarContrasenaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecuperarContrasenaViewModel(application: Application): AndroidViewModel(application){
    private val dbHelper = UsuarioDbHelper(application)
    private val _uiState = MutableStateFlow(RecuperarContrasenaUiState())
    val uiState: StateFlow<RecuperarContrasenaUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String){
        _uiState.update { it.copy(correo = valor.trim(), errorCorreo = null) }
    }

    fun onCodigoSmsCambia(valor: String){
        _uiState.update { it.copy(codigoSmsIngresado = valor, errorCodigoSms = null) }
    }

    fun onNuevaContrasenaCambia(valor: String){
        _uiState.update { it.copy(nuevaContrasena = valor, errorConfirmacion = null, errorNuevaContrasena = null) }
    }

    fun onConfirmarContrasenaCambia(valor: String){
        _uiState.update { it.copy(confirmarContrasena = valor, errorConfirmacion = null ) }
    }

    fun onAlteranConfirmacionVisible(){
        _uiState.update { it.copy(nuevaContrasenaVisible = !it.nuevaContrasenaVisible) }
    }

    fun onEnviarSms(): String? {
        val estado = _uiState.value
        if (estado.correo.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(estado.correo).matches()){
            _uiState.update { it.copy(errorCorreo = "Ingresa un correo válido") }
            return null
        }

        if (!dbHelper.verificarCorreoExistente(estado.correo)){
            _uiState.update { it.copy(errorCorreo = "Este correo no esta registrado") }
            return null
        }

        val codigoSimulado = (100000..999999).random().toString()

        _uiState.update {
            it.copy(
                pasoActual = 2,
                codigoGenerado = codigoSimulado
            )
        }
        return codigoSimulado
    }

    fun onVerificarCodigoSms(): Boolean{
        val estado = _uiState.value
        if (estado.codigoSmsIngresado == estado.codigoGenerado){
            _uiState.update { it.copy(pasoActual = 3) }
            return true
        }else {
            _uiState.update { it.copy(errorCodigoSms = "Codigo incorrecto") }
            return false
        }
    }

    fun onCrearContrasena(): Boolean{
        val estado = _uiState.value

        val errorNuevaContrasena = when{
            estado.nuevaContrasena.isBlank() -> "La contraseña es obligatoria"
            estado.nuevaContrasena.length < 8 -> "Debe tener al menos 8 caracteres"
            else -> null
        }

        val errorConfirmacion = when {
            estado.confirmarContrasena.isBlank() -> "Debes confirmar la contraseña"
            estado.nuevaContrasena != estado.confirmarContrasena -> "las contraseñas no coinciden"
            else -> null
        }

        if (errorNuevaContrasena == null && errorConfirmacion == null){
            val hash = BCrypt.withDefaults().hashToString(12, estado.nuevaContrasena.toCharArray())
            val exito = dbHelper.actualizarContrasena(estado.correo, hash)

            _uiState.update { it.copy(recuperacionExitosa = exito) }
            return exito
        } else {
            _uiState.update {
                it.copy(errorNuevaContrasena = errorNuevaContrasena, errorConfirmacion = errorConfirmacion)
            }
            return false
        }
    }

}
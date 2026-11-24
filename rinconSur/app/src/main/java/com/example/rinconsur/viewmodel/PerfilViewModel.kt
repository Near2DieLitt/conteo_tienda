package com.example.rinconsur.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.ConvertidorImagen
import com.example.rinconsur.data.RinconSurRepository
import com.example.rinconsur.model.Perfiles
import com.example.rinconsur.model.ValidadorClave
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val nombre: String = "",
    val cargo: String = "",
    val fotoBitmap: Bitmap? = null,
    val descripcion: String = "",
    val miembrosEquipo: String = "",
    val sesionesCompletadas: String = "",
    val permisosCodigo: String = "",
    val datosPendientes: String = "0 pendientes",
    val estadoNotificaciones: String = "Activadas",
    val errorCambioClave: String? = null
)

class PerfilViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RinconSurRepository()
    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun cargarDatosUsuario(correo: String) {
        if (correo.isBlank()) return
        viewModelScope.launch {
            try {
                val dto = repository.buscarUsuarioPorCorreo(correo)
                val usuario = dto.aUsuario()
                val perfil = Perfiles.deCodigo(usuario.perfil)
                val miembros = try { repository.listarUsuarios().count { it.activo } } catch (_: Exception) { 1 }
                val completadasCount = try { repository.listarSesiones().count { it.completada } } catch (_: Exception) { 0 }
                val bitmapGuardado = usuario.avatar?.let { ConvertidorImagen.aBitmap(it) }

                _uiState.update {
                    it.copy(
                        nombre = usuario.nombre,
                        cargo = perfil.nombre,
                        miembrosEquipo = if (miembros == 1) "1 miembro" else "$miembros miembros",
                        sesionesCompletadas = "$completadasCount",
                        permisosCodigo = perfil.codigo,
                        datosPendientes = "0 pendientes",
                        fotoBitmap = bitmapGuardado,
                        descripcion = usuario.descripcion ?: ""
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun actualizarFoto(bitmap: Bitmap, correo: String) {
        _uiState.update { it.copy(fotoBitmap = bitmap) }
        val bytes = ConvertidorImagen.aBytes(bitmap)
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        viewModelScope.launch {
            try { repository.actualizarPerfilPropio(correo, avatarBase64 = b64) } catch (_: Exception) { }
        }
    }

    fun actualizarDescripcion(correo: String, nuevaDescripcion: String) {
        _uiState.update { it.copy(descripcion = nuevaDescripcion) }
        viewModelScope.launch {
            try { repository.actualizarPerfilPropio(correo, descripcion = nuevaDescripcion) } catch (_: Exception) { }
        }
    }

    fun limpiarErrorClave() {
        _uiState.update { it.copy(errorCambioClave = null) }
    }

    fun cambiarContrasena(
        correo: String,
        claveActual: String,
        claveNueva: String,
        confirmar: String,
        esPrimerInicio: Boolean = false,
        onExito: () -> Unit = {}
    ) {
        if (claveActual.isBlank()) {
            _uiState.update { it.copy(errorCambioClave = "Ingresa tu contraseña actual") }
            return
        }
        if (claveActual == claveNueva) {
            _uiState.update { it.copy(errorCambioClave = "La nueva contraseña debe ser diferente a la actual") }
            return
        }
        val errorRequisitos = ValidadorClave.validar(claveNueva)
        if (errorRequisitos != null) {
            _uiState.update { it.copy(errorCambioClave = errorRequisitos) }
            return
        }
        if (claveNueva != confirmar) {
            _uiState.update { it.copy(errorCambioClave = "La confirmación no coincide") }
            return
        }

        viewModelScope.launch {
            try {
                repository.cambiarClave(correo, claveActual, claveNueva, esPrimerInicio)
                _uiState.update { it.copy(errorCambioClave = null) }
                onExito()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorCambioClave = e.message ?: "No se pudo actualizar la contraseña") }
            }
        }
    }
}

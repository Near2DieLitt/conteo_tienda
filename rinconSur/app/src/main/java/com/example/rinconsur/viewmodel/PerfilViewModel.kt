package com.example.rinconsur.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.ConvertidorImagen
import com.example.rinconsur.data.UsuarioDbHelper
import com.example.rinconsur.model.Perfiles
import com.example.rinconsur.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class PerfilUiState(
    val nombre: String = "",
    val cargo: String = "",
    val fotoBitmap: Bitmap? = null,
    val descripcion: String = "",
    val miembrosEquipo: String = "",
    val sesionesCompletadas: String = "",
    val permisosCodigo: String = "",
    val datosPendientes: String = "",
    val estadoNotificaciones: String = "Activadas"
)

class PerfilViewModel(application: Application): AndroidViewModel(application){

    private val dbHelper = UsuarioDbHelper(application)

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun cargarDatosUsuario(correo: String){
        val usuario = dbHelper.obtenerUsuarioPorCorreo(correo)

        if (usuario != null){
            val perfil = Perfiles.deCodigo(usuario.perfil)
            val miembros = dbHelper.obtenerColaboradores().count { it.activo }

            val bitmapGuardado = if (usuario.avatar != null){
                ConvertidorImagen.aBitmap(usuario.avatar)
            }else null

            val completadasCount = dbHelper.contarSesionesCompletadas()

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
        }
    }

    fun actualizarFoto(bitmap: Bitmap, correo: String){
        _uiState.update { it.copy(fotoBitmap = bitmap) }

        val bytes = ConvertidorImagen.aBytes(bitmap)

        dbHelper.actualizarAvatar(correo, bytes)
    }

    fun actualizarDescripcion(correo: String, nuevaDescripcion: String){
        dbHelper.actualizarDescripcion(correo, nuevaDescripcion)
        _uiState.update { currentState ->
            currentState.copy(descripcion = nuevaDescripcion)
        }
    }

}
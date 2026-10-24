package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.UsuarioDbHelper
import com.example.rinconsur.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



data class ItemSessionUi(
    val id: Long,
    val titulo: String,
    val horaUbicacion: String,
    val estado: String,
    val completada: Boolean = false,
    val mostrarBotonIr: Boolean = false
)

data class MenuprincipalUiState(
    val nombreUsuario: String = "",
    val correoUsuario: String = "",
    val rolUsuario: RolUsuario = RolUsuario.OBSERVADOR,
    val busqueda: String = "",
    val tabSeleccionada: Int = 0,
    val sesiones: List<ItemSessionUi> = emptyList(),
    val flujoGeneralTotal: String = "5.735",
    val porcentajeMeta: Float = 0.78f,
    val textoMeta: String = "78% de la meta",
    val zonasFlujo: String = "A1 - A2 - A3 - A4"
)

class MenuPrincipalViewmodel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = UsuarioDbHelper(application)

    private val _uiState = MutableStateFlow(MenuprincipalUiState())
    val uiState: StateFlow<MenuprincipalUiState> = _uiState.asStateFlow()

    init {
        cargarSessiones()
    }

    fun cargarDatosUsuarios(correo: String) {
        val usuario = dbHelper.obtenerUsuarioPorCorreo(correo)
        if (usuario != null) {
            _uiState.update { 
                it.copy(
                    nombreUsuario = usuario.nombre,
                    correoUsuario = usuario.correo,
                    rolUsuario = usuario.rol
                ) 
            }
        } else {
            _uiState.update { 
                it.copy(
                    nombreUsuario = "Usuario",
                    correoUsuario = correo,
                    rolUsuario = RolUsuario.OBSERVADOR
                ) 
            }
        }
    }


    fun cargarSessiones() {
        val listaSessiones = listOf(
            ItemSessionUi(1,"Acceso Estacionamiento", "11:00 A3", "Finalizada", completada = true),
            ItemSessionUi(2,"Acceso conexión", "13:05 A4", "Validada", completada = false),
            ItemSessionUi(3,"Acceso conexión", "16:00 A4", "Programada", completada = false),
            ItemSessionUi(4,"Acceso Lateral", "17:00 A2", "Programada", completada = false),
        )
        _uiState.update { it.copy(sesiones = listaSessiones) }
    }

    fun actualizarBusqueda(nuevaBusqueda: String) {
        _uiState.update { it.copy(busqueda = nuevaBusqueda) }
    }

    fun seleccionarTab(indice: Int) {
        _uiState.update { it.copy(tabSeleccionada = indice) }
    }

    fun alternarCompletado(idSesion: Long) {
        _uiState.update { estado ->
            val listaActualizada = estado.sesiones.map { sesion ->
                if (sesion.id == idSesion) sesion.copy(completada = !sesion.completada)
                else sesion
            }
            estado.copy(sesiones = listaActualizada)
        }
    }
}
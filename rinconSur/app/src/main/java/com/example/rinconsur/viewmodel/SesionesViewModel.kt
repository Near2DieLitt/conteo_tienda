package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.Fechas
import com.example.rinconsur.data.UsuarioDbHelper
import com.example.rinconsur.model.SesionAcceso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
data class SesionesUiState(
    val tab: Int = 0,
    val fechaSeleccionada: String = "",
    val aniosVisible: Int = 0,
    val mesVisible: Int = 0,
    val sesiones: List<SesionAcceso> = emptyList(),
    val usuariosDisponibles: List<Pair<String, String>> = emptyList()
)

class SesionesViewModel (application: Application): AndroidViewModel(application){
    private val db = UsuarioDbHelper(application)

    private var puedeEditar = false

    private val _uiState = MutableStateFlow(estadoInicial())
    val uiState: StateFlow<SesionesUiState> = _uiState.asStateFlow()

    init {
        recargar()
        cargarUsuarios()
    }

    private fun estadoInicial(): SesionesUiState {
        val hoy = Fechas.hoy()
        val c = Fechas.deTexto(hoy)
        return SesionesUiState(
            fechaSeleccionada = hoy,
            aniosVisible = c.get(Calendar.YEAR),
            mesVisible = c.get(Calendar.MONTH)
        )
    }

    fun cargarUsuarios() {
        val lista = db.obtenerUsuariosListado()
        _uiState.update { it.copy(usuariosDisponibles = lista) }
    }

    fun definirPermisos(puede: Boolean) {
        puedeEditar = puede
        recargar()
    }

    private fun recargar() {
        val s = _uiState.value
        val lista = when (s.tab) {
            0 -> db.obtenerSesiones(s.fechaSeleccionada, s.fechaSeleccionada, puedeEditar)
            1 -> db.obtenerSesiones(
                Fechas.inicioSemana(s.fechaSeleccionada),
                Fechas.finSemana(s.fechaSeleccionada),
                puedeEditar
            )
            else -> db.obtenerSesiones("0000-01-01", Fechas.ayer(), puedeEditar)
        }
        _uiState.update { it.copy(sesiones = lista) }
    }

    fun seleccionarTab(tab: Int) {
        _uiState.update { it.copy(tab = tab) }
        recargar()
    }

    fun seleccionarDia(dia: Int) {
        _uiState.update {
            it.copy(fechaSeleccionada = Fechas.fecha(it.aniosVisible, it.mesVisible, dia))
        }
        recargar()
    }

    fun cambiarMes(delta: Int) {
        _uiState.update {
            val c = Calendar.getInstance().apply {
                set(it.aniosVisible, it.mesVisible, 1)
                add(Calendar.MONTH, delta)
            }
            it.copy(aniosVisible = c.get(Calendar.YEAR), mesVisible = c.get(Calendar.MONTH))
        }
    }


    fun crearSesion(codigo: String, nombre: String, usuarioAsignado: String? = null): Boolean {
        if (!puedeEditar) return false
        val codigoLimpio = codigo.trim().uppercase()
        val entradasValidas = setOf("A1", "A2", "A3", "A4")
        if (!entradasValidas.contains(codigoLimpio)) return false

        val fecha = _uiState.value.fechaSeleccionada
        if (fecha < Fechas.hoy()) return false
        db.insertarSesion(codigoLimpio, nombre.trim(), fecha, usuarioAsignado)
        recargar()
        return true
    }

    fun cambiarActivoSesion(idSesion: Long, activo: Boolean) {
        if (!puedeEditar) return
        db.cambiarActivoSesion(idSesion, activo)
        recargar()
    }



    fun cambiarActivoConteo(idConteo: Long, activo: Boolean) {
        if (!puedeEditar) return
        db.cambiarActivoConteo(idConteo, activo)
        recargar()
    }

    fun cambiarEstadoConteo(idConteo: Long, completado: Boolean) {
        if (!puedeEditar) return
        db.cambiarEstadoConteo(idConteo, completado)
        recargar()
    }
}

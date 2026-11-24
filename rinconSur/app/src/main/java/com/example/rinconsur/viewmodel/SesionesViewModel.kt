package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.Fechas
import com.example.rinconsur.data.RinconSurRepository
import com.example.rinconsur.data.remote.UsuarioRemotoDto
import com.example.rinconsur.model.SesionAcceso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class SesionesUiState(
    val tab: Int = 0,
    val fechaSeleccionada: String = "",
    val aniosVisible: Int = 0,
    val mesVisible: Int = 0,
    val sesiones: List<SesionAcceso> = emptyList(),
    val usuariosDisponibles: List<Pair<String, String>> = emptyList()
)

class SesionesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RinconSurRepository()

    private var puedeCrearSesiones = false
    private var puedeHacerConteos = false
    private var usuariosCache: List<UsuarioRemotoDto> = emptyList()

    private val _uiState = MutableStateFlow(estadoInicial())
    val uiState: StateFlow<SesionesUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarios()
        recargar()
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
        viewModelScope.launch {
            try {
                val todos = repository.listarUsuarios()
                usuariosCache = todos
                val lista = todos.filter { it.activo }
                    .sortedBy { it.nombre }
                    .map { Pair(it.correo, it.nombre) }
                _uiState.update { it.copy(usuariosDisponibles = lista) }
            } catch (_: Exception) { }
        }
    }

    fun definirPermisos(puedeSesiones: Boolean, puedeConteos: Boolean = false) {
        puedeCrearSesiones = puedeSesiones
        puedeHacerConteos = puedeConteos
        cargarUsuarios()
        recargar()
    }

    private fun recargar() {
        val s = _uiState.value
        val (desde, hasta) = when (s.tab) {
            0 -> Pair(s.fechaSeleccionada, s.fechaSeleccionada)
            1 -> Pair(Fechas.inicioSemana(s.fechaSeleccionada), Fechas.finSemana(s.fechaSeleccionada))
            else -> Pair("2000-01-01", Fechas.ayer())
        }

        viewModelScope.launch {
            try {
                if (usuariosCache.isEmpty()) {
                    usuariosCache = try { repository.listarUsuarios() } catch (_: Exception) { emptyList() }
                }
                val mapaNombres = usuariosCache.associate { it.id to it.nombre }
                val sesionesRemotas = repository.listarSesiones(
                    desde = desde,
                    hasta = hasta,
                    incluirInactivas = puedeCrearSesiones
                )
                val flujos = try {
                    repository.listarFlujo(desde, hasta, incluirInactivos = false)
                } catch (_: Exception) {
                    emptyList()
                }
                val flujoPorClave = flujos.groupBy { "${it.fecha}_${it.acceso.uppercase()}" }

                val lista = sesionesRemotas.map { dto ->
                    val flujosSesion = flujoPorClave["${dto.fecha}_${dto.codigo.uppercase()}"].orEmpty()
                    SesionAcceso(
                        id = dto.id ?: 0L,
                        codigo = dto.codigo,
                        nombre = dto.nombre,
                        fecha = dto.fecha,
                        activo = dto.activo,
                        usuarioAsignado = dto.idUsuarioAsignado?.let { mapaNombres[it] },
                        transitoJoven = flujosSesion.sumOf { it.transitoJoven },
                        transitoAdulto = flujosSesion.sumOf { it.transitoAdulto },
                        transitoMayor = flujosSesion.sumOf { it.transitoMayor },
                        totalIngresos = flujosSesion.sumOf { it.ingresos },
                        conteos = emptyList()
                    )
                }
                _uiState.update { it.copy(sesiones = lista) }
            } catch (_: Exception) { }
        }
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

    fun crearSesion(
        codigo: String,
        nombre: String,
        usuarioAsignado: String? = null,
        onResultado: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (!puedeCrearSesiones) {
            onResultado(false, "Solo un supervisor puede crear sesiones")
            return
        }
        val codigoLimpio = codigo.trim().uppercase()
        val entradasValidas = setOf("A1", "A2", "A3", "A4")
        if (!entradasValidas.contains(codigoLimpio)) {
            onResultado(false, "Código de acceso inválido")
            return
        }

        val fecha = _uiState.value.fechaSeleccionada
        if (fecha < Fechas.hoy()) {
            onResultado(false, "No se puede programar en una fecha pasada")
            return
        }

        viewModelScope.launch {
            try {
                val idUsuario = usuarioAsignado?.let { correo ->
                    usuariosCache.firstOrNull { it.correo.equals(correo, ignoreCase = true) }?.id
                        ?: try { repository.buscarUsuarioPorCorreo(correo).id } catch (_: Exception) { null }
                }
                repository.crearSesion(codigoLimpio, nombre.trim(), fecha, idUsuario)
                recargar()
                onResultado(true, "Sesión creada y asignada")
            } catch (e: Exception) {
                onResultado(false, e.message ?: "Error al crear sesión")
            }
        }
    }

    fun cambiarActivoSesion(idSesion: Long, activo: Boolean) {
        if (!puedeCrearSesiones) return
        viewModelScope.launch {
            try {
                repository.cambiarActivoSesion(idSesion, activo)
                recargar()
            } catch (_: Exception) { }
        }
    }

    fun cambiarActivoConteo(idConteo: Long, activo: Boolean) {
        if (!puedeHacerConteos) return
        recargar()
    }

    fun cambiarEstadoConteo(idConteo: Long, completado: Boolean) {
        if (!puedeHacerConteos) return
        viewModelScope.launch {
            try {
                repository.cambiarCompletadaSesion(idConteo, completado)
                recargar()
            } catch (_: Exception) { }
        }
    }
}

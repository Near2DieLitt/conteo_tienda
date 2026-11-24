package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.Fechas
import com.example.rinconsur.data.RinconSurRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.VisitanteEnTiendaItem
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.model.ResumenVisitantes
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.model.SesionAcceso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val idUsuario: Long = -1L,
    val rolUsuario: RolUsuario = RolUsuario.OBSERVADOR,
    val busqueda: String = "",
    val tabSeleccionada: Int = 0,
    val sesiones: List<ItemSessionUi> = emptyList(),
    val flujoGeneralTotal: String = "0",
    val porcentajeMeta: Float = 0.78f,
    val textoMeta: String = "78% de la meta",
    val zonasFlujo: String = "A1 - A2 - A3 - A4",
    val resumenVisitantes: ResumenVisitantes = ResumenVisitantes(),
    val visitantesEnTienda: List<VisitanteEnTiendaItem> = emptyList(),
    val sesionesEntradaActivas: List<SesionAcceso> = emptyList(),
    val entradaActiva: String? = null,
    val mensajeInformativo: String? = null
) {
    val entradasValidas: Set<String> = setOf("A1", "A2", "A3", "A4")
    val puedeHacerConteos: Boolean get() = rolUsuario.puedeHacerConteos
    val esSesionIniciada: Boolean get() = entradaActiva != null && entradasValidas.contains(entradaActiva)
}

class MenuPrincipalViewmodel(application: Application) : AndroidViewModel(application) {
    private val sesionPrefs = SesionPreferencias(application)
    private val repository = RinconSurRepository()

    private val _uiState = MutableStateFlow(MenuprincipalUiState())
    val uiState: StateFlow<MenuprincipalUiState> = _uiState.asStateFlow()

    fun cargarDatosUsuarios(correo: String) {
        if (correo.isBlank()) return
        viewModelScope.launch {
            try {
                val dto = repository.buscarUsuarioPorCorreo(correo)
                _uiState.update {
                    it.copy(
                        nombreUsuario = dto.nombre,
                        correoUsuario = dto.correo,
                        idUsuario = dto.id,
                        rolUsuario = dto.aRolUsuario()
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        nombreUsuario = it.nombreUsuario.ifBlank { "Usuario" },
                        correoUsuario = correo
                    )
                }
            }
            refrescar()
        }
    }

    fun refrescar() {
        val stateActual = _uiState.value
        if (stateActual.idUsuario == -1L) return

        viewModelScope.launch {
            try {
                val hoy = Fechas.hoy()
                val sesionesHoyUsuario = repository.listarSesiones(
                    fecha = hoy,
                    incluirInactivas = false,
                    idUsuario = stateActual.idUsuario
                )
                val sesionesEntrada = sesionesHoyUsuario.map {
                    SesionAcceso(
                        id = it.id ?: 0L,
                        codigo = it.codigo,
                        nombre = it.nombre,
                        fecha = it.fecha,
                        activo = it.activo,
                        conteos = emptyList()
                    )
                }

                var entrada = sesionPrefs.obtenerEntradaActiva(stateActual.correoUsuario)
                if (sesionesEntrada.isNotEmpty()) {
                    if (entrada == null || sesionesEntrada.none { it.codigo.equals(entrada, ignoreCase = true) }) {
                        entrada = sesionesEntrada.first().codigo
                        sesionPrefs.guardarEntradaActiva(stateActual.correoUsuario, entrada)
                    }
                } else {
                    entrada = null
                    sesionPrefs.borrarEntradaActiva(stateActual.correoUsuario)
                }

                val idSesActiva = entrada?.let { cod ->
                    sesionesEntrada.firstOrNull { it.codigo.equals(cod, ignoreCase = true) }?.id
                }

                val tab = _uiState.value.tabSeleccionada
                val (desde, hasta) = if (tab == 0) Pair(hoy, hoy) else Pair(sumarDias(hoy, 1), "2099-12-31")
                val listaSesiones = repository.listarSesiones(
                    desde = desde,
                    hasta = hasta,
                    incluirInactivas = true,
                    idUsuario = stateActual.idUsuario
                )
                val itemsMapped = listaSesiones.map { s ->
                    val estaCompletada = s.completada || !s.activo
                    val estadoTxt = when {
                        estaCompletada -> "Finalizada"
                        !s.activo -> "Inactiva"
                        else -> "Programada"
                    }
                    ItemSessionUi(
                        id = s.id ?: 0L,
                        titulo = "${s.codigo} · ${s.nombre}",
                        horaUbicacion = "${s.fecha} · Programada",
                        estado = estadoTxt,
                        completada = estaCompletada
                    )
                }
                val busqueda = _uiState.value.busqueda.trim()
                val itemsFiltrados = if (busqueda.isBlank()) itemsMapped else itemsMapped.filter {
                    it.titulo.contains(busqueda, ignoreCase = true) ||
                            it.horaUbicacion.contains(busqueda, ignoreCase = true) ||
                            it.estado.contains(busqueda, ignoreCase = true)
                }

                val resumen = try { repository.obtenerResumenVisitantes(hoy, idSesActiva) } catch (_: Exception) { ResumenVisitantes() }
                val activos = try { repository.listarVisitantesEnTienda(hoy, idSesActiva) } catch (_: Exception) { emptyList() }
                val flujosHoy = try { repository.listarFlujo(hoy, hoy, false) } catch (_: Exception) { emptyList() }
                val totalFlujoHoy = flujosHoy.sumOf { it.ingresos }
                val zonas = try {
                    repository.listarSesiones(fecha = hoy, incluirInactivas = false).map { it.codigo }.distinct()
                } catch (_: Exception) {
                    emptyList()
                }
                val zonasTexto = if (zonas.isEmpty()) "Sin zonas activas" else zonas.joinToString(" - ")

                _uiState.update {
                    it.copy(
                        sesionesEntradaActivas = sesionesEntrada,
                        entradaActiva = entrada,
                        sesiones = itemsFiltrados,
                        resumenVisitantes = resumen,
                        visitantesEnTienda = activos,
                        flujoGeneralTotal = String.format(Locale.US, "%,d", totalFlujoHoy),
                        zonasFlujo = zonasTexto
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun cargarVisitantes() {
        refrescar()
    }

    private fun idSesionActiva(): Long? {
        val entrada = _uiState.value.entradaActiva ?: return null
        return _uiState.value.sesionesEntradaActivas
            .firstOrNull { it.codigo.equals(entrada, ignoreCase = true) }?.id
    }

    fun finalizarSesionEntrada() {
        val idSes = idSesionActiva()
        viewModelScope.launch {
            try {
                if (idSes != null) {
                    repository.finalizarSesion(idSes)
                }
                sesionPrefs.borrarEntradaActiva(_uiState.value.correoUsuario)
                _uiState.update {
                    it.copy(
                        entradaActiva = null,
                        mensajeInformativo = "Sesión de acceso finalizada correctamente"
                    )
                }
                refrescar()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeInformativo = e.message ?: "Error al finalizar sesión") }
            }
        }
    }

    fun registrarEntradaVisitante(rangoEtario: RangoEtario = RangoEtario.ADULTO) {
        if (!_uiState.value.puedeHacerConteos) {
            _uiState.update { it.copy(mensajeInformativo = "Solo el perfil Observador puede realizar conteos de visitantes.") }
            return
        }
        val acceso = _uiState.value.entradaActiva ?: "A1"
        val idSes = idSesionActiva()
        val hoy = Fechas.hoy()
        val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            try {
                repository.registrarEntradaVisitante(hoy, horaActual, acceso, rangoEtario.name, idSes)
                repository.registrarFlujo(
                    acceso = acceso,
                    fecha = hoy,
                    ingresos = 1,
                    compradores = 0,
                    joven = if (rangoEtario == RangoEtario.JOVEN) 1 else 0,
                    adulto = if (rangoEtario == RangoEtario.ADULTO) 1 else 0,
                    mayor = if (rangoEtario != RangoEtario.JOVEN && rangoEtario != RangoEtario.ADULTO) 1 else 0
                )
                refrescar()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeInformativo = e.message ?: "No se pudo registrar el visitante") }
            }
        }
    }

    fun registrarSalidaVisitanteEspecifico(idVisitante: Long) {
        if (!_uiState.value.puedeHacerConteos) {
            _uiState.update { it.copy(mensajeInformativo = "Solo el perfil Observador puede realizar conteos de visitantes.") }
            return
        }
        viewModelScope.launch {
            try {
                repository.registrarSalidaSinCompra(idVisitante)
                _uiState.update { it.copy(mensajeInformativo = "Salida sin compra registrada") }
                refrescar()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeInformativo = e.message ?: "No se pudo registrar la salida") }
            }
        }
    }

    fun ejecutarCierreDiario() {
        if (!_uiState.value.puedeHacerConteos) {
            _uiState.update { it.copy(mensajeInformativo = "Solo el perfil Observador puede ejecutar conteos y cierre.") }
            return
        }
        val idSes = idSesionActiva()
        viewModelScope.launch {
            try {
                val actualizados = repository.ejecutarCierreDiario(Fechas.hoy(), idSes)
                _uiState.update {
                    it.copy(
                        mensajeInformativo = "Cierre diario completado. $actualizados visitante(s) en tienda se marcaron como salida sin compra."
                    )
                }
                refrescar()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeInformativo = e.message ?: "Error al ejecutar el cierre diario") }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeInformativo = null) }
    }

    private fun sumarDias(fecha: String, dias: Int): String {
        val c = Fechas.deTexto(fecha)
        c.add(java.util.Calendar.DAY_OF_MONTH, dias)
        return Fechas.aTexto(c)
    }

    fun seleccionarTab(indice: Int) {
        _uiState.update { it.copy(tabSeleccionada = indice) }
        refrescar()
    }

    fun alternarCompletado(idSesion: Long) {
        if (!_uiState.value.puedeHacerConteos) {
            _uiState.update { it.copy(mensajeInformativo = "Solo el perfil Observador puede marcar conteos de sesiones.") }
            return
        }
        val actual = _uiState.value.sesiones.firstOrNull { it.id == idSesion }?.completada ?: false
        val nuevoEstado = !actual
        viewModelScope.launch {
            try {
                repository.cambiarCompletadaSesion(idSesion, nuevoEstado)
                refrescar()
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeInformativo = e.message ?: "Error al actualizar sesión") }
            }
        }
    }
}

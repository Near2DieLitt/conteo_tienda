package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.Fechas
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.UsuarioDbHelper
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.model.ResumenVisitantes
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.model.SesionAcceso
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
    val zonasFlujo: String = "A1 - A2 - A3 - A4",
    val resumenVisitantes: ResumenVisitantes = ResumenVisitantes(),
    val sesionesEntradaActivas: List<SesionAcceso> = emptyList(),
    val entradaActiva: String? = null,
    val mensajeInformativo: String? = null
) {
    val entradasValidas: Set<String> = setOf("A1", "A2", "A3", "A4")
    val esSesionIniciada: Boolean get() = entradaActiva != null && entradasValidas.contains(entradaActiva)
}

class MenuPrincipalViewmodel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = UsuarioDbHelper(application)
    private val sesionPrefs = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(MenuprincipalUiState())
    val uiState: StateFlow<MenuprincipalUiState> = _uiState.asStateFlow()

    init {
        cargarSessiones()
        cargarSesionesEntrada()
        cargarVisitantes()
    }

    fun cargarSesionesEntrada(correoOverride: String? = null) {
        val correo = correoOverride ?: _uiState.value.correoUsuario
        val esSupervisor = _uiState.value.rolUsuario == RolUsuario.SUPERVISOR
        val correoFiltro = if (esSupervisor) null else correo

        val sesionesActivas = dbHelper.obtenerSesionesActivasEntrada(Fechas.hoy(), correoFiltro)
        var entrada = sesionPrefs.obtenerEntradaActiva()

        if (sesionesActivas.isNotEmpty()) {
            if (entrada == null || sesionesActivas.none { it.codigo.equals(entrada, ignoreCase = true) }) {
                entrada = sesionesActivas.first().codigo
                sesionPrefs.guardarEntradaActiva(entrada)
            }
        } else {
            entrada = null
            sesionPrefs.borrarEntradaActiva()
        }

        _uiState.update { 
            it.copy(
                sesionesEntradaActivas = sesionesActivas,
                entradaActiva = entrada
            ) 
        }
    }

    fun seleccionarSesionEntrada(codigo: String) {
        val codigoLimpio = codigo.trim().uppercase()
        if (_uiState.value.entradasValidas.contains(codigoLimpio)) {
            sesionPrefs.guardarEntradaActiva(codigoLimpio)
            _uiState.update { it.copy(entradaActiva = codigoLimpio) }
            cargarVisitantes()
        }
    }

    fun finalizarSesionEntrada() {
        val entrada = _uiState.value.entradaActiva
        if (!entrada.isNullOrBlank()) {
            dbHelper.finalizarSesionPorCodigo(entrada, Fechas.hoy())
        }
        sesionPrefs.borrarEntradaActiva()
        _uiState.update { it.copy(entradaActiva = null) }
        cargarSesionesEntrada()
        cargarSessiones()
        cargarVisitantes()
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
        cargarSesionesEntrada(correo)
        cargarSessiones()
        cargarVisitantes()
    }

    fun cargarVisitantes() {
        val resumen = dbHelper.obtenerResumenVisitantes()
        _uiState.update { it.copy(resumenVisitantes = resumen) }
    }

    fun registrarEntradaVisitante(rangoEtario: RangoEtario = RangoEtario.ADULTO) {
        dbHelper.registrarEntradaVisitante()
        val acceso = _uiState.value.entradaActiva
        if (!acceso.isNullOrBlank()) {
            dbHelper.registrarOActualizarFlujo(
                acceso = acceso,
                deltaIngresos = 1,
                deltaCompradores = 0,
                rangoEtario = rangoEtario
            )
        }
        cargarVisitantes()
    }

    fun registrarSalidaSinCompra() {
        val exito = dbHelper.registrarSalidaVisitanteSinCompra()
        if (!exito) {
            _uiState.update { it.copy(mensajeInformativo = "No hay visitantes activos con estado 'En_Tienda'.") }
        }
        cargarVisitantes()
    }

    fun ejecutarCierreDiario() {
        val actualizados = dbHelper.ejecutarCierreDiario()
        _uiState.update { 
            it.copy(
                mensajeInformativo = "Cierre diario ejecutado. $actualizados visitante(s) pasaron de 'En_Tienda' a 'No_Compro'."
            ) 
        }
        cargarVisitantes()
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensajeInformativo = null) }
    }

    private fun sumarDias(fecha: String, dias: Int): String {
        val c = Fechas.deTexto(fecha)
        c.add(java.util.Calendar.DAY_OF_MONTH, dias)
        return Fechas.aTexto(c)
    }

    fun cargarSessiones() {
        val hoy = Fechas.hoy()
        val tab = _uiState.value.tabSeleccionada
        val busqueda = _uiState.value.busqueda.trim()

        val (desde, hasta) = if (tab == 0) {
            Pair(hoy, hoy)
        } else {
            Pair(sumarDias(hoy, 1), "2099-12-31")
        }
        val esSupervisor = _uiState.value.rolUsuario == RolUsuario.SUPERVISOR
        val correoFiltro = if (esSupervisor) null else _uiState.value.correoUsuario
        val listaDb = dbHelper.obtenerSesiones(desde, hasta, incluirInactivas = true, correoUsuario = correoFiltro)

        val itemsMapped = listaDb.map { sesion ->
            val primerHorario = sesion.conteos.firstOrNull()?.horario ?: "Programada"
            val totalConteos = sesion.conteos.size
            val completados = sesion.conteos.count { it.completado }
            val estadoTxt = if (totalConteos > 0 && completados == totalConteos) "Finalizada"
            else if (completados > 0) "En Progreso"
            else if (!sesion.activo) "Inactiva"
            else "Programada"

            ItemSessionUi(
                id = sesion.id,
                titulo = "${sesion.codigo} · ${sesion.nombre}",
                horaUbicacion = "${sesion.fecha} · $primerHorario",
                estado = estadoTxt,
                completada = !sesion.activo || (totalConteos > 0 && completados == totalConteos)
            )
        }

        val itemsFiltrados = if (busqueda.isBlank()) itemsMapped else itemsMapped.filter {
            it.titulo.contains(busqueda, ignoreCase = true) ||
                    it.horaUbicacion.contains(busqueda, ignoreCase = true) ||
                    it.estado.contains(busqueda, ignoreCase = true)
        }

        val resumenFlujo = dbHelper.obtenerResumenFlujo(hoy, hoy, null)
        val zonas = dbHelper.obtenerSesionesActivasEntrada(hoy).map { it.codigo }.distinct()
        val zonasTexto = if (zonas.isEmpty()) "Sin zonas activas" else zonas.joinToString(" - ")

        _uiState.update {
            it.copy(
                sesiones = itemsFiltrados,
                flujoGeneralTotal = String.format(java.util.Locale.US, "%,d", resumenFlujo.ingresos),
                zonasFlujo = zonasTexto
            )
        }
    }

    fun actualizarBusqueda(nuevaBusqueda: String) {
        _uiState.update { it.copy(busqueda = nuevaBusqueda) }
        cargarSessiones()
    }

    fun seleccionarTab(indice: Int) {
        _uiState.update { it.copy(tabSeleccionada = indice) }
        cargarSessiones()
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
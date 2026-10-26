package com.example.rinconsur.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.Fechas
import com.example.rinconsur.data.UsuarioDbHelper
import com.example.rinconsur.model.Accesos
import com.example.rinconsur.model.RegistroFlujo
import com.example.rinconsur.model.ResumenFlujo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar
import kotlin.math.roundToInt
import kotlin.random.Random

data class FormFlujoState(
    val visible: Boolean = false,
    val acceso: String = "A1",
    val fecha: String = "",
    val ingresos: String = "",
    val compradores: String = "",
    val transitoJoven: String = "",
    val transitoAdulto: String = "",
    val transitoMayor: String = "",
    val errores: Map<String, String> = emptyMap()
)

data class AnaliticaUiState(
    val periodo: Int = 0,
    val personalizadoDesde: String = "",
    val personalizadoHasta: String = "",
    val rangoDesde: String = "",
    val rangoHasta: String = "",
    val acceso: String? = null,
    val modoComparar: Int = 0,
    val resumen: ResumenFlujo = ResumenFlujo(),
    val crecimiento: Float? = null,
    val duracionPromedioMin: Int? = null,
    val cantidadSesiones: Int = 0,
    val porAcceso: Map<String, ResumenFlujo> = emptyMap(),
    val registros: List<RegistroFlujo> = emptyList(),
    val form: FormFlujoState = FormFlujoState()
)

object ValidadorRegistroFlujo {
    fun validar(f: FormFlujoState, hoy: String): Map<String, String> {
        val errores = mutableMapOf<String, String>()

        if (f.fecha.isBlank()) {
            errores["fecha"] = "Selecciona una fecha"
        } else if (f.fecha > hoy) {
            errores["fecha"] = "No puedes registrar fechas futuras"
        }

        val ingresos = f.ingresos.toIntOrNull()
        if (f.ingresos.isBlank()) {
            errores["ingresos"] = "Ingresa el total de ingresos"
        } else if (ingresos == null || ingresos <= 0) {
            errores["ingresos"] = "Debe ser un número mayor a 0"
        }

        val compradores = f.compradores.toIntOrNull() ?: 0
        val joven = f.transitoJoven.toIntOrNull() ?: 0
        val adulto = f.transitoAdulto.toIntOrNull() ?: 0
        val mayor = f.transitoMayor.toIntOrNull() ?: 0
        val suma = compradores + joven + adulto + mayor

        if (ingresos != null && ingresos > 0 && suma > ingresos) {
            errores["total"] = "Compradores + tránsito ($suma) no pueden superar los ingresos ($ingresos)"
        }

        return errores
    }
}

class AnaliticaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = UsuarioDbHelper(application)

    private var puedeEditar = false

    private val _uiState = MutableStateFlow(estadoInicial())
    val uiState: StateFlow<AnaliticaUiState> = _uiState.asStateFlow()

    init {
        recargar()
    }

    private fun estadoInicial(): AnaliticaUiState {
        val hoy = Fechas.hoy()
        return AnaliticaUiState(
            personalizadoDesde = sumarDias(hoy, -6),
            personalizadoHasta = hoy
        )
    }

    fun definirPermisos(puede: Boolean) {
        puedeEditar = puede
        recargar()
    }

    private fun sumarDias(fecha: String, dias: Int): String {
        val c = Fechas.deTexto(fecha)
        c.add(Calendar.DAY_OF_MONTH, dias)
        return Fechas.aTexto(c)
    }

    private fun diasEntre(desde: String, hasta: String): Int {
        val a = Fechas.deTexto(desde).timeInMillis
        val b = Fechas.deTexto(hasta).timeInMillis
        return ((b - a) / 86_400_000.0).roundToInt() + 1
    }

    private fun rango(s: AnaliticaUiState): Pair<String, String> {
        val hoy = Fechas.hoy()
        return when (s.periodo) {
            0 -> Pair(sumarDias(hoy, -6), hoy)
            1 -> Pair(sumarDias(hoy, -29), hoy)
            else -> Pair(s.personalizadoDesde, s.personalizadoHasta)
        }
    }

    private fun aMinutos(hora: String): Int? {
        val p = hora.split(":")
        if (p.size != 2) return null
        val h = p[0].trim().toIntOrNull() ?: return null
        val m = p[1].trim().toIntOrNull() ?: return null
        return h * 60 + m
    }

    private fun duracionMinutos(horario: String): Int? {
        val partes = horario.split("–", "-").map { it.trim() }
        if (partes.size != 2) return null
        val inicio = aMinutos(partes[0]) ?: return null
        val fin = aMinutos(partes[1]) ?: return null
        return if (fin > inicio) fin - inicio else null
    }

    private fun recargar() {
        val s = _uiState.value
        val (desde, hasta) = rango(s)

        val dias = diasEntre(desde, hasta)
        val previoHasta = sumarDias(desde, -1)
        val previoDesde = sumarDias(previoHasta, -(dias - 1))

        val actual = db.obtenerResumenFlujo(desde, hasta, s.acceso)
        val previo = db.obtenerResumenFlujo(previoDesde, previoHasta, s.acceso)
        val crecimiento = if (previo.ingresos == 0) null
        else (actual.ingresos - previo.ingresos) * 100f / previo.ingresos

        val duraciones = db.obtenerHorariosConteos(desde, hasta, s.acceso)
            .mapNotNull { duracionMinutos(it) }
        val promedio = if (duraciones.isEmpty()) null else duraciones.average().roundToInt()

        _uiState.update {
            it.copy(
                rangoDesde = desde,
                rangoHasta = hasta,
                resumen = actual,
                crecimiento = crecimiento,
                duracionPromedioMin = promedio,
                cantidadSesiones = db.contarSesiones(desde, hasta, s.acceso),
                porAcceso = db.obtenerFlujoPorAcceso(desde, hasta),
                registros = db.obtenerRegistrosFlujo(desde, hasta, s.acceso, puedeEditar, 8)
            )
        }
    }

    fun seleccionarPeriodo(indice: Int) {
        _uiState.update { it.copy(periodo = indice) }
        recargar()
    }

    fun aplicarRango(desde: String, hasta: String) {
        val inicio = if (desde <= hasta) desde else hasta
        val fin = if (desde <= hasta) hasta else desde
        _uiState.update {
            it.copy(periodo = 2, personalizadoDesde = inicio, personalizadoHasta = fin)
        }
        recargar()
    }

    fun seleccionarAcceso(acceso: String?) {
        _uiState.update { it.copy(acceso = acceso) }
        recargar()
    }

    fun seleccionarModo(modo: Int) {
        _uiState.update { it.copy(modoComparar = modo) }
    }

    fun abrirFormulario() {
        if (!puedeEditar) return
        _uiState.update { it.copy(form = FormFlujoState(visible = true, fecha = Fechas.hoy())) }
    }

    fun cerrarFormulario() {
        _uiState.update { it.copy(form = FormFlujoState()) }
    }

    fun cambiarCampo(campo: String, valor: String) {
        _uiState.update { s ->
            val f = s.form
            val limpio = if (campo == "acceso" || campo == "fecha") valor
            else valor.filter { it.isDigit() }.take(7)

            val nuevo = when (campo) {
                "acceso" -> f.copy(acceso = limpio)
                "fecha" -> f.copy(fecha = limpio)
                "ingresos" -> f.copy(ingresos = limpio)
                "compradores" -> f.copy(compradores = limpio)
                "transitoJoven" -> f.copy(transitoJoven = limpio)
                "transitoAdulto" -> f.copy(transitoAdulto = limpio)
                "transitoMayor" -> f.copy(transitoMayor = limpio)
                else -> f
            }
            s.copy(form = nuevo.copy(errores = nuevo.errores - campo - "total"))
        }
    }

    fun guardarRegistro(): Boolean {
        if (!puedeEditar) return false
        val f = _uiState.value.form
        val errores = ValidadorRegistroFlujo.validar(f, Fechas.hoy())

        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(form = f.copy(errores = errores)) }
            return false
        }

        db.insertarRegistroFlujo(
            RegistroFlujo(
                id = 0,
                acceso = f.acceso,
                fecha = f.fecha,
                ingresos = f.ingresos.toInt(),
                compradores = f.compradores.toIntOrNull() ?: 0,
                transitoJoven = f.transitoJoven.toIntOrNull() ?: 0,
                transitoAdulto = f.transitoAdulto.toIntOrNull() ?: 0,
                transitoMayor = f.transitoMayor.toIntOrNull() ?: 0,
                activo = true
            )
        )
        _uiState.update { it.copy(form = FormFlujoState()) }
        recargar()
        return true
    }

    fun cambiarActivoRegistro(idRegistro: Long, activo: Boolean) {
        if (!puedeEditar) return
        db.cambiarActivoRegistroFlujo(idRegistro, activo)
        recargar()
    }

    fun cargarDatosDePrueba(): Boolean {
        if (!puedeEditar) return false
        if (db.cantidadRegistrosFlujo() > 0) return false

        val azar = Random(2026)
        val hoy = Fechas.hoy()
        val basesDiarias = listOf(190, 115, 85, 120)
        val lista = mutableListOf<RegistroFlujo>()

        for (dia in 0 until 60) {
            val fecha = sumarDias(hoy, -dia)
            val tendencia = 1.0 + (60 - dia) / 600.0

            Accesos.lista.forEachIndexed { i, acceso ->
                val ingresos = (basesDiarias[i] * tendencia * (0.75 + azar.nextDouble() * 0.5)).toInt()
                val compradores = (ingresos * (0.10 + azar.nextDouble() * 0.10)).toInt()
                val transito = (ingresos * (0.35 + azar.nextDouble() * 0.15)).toInt()
                val joven = (transito * 0.30).toInt()
                val adulto = (transito * 0.45).toInt()
                val mayor = transito - joven - adulto

                lista.add(
                    RegistroFlujo(
                        id = 0,
                        acceso = acceso,
                        fecha = fecha,
                        ingresos = ingresos,
                        compradores = compradores,
                        transitoJoven = joven,
                        transitoAdulto = adulto,
                        transitoMayor = mayor,
                        activo = true
                    )
                )
            }
        }

        db.insertarRegistrosFlujo(lista)
        recargar()
        return true
    }
}
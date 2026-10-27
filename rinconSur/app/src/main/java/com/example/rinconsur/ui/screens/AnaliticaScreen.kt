package com.example.rinconsur.ui.screens


import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.Fechas
import com.example.rinconsur.model.Accesos
import com.example.rinconsur.model.RegistroFlujo
import com.example.rinconsur.model.ResumenFlujo
import com.example.rinconsur.ui.components.BarraNavegacionInferior
import com.example.rinconsur.ui.components.DestinoBarra
import com.example.rinconsur.ui.theme.*
import com.example.rinconsur.viewmodel.AnaliticaViewModel
import com.example.rinconsur.viewmodel.FormFlujoState
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val ColorTransito = Ambar600
private val ColorSinTransito = Color(0xFF0F7A73)
private val FondoBeige = Color(0xFFF0ECE3)
private val FondoGrafico = Color(0xFFE8F0EA)
private val localeCl: Locale = Locale.forLanguageTag("es-CL")

private fun fmtNumero(n: Int): String = String.format(localeCl, "%,d", n)
private fun fmtPorc(p: Float): String = String.format(localeCl, "%.1f%%", p)
private fun fmtFechaCorta(f: String): String =
    if (f.length < 10) "" else "${f.substring(8, 10)}/${f.substring(5, 7)}/${f.substring(0, 4)}"

private fun millisAFecha(ms: Long): String {
    val c = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = ms }
    return Fechas.fecha(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
}

@Composable
fun AnaliticaScreen(
    puedeEditar: Boolean,
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit,
    onNavegar: (DestinoBarra) -> Unit,
    viewModel: AnaliticaViewModel = viewModel()
) {
    LaunchedEffect(puedeEditar) {
        viewModel.definirPermisos(puedeEditar)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var mostrarRango by remember { mutableStateOf(false) }
    var menuAbierto by remember { mutableStateOf(false) }

    if (mostrarRango) {
        DialogoRangoFechas(
            onCancelar = { mostrarRango = false },
            onAplicar = { desde, hasta ->
                viewModel.aplicarRango(desde, hasta)
                mostrarRango = false
            }
        )
    }

    if (state.form.visible) {
        DialogoRegistroFlujo(
            form = state.form,
            onCambio = { campo, valor -> viewModel.cambiarCampo(campo, valor) },
            onCancelar = { viewModel.cerrarFormulario() },
            onGuardar = {
                if (viewModel.guardarRegistro()) {
                    Toast.makeText(context, "Conteo registrado", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    val resumen = state.resumen
    val nombresPeriodo = listOf("Semana", "Mes", "Personalizado")
    val tituloResumen = when (state.periodo) {
        0 -> "Resumen semanal"
        1 -> "Resumen mensual"
        else -> "Resumen del período"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BarraNavegacionInferior(
                seleccionado = DestinoBarra.ANALITICA,
                onNavegar = onNavegar,
                modoOscuro = modoOscuro
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Encabezado
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.rotulo_rincon_sur),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Analítica",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (modoOscuro) Color.White else Petroleo800
                    )
                }
                IconButton(
                    onClick = onCambiarTema,
                    modifier = Modifier
                        .background(if (modoOscuro) Color(0xFF163E45) else Superficie, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (modoOscuro) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = stringResource(id = R.string.desc_cambiar_tema),
                        tint = if (modoOscuro) Color.White else Petroleo800
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SelectorCampo(
                    etiqueta = "Período",
                    valor = nombresPeriodo[state.periodo],
                    opciones = nombresPeriodo,
                    onSeleccion = { i ->
                        if (i == 2) mostrarRango = true else viewModel.seleccionarPeriodo(i)
                    },
                    modoOscuro = modoOscuro,
                    modifier = Modifier.weight(1f)
                )
                SelectorCampo(
                    etiqueta = "Acceso",
                    valor = state.acceso ?: "Todos",
                    opciones = listOf("Todos") + Accesos.lista,
                    onSeleccion = { i ->
                        viewModel.seleccionarAcceso(if (i == 0) null else Accesos.lista[i - 1])
                    },
                    modoOscuro = modoOscuro,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF163E45) else Turquesa100),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (modoOscuro) Color(0xFF1D4950) else Superficie),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.BarChart, contentDescription = null, tint = if (modoOscuro) Color.White else Petroleo800)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tituloResumen, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800)
                            Text(
                                text = "${fmtFechaCorta(state.rangoDesde)} – ${fmtFechaCorta(state.rangoHasta)}",
                                fontSize = 10.sp,
                                color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario
                            )
                        }
                        Box {
                            IconButton(onClick = { menuAbierto = true }, enabled = puedeEditar) {
                                Icon(
                                    Icons.Outlined.MoreHoriz,
                                    contentDescription = "Opciones",
                                    tint = (if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario).copy(alpha = if (puedeEditar) 1f else 0.4f)
                                )
                            }
                            DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                                DropdownMenuItem(
                                    text = { Text("Registrar conteo") },
                                    onClick = {
                                        menuAbierto = false
                                        viewModel.abrirFormulario()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cargar datos de prueba") },
                                    onClick = {
                                        menuAbierto = false
                                        val cargados = viewModel.cargarDatosDePrueba()
                                        Toast.makeText(
                                            context,
                                            if (cargados) "Datos ficticios cargados" else "Ya existen registros",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF1D4950) else Superficie),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MedidorSemicircular(
                                porcentajeTransito = resumen.porcentajeTransito,
                                totalTexto = fmtNumero(resumen.ingresos),
                                modoOscuro = modoOscuro
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilaLeyenda(if (modoOscuro) Color(0xFF2AAEA4) else ColorSinTransito, "Flujo total", fmtNumero(resumen.ingresos), modoOscuro = modoOscuro)
                                FilaLeyenda(if (modoOscuro) AdvertenciaOscura else ColorTransito, "Tránsito", fmtPorc(resumen.porcentajeTransito), modoOscuro = modoOscuro)
                                FilaLeyenda(if (modoOscuro) ExitoOscuro else Exito, "Conversión", fmtPorc(resumen.porcentajeConversion), modoOscuro = modoOscuro)
                            }
                        }
                    }

                    if (resumen.ingresos == 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aún no hay conteos en este período.",
                            fontSize = 11.sp,
                            color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF1D4950) else FondoBeige),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Estadistica(
                        icono = Icons.Outlined.TrendingUp,
                        valor = state.crecimiento?.let { String.format(localeCl, "%+.0f%%", it) } ?: "—",
                        etiqueta = "Crecimiento",
                        modoOscuro = modoOscuro,
                        modifier = Modifier.weight(1f)
                    )
                    DivisorVertical(modoOscuro = modoOscuro)
                    Estadistica(
                        icono = Icons.Outlined.Schedule,
                        valor = state.duracionPromedioMin?.let { "$it min" } ?: "—",
                        etiqueta = "Promedio",
                        modoOscuro = modoOscuro,
                        modifier = Modifier.weight(1f)
                    )
                    DivisorVertical(modoOscuro = modoOscuro)
                    Estadistica(
                        icono = Icons.Outlined.CalendarToday,
                        valor = state.cantidadSesiones.toString(),
                        etiqueta = "Sesiones",
                        modoOscuro = modoOscuro,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val nombresModo = listOf("Flujo total", "Tránsito", "Compradores")
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ACTIVIDAD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (modoOscuro) AdvertenciaOscura else Ambar600,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Flujo por acceso",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (modoOscuro) Color.White else Petroleo800
                    )
                }
                SelectorCompacto(
                    valor = nombresModo[state.modoComparar],
                    opciones = nombresModo,
                    onSeleccion = { viewModel.seleccionarModo(it) },
                    modoOscuro = modoOscuro
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF163E45) else FondoGrafico),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GraficoBarras(
                        porAcceso = state.porAcceso,
                        modo = state.modoComparar,
                        accesoSeleccionado = state.acceso,
                        modoOscuro = modoOscuro
                    )
                    HorizontalDivider(
                        color = (if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario).copy(alpha = 0.25f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (state.modoComparar) {
                            0 -> {
                                LeyendaPunto(if (modoOscuro) AdvertenciaOscura else ColorTransito, "Tránsito estimado", modoOscuro = modoOscuro)
                                Spacer(modifier = Modifier.width(16.dp))
                                LeyendaPunto(if (modoOscuro) Color(0xFF2AAEA4) else ColorSinTransito, "Flujo sin tránsito", modoOscuro = modoOscuro)
                            }
                            1 -> LeyendaPunto(if (modoOscuro) AdvertenciaOscura else ColorTransito, "Tránsito estimado", modoOscuro = modoOscuro)
                            else -> LeyendaPunto(if (modoOscuro) Color(0xFF2AAEA4) else ColorSinTransito, "Compradores", modoOscuro = modoOscuro)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF163E45) else Superficie),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Perfil de quien transita",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (modoOscuro) Color.White else Petroleo800
                    )
                    Text(
                        text = "Rango etario · ${fmtNumero(resumen.transito)} personas sin compra",
                        fontSize = 11.sp,
                        color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (resumen.transito == 0) {
                        Text("Sin datos de tránsito en este período.", fontSize = 12.sp, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario)
                    } else {
                        FilaSegmento("Hasta 29 años", resumen.transitoJoven, resumen.transito, if (modoOscuro) AdvertenciaOscura else ColorTransito, modoOscuro = modoOscuro)
                        Spacer(modifier = Modifier.height(10.dp))
                        FilaSegmento("30 a 49 años", resumen.transitoAdulto, resumen.transito, if (modoOscuro) Color(0xFF2AAEA4) else ColorSinTransito, modoOscuro = modoOscuro)
                        Spacer(modifier = Modifier.height(10.dp))
                        FilaSegmento("50 años o más", resumen.transitoMayor, resumen.transito, if (modoOscuro) Color.White else Petroleo800, modoOscuro = modoOscuro)
                    }
                }
            }

            if (state.registros.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF163E45) else Superficie),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Últimos registros",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (modoOscuro) Color.White else Petroleo800
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        state.registros.forEachIndexed { i, registro ->
                            if (i > 0) {
                                HorizontalDivider(color = if (modoOscuro) Color(0xFF254A50) else FondoAplicacion)
                            }
                            FilaRegistro(
                                registro = registro,
                                puedeEditar = puedeEditar,
                                modoOscuro = modoOscuro,
                                onCambiarActivo = { activo ->
                                    viewModel.cambiarActivoRegistro(registro.id, activo)
                                    Toast.makeText(
                                        context,
                                        if (activo) "Registro habilitado" else "Registro deshabilitado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
private fun SelectorCampo(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    onSeleccion: (Int) -> Unit,
    modoOscuro: Boolean = false,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Card(
            colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF163E45) else Superficie),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { abierto = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(etiqueta, fontSize = 10.sp, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario)
                    Text(valor, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800)
                }
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = if (modoOscuro) Color.White else Petroleo800)
            }
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEachIndexed { i, opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        abierto = false
                        onSeleccion(i)
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectorCompacto(
    valor: String,
    opciones: List<String>,
    onSeleccion: (Int) -> Unit,
    modoOscuro: Boolean = false
) {
    var abierto by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (modoOscuro) Color(0xFF1D4950) else Superficie)
                .clickable { abierto = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(valor, fontSize = 11.sp, color = if (modoOscuro) Color.White else Petroleo800)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = if (modoOscuro) Color.White else Petroleo800,
                modifier = Modifier.size(14.dp)
            )
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEachIndexed { i, opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        abierto = false
                        onSeleccion(i)
                    }
                )
            }
        }
    }
}

@Composable
private fun MedidorSemicircular(porcentajeTransito: Float, totalTexto: String, modoOscuro: Boolean = false) {
    val barridoAnimado by animateFloatAsState(
        targetValue = (porcentajeTransito / 100f).coerceIn(0f, 1f) * 180f,
        animationSpec = tween(800),
        label = "medidor"
    )

    Box(
        modifier = Modifier
            .width(124.dp)
            .height(78.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grosor = 16.dp.toPx()
            val diametro = size.width - grosor
            val origen = Offset(grosor / 2, grosor / 2)
            val area = Size(diametro, diametro)

            drawArc(
                color = if (modoOscuro) Color(0xFF31575D) else Color(0xFFE5ECE9),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = origen,
                size = area,
                style = Stroke(width = grosor)
            )
            drawArc(
                color = if (modoOscuro) AdvertenciaOscura else ColorTransito,
                startAngle = 360f - barridoAnimado,
                sweepAngle = barridoAnimado,
                useCenter = false,
                topLeft = origen,
                size = area,
                style = Stroke(width = grosor)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(totalTexto, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800)
            Text("personas", fontSize = 9.sp, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario)
        }
    }
}

@Composable
private fun FilaLeyenda(color: Color, etiqueta: String, valor: String, modoOscuro: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(etiqueta, fontSize = 11.sp, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario, modifier = Modifier.weight(1f))
        Text(valor, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800)
    }
}

@Composable
private fun LeyendaPunto(color: Color, texto: String, modoOscuro: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(texto, fontSize = 10.sp, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario)
    }
}

@Composable
private fun Estadistica(icono: ImageVector, valor: String, etiqueta: String, modoOscuro: Boolean = false, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icono, contentDescription = null, tint = if (modoOscuro) AdvertenciaOscura else Ambar600, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800)
        Text(etiqueta, fontSize = 10.sp, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario)
    }
}

@Composable
private fun DivisorVertical(modoOscuro: Boolean = false) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background((if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario).copy(alpha = 0.25f))
    )
}

@Composable
private fun GraficoBarras(
    porAcceso: Map<String, ResumenFlujo>,
    modo: Int,
    accesoSeleccionado: String?,
    modoOscuro: Boolean = false
) {
    fun valorDe(r: ResumenFlujo): Int = when (modo) {
        1 -> r.transito
        2 -> r.compradores
        else -> r.ingresos
    }

    val maximo = Accesos.lista
        .maxOf { valorDe(porAcceso[it] ?: ResumenFlujo()) }
        .coerceAtLeast(1)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Accesos.lista.forEach { acceso ->
            BarraAcceso(
                acceso = acceso,
                datos = porAcceso[acceso] ?: ResumenFlujo(),
                modo = modo,
                maximo = maximo,
                resaltada = accesoSeleccionado == null || accesoSeleccionado == acceso,
                modoOscuro = modoOscuro,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BarraAcceso(
    acceso: String,
    datos: ResumenFlujo,
    modo: Int,
    maximo: Int,
    resaltada: Boolean,
    modoOscuro: Boolean = false,
    modifier: Modifier = Modifier
) {
    val valor = when (modo) {
        1 -> datos.transito
        2 -> datos.compradores
        else -> datos.ingresos
    }
    val fraccion by animateFloatAsState(
        targetValue = valor.toFloat() / maximo,
        animationSpec = tween(700),
        label = "alturaBarra"
    )
    val opacidad by animateFloatAsState(
        targetValue = if (resaltada) 1f else 0.35f,
        animationSpec = tween(300),
        label = "opacidadBarra"
    )

    Column(
        modifier = modifier.alpha(opacidad),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(fmtNumero(valor), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (modoOscuro) Color(0xFF1D4950) else Color(0xFFF7F9F6)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraccion.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(13.dp))
            ) {
                val colorTransitoUso = if (modoOscuro) AdvertenciaOscura else ColorTransito
                val colorSinTransitoUso = if (modoOscuro) Color(0xFF2AAEA4) else ColorSinTransito

                when (modo) {
                    1 -> Box(modifier = Modifier.fillMaxSize().background(colorTransitoUso))
                    2 -> Box(modifier = Modifier.fillMaxSize().background(colorSinTransitoUso))
                    else -> {
                        val sinTransito = (datos.ingresos - datos.transito).coerceAtLeast(0)
                        if (sinTransito > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(sinTransito.toFloat())
                                    .background(colorSinTransitoUso)
                            )
                        }
                        if (datos.transito > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(datos.transito.toFloat())
                                    .background(colorTransitoUso)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(acceso, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color(0xFF2AAEA4) else ColorSinTransito)
        Text(Accesos.nombre(acceso), fontSize = 8.sp, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario, maxLines = 1)
    }
}

@Composable
private fun FilaSegmento(etiqueta: String, cantidad: Int, total: Int, color: Color, modoOscuro: Boolean = false) {
    val porcentaje = if (total == 0) 0f else cantidad * 100f / total
    val fraccion by animateFloatAsState(
        targetValue = porcentaje / 100f,
        animationSpec = tween(700),
        label = "segmento"
    )

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(etiqueta, fontSize = 12.sp, color = if (modoOscuro) Color.White else Petroleo800, modifier = Modifier.weight(1f))
            Text(
                text = "${fmtNumero(cantidad)} · ${fmtPorc(porcentaje)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (modoOscuro) Color.White else Petroleo800
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (modoOscuro) Color(0xFF1D4950) else FondoAplicacion)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
private fun FilaRegistro(
    registro: RegistroFlujo,
    puedeEditar: Boolean,
    modoOscuro: Boolean = false,
    onCambiarActivo: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .alpha(if (registro.activo) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (modoOscuro) Color(0xFF0F5961) else Petroleo800),
            contentAlignment = Alignment.Center
        ) {
            Text(registro.acceso, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${fmtFechaCorta(registro.fecha)} · ${fmtNumero(registro.ingresos)} ingresos",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (modoOscuro) Color.White else Petroleo800
            )
            Text(
                text = "Tránsito ${fmtNumero(registro.transito)} · Compradores ${fmtNumero(registro.compradores)}",
                fontSize = 11.sp,
                color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario
            )
            if (!registro.activo) {
                Text("Deshabilitado", fontSize = 10.sp, color = Error)
            }
        }
        IconButton(
            onClick = { onCambiarActivo(!registro.activo) },
            enabled = puedeEditar,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (registro.activo) Icons.Outlined.Block else Icons.Outlined.Restore,
                contentDescription = if (registro.activo) "Deshabilitar registro" else "Habilitar registro",
                tint = (if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario).copy(alpha = if (puedeEditar) 1f else 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoRangoFechas(
    onCancelar: () -> Unit,
    onAplicar: (String, String) -> Unit
) {
    val estado = rememberDateRangePickerState()
    val inicio = estado.selectedStartDateMillis
    val fin = estado.selectedEndDateMillis

    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                enabled = inicio != null,
                onClick = {
                    if (inicio != null) {
                        onAplicar(millisAFecha(inicio), millisAFecha(fin ?: inicio))
                    }
                }
            ) { Text("Aplicar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    ) {
        DateRangePicker(state = estado, modifier = Modifier.height(500.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoRegistroFlujo(
    form: FormFlujoState,
    onCambio: (String, String) -> Unit,
    onCancelar: () -> Unit,
    onGuardar: () -> Unit
) {
    var mostrarFecha by remember { mutableStateOf(false) }

    if (mostrarFecha) {
        val estadoFecha = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { onCambio("fecha", millisAFecha(it)) }
                    mostrarFecha = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarFecha = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoFecha)
        }
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Registrar conteo", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectorCampo(
                    etiqueta = "Acceso",
                    valor = "${form.acceso} · ${Accesos.nombre(form.acceso)}",
                    opciones = Accesos.lista.map { "$it · ${Accesos.nombre(it)}" },
                    onSeleccion = { i -> onCambio("acceso", Accesos.lista[i]) },
                    modifier = Modifier.fillMaxWidth()
                )

                val errorFecha = form.errores["fecha"]
                Box {
                    OutlinedTextField(
                        value = fmtFechaCorta(form.fecha),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha") },
                        leadingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = null) },
                        trailingIcon = {
                            if (errorFecha != null) {
                                Icon(Icons.Outlined.ErrorOutline, contentDescription = "Error", tint = Error)
                            }
                        },
                        isError = errorFecha != null,
                        supportingText = if (errorFecha != null) {
                            { Text(errorFecha) }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { mostrarFecha = true }
                    )
                }

                CampoNumero(
                    valor = form.ingresos,
                    etiqueta = "Ingresos totales",
                    icono = Icons.Outlined.Group,
                    error = form.errores["ingresos"],
                    onCambio = { onCambio("ingresos", it) }
                )
                CampoNumero(
                    valor = form.compradores,
                    etiqueta = "Compradores",
                    icono = Icons.Outlined.ShoppingCart,
                    error = form.errores["compradores"],
                    onCambio = { onCambio("compradores", it) }
                )

                Text("Tránsito sin compra, por edad", fontSize = 12.sp)

                CampoNumero(
                    valor = form.transitoJoven,
                    etiqueta = "Hasta 29 años",
                    icono = Icons.Outlined.DirectionsWalk,
                    error = form.errores["transitoJoven"],
                    onCambio = { onCambio("transitoJoven", it) }
                )
                CampoNumero(
                    valor = form.transitoAdulto,
                    etiqueta = "30 a 49 años",
                    icono = Icons.Outlined.DirectionsWalk,
                    error = form.errores["transitoAdulto"],
                    onCambio = { onCambio("transitoAdulto", it) }
                )
                CampoNumero(
                    valor = form.transitoMayor,
                    etiqueta = "50 años o más",
                    icono = Icons.Outlined.DirectionsWalk,
                    error = form.errores["transitoMayor"],
                    onCambio = { onCambio("transitoMayor", it) }
                )

                form.errores["total"]?.let { mensaje ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(mensaje, fontSize = 12.sp, color = Error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onGuardar
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun CampoNumero(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (error != null) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = "Error", tint = Error)
            }
        },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
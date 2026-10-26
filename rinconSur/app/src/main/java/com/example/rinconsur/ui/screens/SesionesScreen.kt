package com.example.rinconsur.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.Fechas
import com.example.rinconsur.model.SesionAcceso
import com.example.rinconsur.ui.components.BarraNavegacionInferior
import com.example.rinconsur.ui.components.DestinoBarra
import com.example.rinconsur.ui.theme.*
import com.example.rinconsur.viewmodel.SesionesViewModel
import java.util.Calendar

@Composable
fun SesionesScreen(
    puedeEditar: Boolean,
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit,
    onNavegar: (DestinoBarra) -> Unit,
    viewModel: SesionesViewModel = viewModel()
) {
    LaunchedEffect(puedeEditar) {
        viewModel.definirPermisos(puedeEditar)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var mostrarNuevaSesion by remember { mutableStateOf(false) }
    var sesionParaConteo by remember { mutableStateOf<SesionAcceso?>(null) }
    var sesionParaDeshabilitar by remember { mutableStateOf<SesionAcceso?>(null) }

    if (mostrarNuevaSesion) {
        DialogoNuevaSesion(
            fecha = Fechas.fechaLarga(state.fechaSeleccionada),
            onCancelar = { mostrarNuevaSesion = false },
            onGuardar = { codigo, nombre ->
                val creada = viewModel.crearSesion(codigo, nombre)
                mostrarNuevaSesion = false
                Toast.makeText(
                    context,
                    if (creada) "Sesión creada" else "No se puede programar en una fecha pasada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    sesionParaConteo?.let { sesion ->
        DialogoNuevoConteo(
            titulo = "${sesion.codigo} · ${sesion.nombre}",
            onCancelar = { sesionParaConteo = null },
            onGuardar = { nombre, horario ->
                viewModel.agregarConteo(sesion.id, nombre, horario)
                sesionParaConteo = null
                Toast.makeText(context, "Conteo agregado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    sesionParaDeshabilitar?.let { sesion ->
        AlertDialog(
            onDismissRequest = { sesionParaDeshabilitar = null },
            title = { Text("Deshabilitar sesión", fontWeight = FontWeight.Bold, color = Petroleo800) },
            text = {
                Text(
                    "¿Deshabilitar ${sesion.codigo} · ${sesion.nombre}? " +
                            "Dejará de verse para los demás usuarios. Podrás habilitarla de nuevo cuando quieras."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cambiarActivoSesion(sesion.id, false)
                        sesionParaDeshabilitar = null
                        Toast.makeText(context, "Sesión deshabilitada", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("Deshabilitar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { sesionParaDeshabilitar = null }) {
                    Text("Cancelar", color = Petroleo800)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BarraNavegacionInferior(
                seleccionado = DestinoBarra.SESIONES,
                onNavegar = onNavegar
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

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.rotulo_rincon_sur),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextoSecundario,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Sesiones",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Petroleo800
                    )
                }
                IconButton(
                    onClick = onCambiarTema,
                    modifier = Modifier
                        .background(Superficie, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (modoOscuro) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = stringResource(id = R.string.desc_cambiar_tema),
                        tint = Petroleo800
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Turquesa100)
                    .padding(4.dp)
            ) {
                listOf("Hoy", "Semana", "Historial").forEachIndexed { i, titulo ->
                    val seleccionada = state.tab == i
                    val fondoTab by animateColorAsState(
                        targetValue = if (seleccionada) Superficie else Color.Transparent,
                        animationSpec = tween(250),
                        label = "fondoTab"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(fondoTab)
                            .clickable { viewModel.seleccionarTab(i) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = titulo,
                            fontSize = 13.sp,
                            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium,
                            color = if (seleccionada) Petroleo800 else TextoSecundario
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Turquesa100),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.cambiarMes(-1) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.ChevronLeft, contentDescription = "Mes anterior", tint = TextoSecundario)
                        }
                        Text(
                            text = "${Fechas.meses[state.mesVisible]} ${state.aniosVisible}",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Petroleo800
                        )
                        IconButton(onClick = { viewModel.cambiarMes(1) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.ChevronRight, contentDescription = "Mes siguiente", tint = TextoSecundario)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("L", "M", "M", "J", "V", "S", "D").forEach { letra ->
                            Text(
                                text = letra,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                color = TextoSecundario
                            )
                        }
                    }

                    val primerDia = Calendar.getInstance().apply { set(state.aniosVisible, state.mesVisible, 1) }
                    val desfase = (primerDia.get(Calendar.DAY_OF_WEEK) + 5) % 7
                    val diasMes = primerDia.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val celdas: List<Int?> = List<Int?>(desfase) { null } + (1..diasMes).toList()

                    celdas.chunked(7).forEach { semana ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (i in 0 until 7) {
                                val dia = semana.getOrNull(i)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dia != null) {
                                        val esPasado = Fechas.fecha(state.aniosVisible, state.mesVisible, dia) < Fechas.hoy()
                                        val seleccionado =
                                            Fechas.fecha(state.aniosVisible, state.mesVisible, dia) == state.fechaSeleccionada
                                        val fondoDia by animateColorAsState(
                                            targetValue = if (seleccionado) Petroleo800 else Color.Transparent,
                                            animationSpec = tween(250),
                                            label = "fondoDia"
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(fondoDia)
                                                .clickable { viewModel.seleccionarDia(dia) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dia.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                                color = if (seleccionado) Color.White
                                                else if (esPasado) TextoSecundario.copy(alpha = 0.45f)
                                                else TextoSecundario
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val fechaPasada = state.fechaSeleccionada < Fechas.hoy()
            val puedeProgramar = puedeEditar && !fechaPasada
            val colorBorde = TextoSecundario
            val textoBoton = when {
                !puedeEditar -> "Solo el administrador puede programar sesiones"
                fechaPasada -> "No se puede programar en fechas pasadas"
                else -> "Programar nueva sesión"
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .alpha(if (puedeProgramar) 1f else 0.45f)
                    .clip(RoundedCornerShape(16.dp))
                    .drawBehind {
                        drawRoundRect(
                            color = colorBorde,
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                            )
                        )
                    }
                    .clickable(enabled = puedeProgramar) { mostrarNuevaSesion = true },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = Petroleo800)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = textoBoton,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Petroleo800,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (state.sesiones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay sesiones en este período",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                }
            } else {
                state.sesiones.forEach { sesion ->
                    TarjetaSesion(
                        sesion = sesion,
                        puedeEditar = puedeEditar,
                        onToggleConteo = { id, marcado -> viewModel.cambiarEstadoConteo(id, marcado) },
                        onCambiarActivoConteo = { id, activo ->
                            viewModel.cambiarActivoConteo(id, activo)
                            Toast.makeText(
                                context,
                                if (activo) "Conteo habilitado" else "Conteo deshabilitado",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onAgregarConteo = { sesionParaConteo = sesion },
                        onDeshabilitarSesion = { sesionParaDeshabilitar = sesion },
                        onHabilitarSesion = {
                            viewModel.cambiarActivoSesion(sesion.id, true)
                            Toast.makeText(context, "Sesión habilitada", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TarjetaSesion(
    sesion: SesionAcceso,
    puedeEditar: Boolean,
    onToggleConteo: (Long, Boolean) -> Unit,
    onCambiarActivoConteo: (Long, Boolean) -> Unit,
    onAgregarConteo: () -> Unit,
    onDeshabilitarSesion: () -> Unit,
    onHabilitarSesion: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val vigentes = sesion.conteos.filter { it.activo }
    val completa = vigentes.isNotEmpty() && sesion.avance == 100

    val fondoTarjeta by animateColorAsState(
        targetValue = if (completa) Color(0xFFF0ECE3) else Superficie,
        animationSpec = tween(400),
        label = "fondoTarjeta"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = fondoTarjeta),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (sesion.activo) 1f else 0.55f)
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Petroleo800),
                    contentAlignment = Alignment.Center
                ) {
                    Text(sesion.codigo, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sesion.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Petroleo800
                    )
                    if (!sesion.activo) {
                        Text("Deshabilitada", fontSize = 11.sp, color = Error)
                    }
                }
                if (puedeEditar) {
                    Box {
                        IconButton(onClick = { menuAbierto = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.MoreHoriz, contentDescription = "Opciones", tint = TextoSecundario)
                        }
                        DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                            if (sesion.activo) {
                                DropdownMenuItem(
                                    text = { Text("Agregar conteo") },
                                    onClick = {
                                        menuAbierto = false
                                        onAgregarConteo()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Deshabilitar sesión", color = Error) },
                                    onClick = {
                                        menuAbierto = false
                                        onDeshabilitarSesion()
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Habilitar sesión") },
                                    onClick = {
                                        menuAbierto = false
                                        onHabilitarSesion()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sesion.conteos.isEmpty()) {
                Text("Sin conteos todavía", fontSize = 12.sp, color = TextoSecundario)
            }

            sesion.conteos.forEach { conteo ->
                val puedeMarcar = puedeEditar && sesion.activo && conteo.activo

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .alpha(if (conteo.activo) 1f else 0.5f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (puedeMarcar) Modifier.clickable { onToggleConteo(conteo.id, !conteo.completado) }
                                else Modifier
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CasillaConteo(marcada = conteo.completado)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(conteo.nombre, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Petroleo800)
                            Text(conteo.horario, fontSize = 11.sp, color = Ambar600)
                            if (!conteo.activo) {
                                Text("Deshabilitado", fontSize = 10.sp, color = Error)
                            }
                        }
                    }
                    if (puedeEditar && sesion.activo) {
                        IconButton(
                            onClick = { onCambiarActivoConteo(conteo.id, !conteo.activo) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (conteo.activo) Icons.Outlined.Block else Icons.Outlined.Restore,
                                contentDescription = if (conteo.activo) "Deshabilitar conteo" else "Habilitar conteo",
                                tint = TextoSecundario,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = FondoAplicacion, modifier = Modifier.padding(vertical = 12.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = Fechas.fechaLarga(sesion.fecha),
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp,
                    color = TextoSecundario
                )
                CirculoAvance(avance = sesion.avance)
            }
        }
    }
}

@Composable
private fun CasillaConteo(marcada: Boolean) {
    val colorBorde by animateColorAsState(
        targetValue = if (marcada) Petroleo800 else TextoSecundario,
        animationSpec = tween(200),
        label = "bordeCasilla"
    )
    val tamanoInterior by animateFloatAsState(
        targetValue = if (marcada) 8f else 0f,
        animationSpec = tween(200),
        label = "interiorCasilla"
    )

    Box(
        modifier = Modifier
            .size(18.dp)
            .border(1.5.dp, colorBorde, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(tamanoInterior.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Petroleo800)
        )
    }
}

@Composable
private fun CirculoAvance(avance: Int) {
    val avanceAnimado by animateFloatAsState(
        targetValue = avance / 100f,
        animationSpec = tween(700),
        label = "avance"
    )

    Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grosor = 5.dp.toPx()
            val lado = size.minDimension - grosor
            val inicio = Offset(grosor / 2, grosor / 2)
            val area = Size(lado, lado)

            drawArc(
                color = Color(0xFFE5E3DA),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = inicio,
                size = area,
                style = Stroke(width = grosor)
            )
            drawArc(
                color = Ambar600,
                startAngle = -90f,
                sweepAngle = 360f * avanceAnimado,
                useCenter = false,
                topLeft = inicio,
                size = area,
                style = Stroke(width = grosor, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$avance%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Petroleo800)
            Text("avance", fontSize = 7.sp, color = TextoSecundario)
        }
    }
}

@Composable
private fun DialogoNuevaSesion(
    fecha: String,
    onCancelar: () -> Unit,
    onGuardar: (String, String) -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nueva sesión", fontWeight = FontWeight.Bold, color = Petroleo800) },
        text = {
            Column {
                Text("Fecha: $fecha", fontSize = 13.sp, color = TextoSecundario)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { if (it.length <= 3) codigo = it },
                    label = { Text("Código (ej: A1)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del acceso") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(codigo, nombre) },
                enabled = codigo.isNotBlank() && nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Petroleo800)
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", color = Petroleo800) }
        }
    )
}

@Composable
private fun DialogoNuevoConteo(
    titulo: String,
    onCancelar: () -> Unit,
    onGuardar: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var horario by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Agregar conteo", fontWeight = FontWeight.Bold, color = Petroleo800) },
        text = {
            Column {
                Text(titulo, fontSize = 13.sp, color = TextoSecundario)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre (ej: Conteo mañana)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = horario,
                    onValueChange = { horario = it },
                    label = { Text("Horario (ej: 11:00–11:45)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, horario) },
                enabled = nombre.isNotBlank() && horario.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Petroleo800)
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", color = Petroleo800) }
        }
    )
}
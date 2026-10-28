package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.data.Fechas
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.ui.components.BarraNavegacionInferior
import com.example.rinconsur.ui.components.DestinoBarra
import com.example.rinconsur.ui.components.TarjetaAccesosActivos
import com.example.rinconsur.ui.components.TarjetaSesionMenu
import com.example.rinconsur.ui.components.TarjetaSinSesionActiva
import com.example.rinconsur.ui.components.TarjetaVisitantesTienda
import com.example.rinconsur.viewmodel.MenuPrincipalViewmodel
import kotlinx.coroutines.delay
@Composable
fun MenuPrincipalScreen(
    correoUsuario: String = "",
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit,
    onNavegarSesiones: () -> Unit = {},
    onNavegarAnalitica: () -> Unit = {},
    onNavegarEquipo: () -> Unit = {},
    onNavegarPerfil: () -> Unit = {},
    onEscanearBoleta: () -> Unit = {},
    viewModel: MenuPrincipalViewmodel = viewModel()
) {
    LaunchedEffect(correoUsuario) {
        viewModel.cargarDatosUsuarios(correoUsuario)
        viewModel.cargarVisitantes()
        while (true) {
            delay(2000)
            viewModel.refrescar()
        }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var mostrarDialogoCierre by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val colorTarjetaFondo = if (modoOscuro) Color(0xFF163E45) else Color(0xFFEBF3F0)
    val verdeResaltado = if (modoOscuro) Color(0xFF2AAEA4) else Color(0xFF0F5961)

    LaunchedEffect(state.mensajeInformativo) {
        state.mensajeInformativo?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarMensaje()
        }
    }

    if (mostrarDialogoCierre) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCierre = false },
            title = { Text("Confirmar Cierre Diario", color = if (modoOscuro) Color.White else Color.Unspecified) },
            text = { Text("¿Deseas ejecutar el Cierre Diario? Todos los visitantes actualmente 'En_Tienda' cambiarán a 'No_Compro', dejando el contador en 0.", color = if (modoOscuro) Color(0xFFB9CACB) else Color.Unspecified) },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoCierre = false
                        viewModel.ejecutarCierreDiario()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = verdeResaltado)
                ) {
                    Text("Ejecutar Cierre", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCierre = false }) {
                    Text("Cancelar", color = verdeResaltado)
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BarraNavegacionInferior(
                seleccionado = DestinoBarra.INICIO,
                onNavegar = { destino ->
                    when (destino) {
                        DestinoBarra.INICIO -> {}
                        DestinoBarra.SESIONES -> onNavegarSesiones()
                        DestinoBarra.ANALITICA -> onNavegarAnalitica()
                        DestinoBarra.EQUIPO -> onNavegarEquipo()
                        DestinoBarra.PERFIL -> onNavegarPerfil()
                    }
                },
                modoOscuro = modoOscuro
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${Fechas.fechaLarga(Fechas.hoy())}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (modoOscuro) Color(0xFFB9CACB) else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = when (state.rolUsuario) {
                                RolUsuario.ADMIN -> if (modoOscuro) Color(0xFF1F4E8C) else Color(0xFFD6E4F5)
                                RolUsuario.SUPERVISOR -> if (modoOscuro) Color(0xFF4B3525) else Color(0xFFF3E0D1)
                                RolUsuario.OBSERVADOR -> if (modoOscuro) Color(0xFF173E35) else Color(0xFFD4ECE2)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (state.rolUsuario) {
                                    RolUsuario.ADMIN -> "Administrador"
                                    RolUsuario.SUPERVISOR -> "Supervisor"
                                    RolUsuario.OBSERVADOR -> "Observador"
                                },
                                color = when (state.rolUsuario) {
                                    RolUsuario.ADMIN -> if (modoOscuro) Color.White else Color(0xFF1F4E8C)
                                    RolUsuario.SUPERVISOR -> if (modoOscuro) Color(0xFFF0A66D) else Color(0xFFB85D19)
                                    RolUsuario.OBSERVADOR -> if (modoOscuro) Color(0xFF69C392) else Color(0xFF1E7550)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Buenos días, ${state.nombreUsuario}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (modoOscuro) Color.White else verdeResaltado
                    )
                    if (state.correoUsuario.isNotBlank()) {
                        Text(
                            text = state.correoUsuario,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (modoOscuro) Color(0xFFB9CACB) else Color.Gray
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onCambiarTema,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (modoOscuro) Color(0xFF163E45) else Color.White)
                    ) {
                        Icon(
                            imageVector = if (modoOscuro) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            stringResource(R.string.desc_cambiar_tema),
                            tint = if (modoOscuro) Color.White else verdeResaltado
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))


            if (state.esSesionIniciada && state.entradaActiva != null) {
                TarjetaVisitantesTienda(
                    resumen = state.resumenVisitantes,
                    entradaActiva = state.entradaActiva!!,
                    colorFondoContenedor = colorTarjetaFondo,
                    verdeResaltado = verdeResaltado,
                    onRegistrarEntrada = { rango -> viewModel.registrarEntradaVisitante(rango) },
                    onRegistrarSalida = { viewModel.registrarSalidaSinCompra() },
                    onEjecutarCierre = { mostrarDialogoCierre = true },
                    onEscanearBoleta = onEscanearBoleta,
                    onFinalizarSesion = { viewModel.finalizarSesionEntrada() },
                    modoOscuro = modoOscuro
                )
            } else {
                TarjetaSinSesionActiva(
                    colorFondoContenedor = colorTarjetaFondo,
                    verdeResaltado = verdeResaltado,
                    onIrASesiones = onNavegarSesiones,
                    modoOscuro = modoOscuro
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            TabRow(
                selectedTabIndex = state.tabSeleccionada,
                containerColor = Color.Transparent,
                contentColor = verdeResaltado,
                divider = {}
            ) {
                Tab(
                    selected = state.tabSeleccionada == 0,
                    onClick = { viewModel.seleccionarTab(0) },
                    text = { Text("Hoy", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = state.tabSeleccionada == 1,
                    onClick = { viewModel.seleccionarTab(1) },
                    text = { Text("Próximas", fontWeight = FontWeight.Bold, color = if (modoOscuro) Color(0xFFB9CACB) else Color.Gray) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorTarjetaFondo),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = verdeResaltado)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mis sesiones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = verdeResaltado
                            )
                        }
                        TextButton(onClick = onNavegarSesiones) {
                            Text("Ver todas", color = verdeResaltado, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val sesionesFiltradas = state.sesiones.filter {
                        state.busqueda.isBlank() || it.titulo.contains(state.busqueda, ignoreCase = true)
                    }

                    sesionesFiltradas.forEach { sesion ->
                        TarjetaSesionMenu(
                            sesion = sesion,
                            onCheckedChange = { viewModel.alternarCompletado(sesion.id) },
                            onClickDetalle = { },
                            modoOscuro = modoOscuro
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            TarjetaAccesosActivos(
                totalFlujo = state.flujoGeneralTotal,
                porcentajeMeta = state.porcentajeMeta,
                textoMeta = state.textoMeta,
                zonasFlujo = state.zonasFlujo,
                colorFondoContenedor = colorTarjetaFondo,
                verdeResaltado = verdeResaltado,
                modoOscuro = modoOscuro
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
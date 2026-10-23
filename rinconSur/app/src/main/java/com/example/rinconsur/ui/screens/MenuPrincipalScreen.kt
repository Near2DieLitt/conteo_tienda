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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.ui.components.TarjetaAccesosActivos
import com.example.rinconsur.ui.components.TarjetaSesionMenu
import com.example.rinconsur.viewmodel.MenuPrincipalViewmodel

@Composable
fun MenuPrincipalScreen(
    correoUsuario: String = "observador@rinconsur.cl",
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit,
    onNavegarSesiones: () -> Unit = {},
    onNavegarAnalitica: () -> Unit = {},
    onNavegarPerfil: () -> Unit = {},
    viewModel: MenuPrincipalViewmodel = viewModel()
) {
    LaunchedEffect(correoUsuario) {
        viewModel.cargarDatosUsuarios(correoUsuario)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val colorTarjetaFondo = Color(0xFFEBF3F0)
    val verdeResaltado = Color(0xFF0F5961)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = verdeResaltado,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF103336),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFE2A253),
                        selectedTextColor = Color(0xFFE2A253),
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavegarSesiones,
                    icon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Sesiones") },
                    label = { Text("Sesiones", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavegarAnalitica,
                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Analítica") },
                    label = { Text("Analítica", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavegarPerfil,
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    )
                )
            }
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
                            text = "MARTES, 15 DE SEPTIEMBRE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (state.rolUsuario == RolUsuario.SUPERVISOR) Color(0xFFF3E0D1) else Color(0xFFD4ECE2),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (state.rolUsuario == RolUsuario.SUPERVISOR) "Supervisor" else "Observador",
                                color = if (state.rolUsuario == RolUsuario.SUPERVISOR) Color(0xFFB85D19) else Color(0xFF1E7550),
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
                            color = Color.Gray
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (modoOscuro) Color(0xFF163E45) else Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notificaciones",
                            tint = if (modoOscuro) Color.White else verdeResaltado
                        )
                    }
                    IconButton(
                        onClick = onCambiarTema,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (modoOscuro) Color(0xFF163E45) else Color.White)
                    ) {
                        Icon(
                            imageVector = if (modoOscuro) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "Tema",
                            tint = if (modoOscuro) Color.White else verdeResaltado
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = state.busqueda,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar acceso o sesión", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

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
                    text = { Text("Próximas", fontWeight = FontWeight.Bold, color = Color.Gray) }
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
                            onClickDetalle = { }
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
                verdeResaltado = verdeResaltado
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
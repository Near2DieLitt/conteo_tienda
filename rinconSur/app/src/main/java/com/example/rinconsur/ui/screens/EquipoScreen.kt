package com.example.rinconsur.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.PerfilAcceso
import com.example.rinconsur.model.Perfiles
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.ui.components.BarraNavegacionInferior
import com.example.rinconsur.ui.components.DestinoBarra
import com.example.rinconsur.ui.theme.*
import com.example.rinconsur.viewmodel.EquipoViewModel

@Composable
fun EquipoScreen(
    correoUsuario: String,
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit,
    onNavegar: (DestinoBarra) -> Unit,
    viewModel: EquipoViewModel = viewModel()
) {
    LaunchedEffect(correoUsuario) { viewModel.cargar(correoUsuario) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var paraDeshabilitar by remember { mutableStateOf<Colaborador?>(null) }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    if (mostrarFormulario && state.esAdmin) {
        DialogoNuevaPersona(
            errores = state.errores,
            onValidar = viewModel::validarCampo,
            onCancelar = {
                viewModel.limpiarErrores()
                mostrarFormulario = false
            },
            onGuardar = { nombre, correo, rut, perfil ->
                if (viewModel.crearColaborador(nombre, correo, rut, perfil)) mostrarFormulario = false
            }
        )
    }

    state.credencial?.let { cred ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Acceso creado", fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800) },
            text = {
                Column {
                    Text("Entrega esta clave temporal a la persona. Solo se muestra una vez.", fontSize = 13.sp, color = if (modoOscuro) Color(0xFFB9CACB) else Color.Unspecified)
                    Spacer(Modifier.height(12.dp))
                    Text("Correo: ${cred.correo}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Color.Unspecified)
                    Text("Clave: ${cred.clave}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800)
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.cerrarCredencial() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800)
                ) { Text("Listo", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("clave", cred.clave))
                    Toast.makeText(context, "Clave copiada", Toast.LENGTH_SHORT).show()
                }) { Text("Copiar clave", color = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800) }
            }
        )
    }

    paraDeshabilitar?.let { c ->
        AlertDialog(
            onDismissRequest = { paraDeshabilitar = null },
            title = { Text("Deshabilitar acceso", fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800) },
            text = {
                Text(
                    "¿Deshabilitar a ${c.nombre}? No podrá iniciar sesión, pero sus datos se conservan y puedes habilitarla de nuevo cuando quieras.",
                    color = if (modoOscuro) Color(0xFFB9CACB) else Color.Unspecified
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cambiarActivo(c, false)
                        paraDeshabilitar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("Deshabilitar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { paraDeshabilitar = null }) { Text("Cancelar", color = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800) }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            AnimatedVisibility(visible = state.esAdmin, enter = fadeIn() + slideInVertically { it }, exit = fadeOut()) {
                FloatingActionButton(
                    onClick = { mostrarFormulario = true },
                    containerColor = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800,
                    contentColor = Color.White,
                    shape = CircleShape
                ) { Icon(Icons.Filled.Add, contentDescription = "Agregar persona") }
            }
        },
        bottomBar = {
            BarraNavegacionInferior(seleccionado = DestinoBarra.EQUIPO, onNavegar = onNavegar, modoOscuro = modoOscuro)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.rotulo_rincon_sur),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Equipo",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (modoOscuro) Color.White else Petroleo800
                    )
                }
                IconButton(
                    onClick = onCambiarTema,
                    modifier = Modifier.background(if (modoOscuro) Color(0xFF163E45) else Superficie, CircleShape).size(48.dp)
                ) {
                    Icon(
                        imageVector = if (modoOscuro) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = stringResource(id = R.string.desc_cambiar_tema),
                        tint = if (modoOscuro) Color.White else Petroleo800
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible = !state.esAdmin) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF4B3525) else Ambar100),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = if (modoOscuro) AdvertenciaOscura else Ambar600)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Solo lectura: únicamente un administrador puede agregar o modificar personas.",
                            fontSize = 12.sp,
                            color = if (modoOscuro) Color.White else TextoPrincipal
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.busqueda,
                onValueChange = viewModel::actualizarBusqueda,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o correo", color = if (modoOscuro) Color(0xFFB9CACB) else Color.Gray) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = if (modoOscuro) Color(0xFFB9CACB) else Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (modoOscuro) Color(0xFF163E45) else Color.White,
                    unfocusedContainerColor = if (modoOscuro) Color(0xFF163E45) else Color.White,
                    focusedTextColor = if (modoOscuro) Color.White else TextoPrincipal,
                    unfocusedTextColor = if (modoOscuro) Color.White else TextoPrincipal,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filtroPerfil == null,
                    onClick = { viewModel.filtrarPerfil(null) },
                    label = { Text("Todos (${state.colaboradores.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800,
                        selectedLabelColor = Color.White,
                        containerColor = if (modoOscuro) Color(0xFF163E45) else Color.Transparent,
                        labelColor = if (modoOscuro) Color(0xFFB9CACB) else Color.Unspecified
                    )
                )
                Perfiles.todos.forEach { p ->
                    FilterChip(
                        selected = state.filtroPerfil == p.codigo,
                        onClick = { viewModel.filtrarPerfil(if (state.filtroPerfil == p.codigo) null else p.codigo) },
                        label = { Text(p.codigo, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800,
                            selectedLabelColor = Color.White,
                            containerColor = if (modoOscuro) Color(0xFF163E45) else Color.Transparent,
                            labelColor = if (modoOscuro) Color(0xFFB9CACB) else Color.Unspecified
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "${state.activos} activos · ${state.colaboradores.size - state.activos} deshabilitados",
                fontSize = 12.sp,
                color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario
            )
            Spacer(Modifier.height(8.dp))

            if (state.visibles.isEmpty()) {
                Text(
                    "No hay personas que coincidan.",
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center,
                    color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario
                )
            }

            state.visibles.forEach { c ->
                TarjetaColaborador(
                    colaborador = c,
                    esYo = c.id == state.miId,
                    puedeEditar = state.esAdmin,
                    modoOscuro = modoOscuro,
                    onCambiarPerfil = { viewModel.cambiarPerfil(c, it) },
                    onDeshabilitar = { paraDeshabilitar = c },
                    onHabilitar = { viewModel.cambiarActivo(c, true) }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun TarjetaColaborador(
    colaborador: Colaborador,
    esYo: Boolean,
    puedeEditar: Boolean,
    modoOscuro: Boolean = false,
    onCambiarPerfil: (PerfilAcceso) -> Unit,
    onDeshabilitar: () -> Unit,
    onHabilitar: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val iniciales = colaborador.nombre.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
    val colorRol = when (colaborador.rol) {
        RolUsuario.ADMIN -> if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800
        RolUsuario.SUPERVISOR -> if (modoOscuro) AdvertenciaOscura else Ambar600
        RolUsuario.OBSERVADOR -> if (modoOscuro) Turquesa700Oscuro else Turquesa700
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF163E45) else Superficie),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().animateContentSize().alpha(if (colaborador.activo) 1f else 0.55f)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).background(if (modoOscuro) Color(0xFF0F5961) else Turquesa100, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text(iniciales, fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    colaborador.nombre + if (esYo) " (tú)" else "",
                    fontWeight = FontWeight.Bold,
                    color = if (modoOscuro) Color.White else TextoPrincipal,
                    fontSize = 14.sp
                )
                Text(colaborador.correo, fontSize = 12.sp, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${colaborador.perfil.codigo} · ${colaborador.perfil.nombre}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorRol
                    )
                    if (!colaborador.activo) {
                        Spacer(Modifier.width(8.dp))
                        Text("Deshabilitado", fontSize = 11.sp, color = Error)
                    }
                }
            }

            Box {
                IconButton(onClick = { menuAbierto = true }, enabled = puedeEditar) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Opciones",
                        tint = (if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario).copy(alpha = if (puedeEditar) 1f else 0.35f)
                    )
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    Perfiles.todos.forEach { p ->
                        DropdownMenuItem(
                            text = { Text("Perfil: ${p.nombre}") },
                            enabled = colaborador.activo && p.codigo != colaborador.perfil.codigo,
                            onClick = {
                                menuAbierto = false
                                onCambiarPerfil(p)
                            }
                        )
                    }
                    if (colaborador.activo) {
                        DropdownMenuItem(
                            text = { Text("Deshabilitar", color = Error) },
                            onClick = {
                                menuAbierto = false
                                onDeshabilitar()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Habilitar") },
                            onClick = {
                                menuAbierto = false
                                onHabilitar()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoNuevaPersona(
    errores: com.example.rinconsur.viewmodel.ErroresColaborador,
    onValidar: (String, String) -> Unit,
    onCancelar: () -> Unit,
    onGuardar: (String, String, String, PerfilAcceso) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var perfil by remember { mutableStateOf(Perfiles.OBSERVADOR) }
    var menuPerfil by remember { mutableStateOf(false) }

    @Composable
    fun campo(
        valor: String, etiqueta: String, error: String?, icono: androidx.compose.ui.graphics.vector.ImageVector,
        teclado: KeyboardType = KeyboardType.Text, placeholder: String? = null, onCambio: (String) -> Unit
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            label = { Text(etiqueta) },
            placeholder = if (placeholder != null) ({ Text(placeholder) }) else null,
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = {
                if (error != null) Icon(Icons.Outlined.ErrorOutline, contentDescription = "Error", tint = Error)
            },
            isError = error != null,
            supportingText = { error?.let { Text(it, color = Error) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nueva persona", fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                campo(nombre, "Nombre completo", errores.nombre, Icons.Outlined.Person) {
                    nombre = it.take(60); onValidar("nombre", nombre)
                }
                campo(rut, "RUT", errores.rut, Icons.Outlined.Badge, placeholder = "12345678-9") {
                    rut = it.take(12); onValidar("rut", rut)
                }
                campo(correo, "Correo", errores.correo, Icons.Outlined.Email, KeyboardType.Email, "nombre@rinconsur.cl") {
                    correo = it.take(100); onValidar("correo", correo)
                }
                Spacer(Modifier.height(8.dp))
                Box {
                    OutlinedButton(onClick = { menuPerfil = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.VerifiedUser, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("${perfil.codigo} · ${perfil.nombre}")
                    }
                    DropdownMenu(expanded = menuPerfil, onDismissRequest = { menuPerfil = false }) {
                        Perfiles.todos.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.codigo} · ${p.nombre}") },
                                onClick = { perfil = p; menuPerfil = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Se genera una clave temporal que verás una sola vez.",
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(nombre, correo, rut, perfil) }
            ) { Text("Crear acceso") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

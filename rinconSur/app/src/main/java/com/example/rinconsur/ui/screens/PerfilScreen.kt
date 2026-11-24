package com.example.rinconsur.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.ui.components.EtiquetaPerfil
import com.example.rinconsur.ui.components.ItemListaPerfil
import com.example.rinconsur.ui.components.RinconSurCampoTexto
import com.example.rinconsur.ui.theme.*
import com.example.rinconsur.viewmodel.PerfilViewModel
import kotlinx.coroutines.launch

@Composable
fun PerfilScreen(
    correoUsuario: String,
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit,
    onCerrarSesion: () -> Unit,
    onIrAInicio: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    LaunchedEffect(correoUsuario) {
        viewModel.cargarDatosUsuario(correoUsuario)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var mostrarOpcionesFoto by remember { mutableStateOf(false) }
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var mostrarDialogoCambiarClave by remember { mutableStateOf(false) }
    var mostrarConfirmarCerrarSesion by remember { mutableStateOf(false) }
    var textoInputDescripcion by remember { mutableStateOf("") }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val fotoCorregida = com.example.rinconsur.data.ConvertidorImagen.corregirRotacionCamara(bitmap)
            viewModel.actualizarFoto(fotoCorregida, correoUsuario)
            scope.launch {
                snackbarHostState.showSnackbar("Foto de perfil actualizada")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) takePictureLauncher.launch(null)
        else {
            scope.launch {
                snackbarHostState.showSnackbar("Permiso de cámara denegado")
            }
        }
    }

    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = com.example.rinconsur.data.ConvertidorImagen.desdeUri(context, uri)
            if (bitmap != null) {
                viewModel.actualizarFoto(bitmap, correoUsuario)
                scope.launch {
                    snackbarHostState.showSnackbar("Foto de perfil actualizada")
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("No se pudo cargar la imagen")
                }
            }
        }
    }

    if (mostrarOpcionesFoto) {
        AlertDialog(
            onDismissRequest = { mostrarOpcionesFoto = false },
            title = { Text("Cambiar foto de perfil", color = if (modoOscuro) Color.White else Color.Unspecified) },
            text = { Text("Elige desde dónde quieres seleccionar tu nueva foto", color = if (modoOscuro) Color(0xFFB9CACB) else Color.Unspecified) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarOpcionesFoto = false
                    val permissionCheckerResult = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    )
                    if (permissionCheckerResult == PackageManager.PERMISSION_GRANTED) {
                        takePictureLauncher.launch(null)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text("Cámara", color = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarOpcionesFoto = false
                    pickGalleryLauncher.launch("image/*")
                }) {
                    Text("Galería", color = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800)
                }
            }
        )
    }

    if (mostrarDialogoEditar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEditar = false },
            title = { Text("Editar perfil", fontWeight = FontWeight.Bold, color = if (modoOscuro) Color.White else Petroleo800) },
            text = {
                Column {
                    Text("Agrega una breve descripción o estado para tu perfil", color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = textoInputDescripcion,
                        onValueChange = { textoInputDescripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = false,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.actualizarDescripcion(correoUsuario, textoInputDescripcion)
                        mostrarDialogoEditar = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEditar = false }) {
                    Text("Cancelar", color = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800)
                }
            }
        )
    }

    if (mostrarDialogoCambiarClave) {
        DialogoCambiarClave(
            errorMensaje = state.errorCambioClave,
            onLimpiarError = viewModel::limpiarErrorClave,
            onCancelar = {
                viewModel.limpiarErrorClave()
                mostrarDialogoCambiarClave = false
            },
            onGuardar = { actual, nueva, confirmar ->
                viewModel.cambiarContrasena(correoUsuario, actual, nueva, confirmar, esPrimerInicio = false) {
                    mostrarDialogoCambiarClave = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Contraseña actualizada exitosamente")
                    }
                }
            }
        )
    }

    if (mostrarConfirmarCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarCerrarSesion = false },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro(a) de que deseas cerrar tu sesión actual en Rincón Sur?") },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmarCerrarSesion = false
                        onCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("Cerrar sesión", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onIrAInicio,
                    modifier = Modifier
                        .background(if (modoOscuro) Color(0xFF163E45) else Superficie, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.cd_volver_inicio),
                        tint = if (modoOscuro) Color.White else Petroleo800
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.rotulo_rincon_sur),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = stringResource(id = R.string.titulo_perfil),
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

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF163E45) else Turquesa100),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(if (modoOscuro) Color(0xFF0F5961) else Petroleo800),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.fotoBitmap != null) {
                                Image(
                                    bitmap = state.fotoBitmap!!.asImageBitmap(),
                                    contentDescription = stringResource(id = R.string.desc_avatar),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val iniciales = if (state.nombre.isNotBlank()) state.nombre.take(2).uppercase() else "US"
                                Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (modoOscuro) Color(0xFF1D4950) else Superficie)
                                .clickable { mostrarOpcionesFoto = true }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.CameraAlt,
                                contentDescription = stringResource(id = R.string.desc_cambiar_foto),
                                tint = if (modoOscuro) AdvertenciaOscura else Ambar600,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(state.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (modoOscuro) Color.White else Petroleo800)
                        Text(state.cargo, fontSize = 12.sp, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario)

                        if (state.descripcion.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${state.descripcion}\"",
                                fontSize = 13.sp,
                                color = if (modoOscuro) Color.White else Petroleo800,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    textoInputDescripcion = state.descripcion
                                    mostrarDialogoEditar = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (modoOscuro) Color(0xFF2AAEA4) else Petroleo800),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(stringResource(id = R.string.btn_editar_perfil), fontSize = 11.sp, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.limpiarErrorClave()
                                    mostrarDialogoCambiarClave = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text("Cambiar clave", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EtiquetaPerfil(stringResource(id = R.string.etiqueta_supervision), modoOscuro = modoOscuro)
                EtiquetaPerfil(stringResource(id = R.string.etiqueta_turno), modoOscuro = modoOscuro)
                EtiquetaPerfil(stringResource(id = R.string.etiqueta_accesos), modoOscuro = modoOscuro)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(id = R.string.campo_operacion).uppercase(), style = MaterialTheme.typography.labelSmall, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF163E45) else Superficie), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    ItemListaPerfil(Icons.Outlined.Group, stringResource(id = R.string.subparte_equipo_compartido), state.miembrosEquipo, modoOscuro = modoOscuro)
                    HorizontalDivider(color = if (modoOscuro) Color(0xFF254A50) else FondoAplicacion, modifier = Modifier.padding(horizontal = 16.dp))
                    ItemListaPerfil(Icons.Outlined.CalendarToday, stringResource(id = R.string.subparte_sesiones_compl), state.sesionesCompletadas, modoOscuro = modoOscuro)
                    HorizontalDivider(color = if (modoOscuro) Color(0xFF254A50) else FondoAplicacion, modifier = Modifier.padding(horizontal = 16.dp))
                    ItemListaPerfil(Icons.Outlined.VerifiedUser, stringResource(id = R.string.subparte_permisos), state.permisosCodigo, modoOscuro = modoOscuro)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(id = R.string.campo_preferencias).uppercase(), style = MaterialTheme.typography.labelSmall, color = if (modoOscuro) Color(0xFFB9CACB) else TextoSecundario, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF163E45) else Superficie), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    ItemListaPerfil(Icons.Outlined.Wifi, stringResource(id = R.string.subparte_datos_conexion), state.datosPendientes, modoOscuro = modoOscuro)
                    HorizontalDivider(color = if (modoOscuro) Color(0xFF254A50) else FondoAplicacion, modifier = Modifier.padding(horizontal = 16.dp))
                    ItemListaPerfil(Icons.Outlined.Notifications, stringResource(id = R.string.subparte_notificaciones), state.estadoNotificaciones, modoOscuro = modoOscuro)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { mostrarConfirmarCerrarSesion = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (modoOscuro) ErrorSuaveOscuro else ErrorSuave),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = null, tint = if (modoOscuro) ErrorOscuro else Error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.btn_cerrar_sesion), color = if (modoOscuro) ErrorOscuro else Error, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@Composable
private fun DialogoCambiarClave(
    errorMensaje: String?,
    onLimpiarError: () -> Unit,
    onCancelar: () -> Unit,
    onGuardar: (String, String, String) -> Unit
) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var verActual by remember { mutableStateOf(false) }
    var verNueva by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Cambiar contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RinconSurCampoTexto(
                    value = actual,
                    onValueChange = { actual = it; onLimpiarError() },
                    label = "Contraseña actual",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (verActual) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verActual = !verActual }) {
                            Icon(if (verActual) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = null)
                        }
                    }
                )
                RinconSurCampoTexto(
                    value = nueva,
                    onValueChange = { nueva = it; onLimpiarError() },
                    label = "Nueva contraseña",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (verNueva) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verNueva = !verNueva }) {
                            Icon(if (verNueva) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = null)
                        }
                    }
                )
                ChecklistRequisitosClave(clave = nueva)
                RinconSurCampoTexto(
                    value = confirmar,
                    onValueChange = { confirmar = it; onLimpiarError() },
                    label = "Confirmar nueva contraseña",
                    errorMensaje = errorMensaje,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verConfirmar = !verConfirmar }) {
                            Icon(if (verConfirmar) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = null)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onGuardar(actual, nueva, confirmar) }) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
fun CambiarClaveObligatoriaScreen(
    correoUsuario: String,
    onClaveActualizada: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    BackHandler(enabled = true) { }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var verActual by remember { mutableStateOf(false) }
    var verNueva by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Cambio de clave obligatorio",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Iniciaste sesión con la clave temporal enviada a tu correo ($correoUsuario). Por seguridad, crea una clave personal antes de continuar.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )

            RinconSurCampoTexto(
                value = actual,
                onValueChange = { actual = it; viewModel.limpiarErrorClave() },
                label = "Clave temporal actual",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (verActual) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { verActual = !verActual }) {
                        Icon(if (verActual) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = null)
                    }
                }
            )
            Spacer(Modifier.height(10.dp))
            RinconSurCampoTexto(
                value = nueva,
                onValueChange = { nueva = it; viewModel.limpiarErrorClave() },
                label = "Nueva contraseña personal",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (verNueva) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { verNueva = !verNueva }) {
                        Icon(if (verNueva) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = null)
                    }
                }
            )
            Spacer(Modifier.height(8.dp))
            ChecklistRequisitosClave(clave = nueva)
            Spacer(Modifier.height(10.dp))
            RinconSurCampoTexto(
                value = confirmar,
                onValueChange = { confirmar = it; viewModel.limpiarErrorClave() },
                label = "Confirmar nueva contraseña",
                errorMensaje = state.errorCambioClave,
                keyboardType = KeyboardType.Password,
                visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { verConfirmar = !verConfirmar }) {
                        Icon(if (verConfirmar) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, contentDescription = null)
                    }
                }
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.cambiarContrasena(correoUsuario, actual, nueva, confirmar, esPrimerInicio = false) {
                        onClaveActualizada()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Guardar y continuar")
            }
        }
    }
}

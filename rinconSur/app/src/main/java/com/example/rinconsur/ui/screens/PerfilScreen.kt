package com.example.rinconsur.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.ui.components.EtiquetaPerfil
import com.example.rinconsur.ui.components.ItemListaPerfil
import com.example.rinconsur.ui.theme.*
import com.example.rinconsur.viewmodel.PerfilViewModel

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

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.actualizarFoto(bitmap, correoUsuario)
            Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) takePictureLauncher.launch(null)
        else Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    // Columna principal sin Scaffold, con el botón de retroceso integrado arriba
    Column(
        modifier = Modifier
            .fillMaxSize()
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
            // BOTÓN DE FLECHA HACIA ATRÁS
            IconButton(
                onClick = onIrAInicio,
                modifier = Modifier
                    .background(Superficie, CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver al inicio",
                    tint = Petroleo800
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.rotulo_rincon_sur),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSecundario,
                    letterSpacing = 2.sp
                )
                Text(
                    text = stringResource(id = R.string.titulo_perfil),
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

        Card(
            colors = CardDefaults.cardColors(containerColor = Turquesa100),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier.size(76.dp).clip(CircleShape).background(Petroleo800),
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
                            .background(Superficie)
                            .clickable {
                                val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                    takePictureLauncher.launch(null)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.CameraAlt, contentDescription = stringResource(id = R.string.desc_cambiar_foto), tint = Ambar600, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(state.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Petroleo800)
                    Text(state.cargo, fontSize = 12.sp, color = TextoSecundario)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Petroleo800),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(stringResource(id = R.string.btn_editar_perfil), fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EtiquetaPerfil(stringResource(id = R.string.etiqueta_supervision))
            EtiquetaPerfil(stringResource(id = R.string.etiqueta_turno))
            EtiquetaPerfil(stringResource(id = R.string.etiqueta_accesos))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(id = R.string.campo_operacion).uppercase(), style = MaterialTheme.typography.labelSmall, color = TextoSecundario, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Superficie), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                ItemListaPerfil(Icons.Outlined.Group, stringResource(id = R.string.subparte_equipo_compartido), state.miembrosEquipo)
                HorizontalDivider(color = FondoAplicacion, modifier = Modifier.padding(horizontal = 16.dp))
                ItemListaPerfil(Icons.Outlined.CalendarToday, stringResource(id = R.string.subparte_sesiones_compl), state.sesionesCompletadas)
                HorizontalDivider(color = FondoAplicacion, modifier = Modifier.padding(horizontal = 16.dp))
                ItemListaPerfil(Icons.Outlined.VerifiedUser, stringResource(id = R.string.subparte_permisos), state.permisosCodigo)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(id = R.string.campo_preferencias).uppercase(), style = MaterialTheme.typography.labelSmall, color = TextoSecundario, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Superficie), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column {
                ItemListaPerfil(Icons.Outlined.Wifi, stringResource(id = R.string.subparte_datos_conexion), state.datosPendientes)
                HorizontalDivider(color = FondoAplicacion, modifier = Modifier.padding(horizontal = 16.dp))
                ItemListaPerfil(Icons.Outlined.Notifications, stringResource(id = R.string.subparte_notificaciones), state.estadoNotificaciones)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorSuave),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.ExitToApp, contentDescription = null, tint = Error)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(id = R.string.btn_cerrar_sesion), color = Error, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(colors = CardDefaults.cardColors(containerColor = ExitoSuave), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Wifi, contentDescription = null, tint = Exito)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.todo_sincronizado), color = Exito, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(stringResource(id = R.string.ultima_conexion), color = TextoSecundario, fontSize = 12.sp)
                }
                Icon(Icons.Outlined.Check, contentDescription = null, tint = Exito)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
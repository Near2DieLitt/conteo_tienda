package com.example.rinconsur.ui.screens


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.ui.components.RinconSurCampoTexto
import com.example.rinconsur.ui.styles.BotonPrincipalAnchoCompleto
import com.example.rinconsur.ui.styles.TarjetaDestacada
import com.example.rinconsur.viewmodel.RecuperarContrasenaViewModel

@Composable
fun RecuperarContrasenaScreen(
    modoOscuro: Boolean = false,
    onVolver: () -> Unit,
    onContrasenaCreada: () -> Unit,
    viewModel: RecuperarContrasenaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permisoNotificacionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult ={}
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED){
                permisoNotificacionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun enviarNotificacionReal(codigo: String){
        val channelId = "canal_sms_recuperacion"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SMS de Recuperación",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nuevo mensaje SMS")
            .setContentText("Rincon Sur: Tu código de recuperacion es $codigo")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }

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
            IconButton(
                onClick = onVolver,
                modifier = Modifier
                    .background(if (modoOscuro) Color(0xFF163E45) else MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver",
                    tint = if (modoOscuro) Color.White else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(24.dp))
            Icon(
                imageVector = Icons.Outlined.LockReset,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Recupera tu acceso",
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(28.dp))

            when (state.pasoActual) {
                1 -> {
                    Text("Ingresa tu correo para validar si estás registrado.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(20.dp))
                    RinconSurCampoTexto(
                        value = state.correo,
                        onValueChange = { viewModel.onCorreoCambia(it) },
                        label = "Correo",
                        placeholder = "nombre@rinconsur.cl",
                        errorMensaje = state.errorCorreo,
                        keyboardType = KeyboardType.Email,
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) }
                    )
                    Spacer(Modifier.height(20.dp))
                    BotonPrincipalAnchoCompleto(
                        texto = "Enviar Código SMS",
                        alPresionar = {
                            val codigoSms = viewModel.onEnviarSms()
                            if (codigoSms != null) {
                                enviarNotificacionReal(codigoSms)
                            }
                        }
                    )
                }

                2 -> {
                    TarjetaDestacada(modificador = Modifier.fillMaxWidth()) {
                        Text("SMS Enviado", fontWeight = FontWeight.Bold)
                        Text("Ingresa el código de 6 dígitos que enviamos a tu celular.", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(20.dp))
                    RinconSurCampoTexto(
                        value = state.codigoSmsIngresado,
                        onValueChange = { viewModel.onCodigoSmsCambia(it) },
                        label = "Código SMS",
                        errorMensaje = state.errorCodigoSms,
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(20.dp))
                    BotonPrincipalAnchoCompleto(
                        texto = "Verificar Código",
                        alPresionar = { viewModel.onVerificarCodigoSms() }
                    )
                }

                3 -> {
                    Text("Crea una nueva contraseña segura.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(20.dp))
                    RinconSurCampoTexto(
                        value = state.nuevaContrasena,
                        onValueChange = { viewModel.onNuevaContrasenaCambia(it) },
                        label = "Nueva contraseña",
                        errorMensaje = state.errorNuevaContrasena,
                        visualTransformation = if (state.nuevaContrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardType = KeyboardType.Password
                    )
                    Spacer(Modifier.height(12.dp))
                    RinconSurCampoTexto(
                        value = state.confirmarContrasena,
                        onValueChange = { viewModel.onConfirmarContrasenaCambia(it) },
                        label = "Confirmar contraseña",
                        errorMensaje = state.errorConfirmacion,
                        visualTransformation = if (state.nuevaContrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardType = KeyboardType.Password
                    )
                    Spacer(Modifier.height(20.dp))
                    BotonPrincipalAnchoCompleto(
                        texto = "Guardar Nueva Contraseña",
                        alPresionar = {
                            if (viewModel.onCrearContrasena()) {
                                Toast.makeText(context, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show()
                                onContrasenaCreada()
                            }
                        }
                    )
                }
            }
        }
    }
}
package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
import com.example.rinconsur.model.ValidadorClave
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
    var mostrarAyudaAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(state.recuperacionExitosa) {
        if (state.recuperacionExitosa) {
            onContrasenaCreada()
        }
    }

    if (mostrarAyudaAdmin) {
        AlertDialog(
            onDismissRequest = { mostrarAyudaAdmin = false },
            title = { Text("Pedir ayuda al administrador", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Si no puedes recibir el correo de verificación, solicita a un Administrador de Rincón Sur que genere una nueva clave temporal privada para tu cuenta desde la sección Equipo.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(onClick = { mostrarAyudaAdmin = false }) { Text("Entendido") }
            }
        )
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
                    .background(
                        if (modoOscuro) Color(0xFF163E45) else MaterialTheme.colorScheme.surface,
                        CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cd_volver),
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
                    Text(
                        "Ingresa tu correo electrónico. Si está registrado, te enviaremos un código de 6 dígitos a tu Gmail.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    RinconSurCampoTexto(
                        value = state.correo,
                        onValueChange = { viewModel.onCorreoCambia(it) },
                        label = "Correo",
                        placeholder = "usuario@gmail.com",
                        errorMensaje = state.errorCorreo,
                        keyboardType = KeyboardType.Email,
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) }
                    )
                    Spacer(Modifier.height(20.dp))
                    BotonPrincipalAnchoCompleto(
                        texto = "Enviar código al correo",
                        alPresionar = { viewModel.onEnviarCodigoPorCorreo() }
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = { mostrarAyudaAdmin = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("¿Sin acceso a tu correo? Pide ayuda al administrador", fontSize = 12.sp)
                    }
                }

                2 -> {
                    TarjetaDestacada(modificador = Modifier.fillMaxWidth()) {
                        Text("Revisa tu correo electrónico", fontWeight = FontWeight.Bold)
                        Text(
                            state.mensajeNeutroEnvio
                                ?: "Si el correo está registrado, enviamos un código de 6 dígitos (vence en 5 minutos).",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Intentos restantes: ${state.intentosRestantes} de 3",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    RinconSurCampoTexto(
                        value = state.codigoSmsIngresado,
                        onValueChange = { viewModel.onCodigoSmsCambia(it) },
                        label = "Código de verificación (6 dígitos)",
                        errorMensaje = state.errorCodigoSms,
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(16.dp))
                    BotonPrincipalAnchoCompleto(
                        texto = "Verificar código",
                        alPresionar = { viewModel.onVerificarCodigoSms() }
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { viewModel.volverAPasoCorreo() }) {
                            Text("Solicitar código nuevo", fontSize = 12.sp)
                        }
                        TextButton(onClick = { mostrarAyudaAdmin = true }) {
                            Text("Pedir ayuda al admin", fontSize = 12.sp)
                        }
                    }
                }

                3 -> {
                    Text(
                        "Crea una nueva contraseña que cumpla los 5 requisitos de seguridad.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    RinconSurCampoTexto(
                        value = state.nuevaContrasena,
                        onValueChange = { viewModel.onNuevaContrasenaCambia(it) },
                        label = "Nueva contraseña",
                        errorMensaje = state.errorNuevaContrasena,
                        visualTransformation = if (state.nuevaContrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardType = KeyboardType.Password,
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.onAlternarNuevaContrasenaVisible() }) {
                                Icon(
                                    imageVector = if (state.nuevaContrasenaVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (state.nuevaContrasenaVisible) {
                                        stringResource(R.string.cd_ocultar_contrasena)
                                    } else {
                                        stringResource(R.string.cd_mostrar_contrasena)
                                    }
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    ChecklistRequisitosClave(clave = state.nuevaContrasena)
                    Spacer(Modifier.height(12.dp))
                    RinconSurCampoTexto(
                        value = state.confirmarContrasena,
                        onValueChange = { viewModel.onConfirmarContrasenaCambia(it) },
                        label = "Confirmar contraseña",
                        errorMensaje = state.errorConfirmacion,
                        visualTransformation = if (state.confirmarContrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardType = KeyboardType.Password,
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.onAlternarConfirmacionVisible() }) {
                                Icon(
                                    imageVector = if (state.confirmarContrasenaVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (state.confirmarContrasenaVisible) {
                                        stringResource(R.string.cd_ocultar_contrasena)
                                    } else {
                                        stringResource(R.string.cd_mostrar_contrasena)
                                    }
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(20.dp))
                    BotonPrincipalAnchoCompleto(
                        texto = "Guardar nueva contraseña",
                        alPresionar = { viewModel.onCrearContrasena() }
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistRequisitosClave(clave: String) {
    val req = ValidadorClave.evaluar(clave)
    val items = listOf(
        req.largoMinimo to "Mínimo 8 caracteres",
        req.tieneMayuscula to "Al menos 1 letra mayúscula (A-Z)",
        req.tieneMinuscula to "Al menos 1 letra minúscula (a-z)",
        req.tieneNumero to "Al menos 1 número (0-9)",
        req.tieneEspecial to "Al menos 1 carácter especial (!@#\$%&*)"
    )
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Requisitos de seguridad:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            items.forEach { (cumple, texto) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (cumple) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (cumple) Color(0xFF1E7550) else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = texto,
                        fontSize = 11.sp,
                        color = if (cumple) Color(0xFF1E7550) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

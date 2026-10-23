package com.example.rinconsur.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.RolUsuario
import com.example.rinconsur.ui.components.RinconSurCampoTexto
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit,
    onOlvidoContrasena: () -> Unit,
    onIngresoObservador: (String) -> Unit,
    onIngresoSupervisor: (String) -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    var correo by rememberSaveable { mutableStateOf("") }

    var contrasena by rememberSaveable { mutableStateOf("") }

    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }

    var errorCorreo by rememberSaveable { mutableStateOf<String?>(null) }

    var errorContrasena by rememberSaveable { mutableStateOf<String?>(null) }

    var errorGeneral by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.usuarioAutenticado) {
        val usuario = uiState.usuarioAutenticado
            ?: return@LaunchedEffect

        when (usuario.rol) {
            RolUsuario.OBSERVADOR -> {
                onIngresoObservador(usuario.correo)
            }

            RolUsuario.SUPERVISOR -> {
                onIngresoSupervisor(usuario.correo)
            }
        }

        loginViewModel.navegacionRealizada()
    }

    LaunchedEffect(uiState.mensajeError) {
        val error = uiState.mensajeError
        if (error != null) {
            errorCorreo = error
            errorContrasena = error
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            )
    ) {
        LoginBackgroundDecoration(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = 80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 24.dp,
                    top = 80.dp,
                    end = 24.dp,
                    bottom = 24.dp
                ),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Rincón Sur",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Acceso para personal autorizado.",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.7f
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            errorGeneral?.let { mensaje ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(16.dp),
                        color =
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            RinconSurCampoTexto(
                value = correo,
                onValueChange = { nuevoCorreo ->
                    correo = nuevoCorreo

                    loginViewModel.actualizarCorreo(
                        nuevoCorreo
                    )

                    errorCorreo = null
                    errorGeneral = null
                },
                label = "Correo",
                placeholder = "nombre@rinconsur.cl",
                errorMensaje = errorCorreo,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RinconSurCampoTexto(
                value = contrasena,
                onValueChange = { nuevaContrasena ->
                    contrasena = nuevaContrasena

                    loginViewModel.actualizarContrasena(
                        nuevaContrasena
                    )

                    errorContrasena = null
                    errorGeneral = null
                },
                label = "Contraseña",
                placeholder = "Ingresa tu contraseña",
                errorMensaje = errorContrasena,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                visualTransformation =
                    if (contrasenaVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            contrasenaVisible =
                                !contrasenaVisible
                        }
                    ) {
                        Icon(
                            imageVector =
                                if (contrasenaVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                            contentDescription =
                                if (contrasenaVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                }
                        )
                    }
                }
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onOlvidoContrasena,
                    modifier = Modifier.align(
                        Alignment.CenterEnd
                    )
                ) {
                    Text(
                        text = "¿Olvidaste la contraseña?",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val correoLimpio = correo.trim()

                    errorCorreo = when {
                        correoLimpio.isBlank() -> {
                            "Ingresa tu correo."
                        }

                        !Patterns.EMAIL_ADDRESS
                            .matcher(correoLimpio)
                            .matches() -> {
                            "Ingresa un correo válido."
                        }

                        else -> null
                    }

                    errorContrasena = when {
                        contrasena.isBlank() -> {
                            "Ingresa tu contraseña."
                        }

                        contrasena.length < 8 -> {
                            "La contraseña debe tener al menos 8 caracteres."
                        }

                        else -> null
                    }

                    errorGeneral = null

                    if (
                        errorCorreo == null &&
                        errorContrasena == null
                    ) {
                        loginViewModel.actualizarCorreo(
                            correoLimpio
                        )

                        loginViewModel.actualizarContrasena(
                            contrasena
                        )

                        loginViewModel.iniciarSesion()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.alturaBoton)
            ) {
                Text(
                    text = "Ingresar",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Text(
                text = "Acceso seguro | Operación sincronizada",
                modifier = Modifier.padding(
                    top = Dimens.espacioGrande
                ),
                style = MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onCambiarTema,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(
                    top = 8.dp,
                    end = 16.dp
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector =
                    if (modoOscuro) {
                        Icons.Outlined.LightMode
                    } else {
                        Icons.Outlined.DarkMode
                    },
                contentDescription =
                    if (modoOscuro) {
                        "Activar modo claro"
                    } else {
                        "Activar modo oscuro"
                    },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoginBackgroundDecoration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(
                width = 310.dp,
                height = 220.dp
            )
            .blur(42.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.32f
                        ),
                        MaterialTheme.colorScheme
                            .primaryContainer
                            .copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.background.copy(
                            alpha = 0f
                        )
                    )
                ),
                shape = RoundedCornerShape(
                    percent = 50
                )
            )
    )
}
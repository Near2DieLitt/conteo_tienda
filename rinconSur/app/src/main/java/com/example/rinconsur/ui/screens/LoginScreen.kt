package com.example.rinconsur.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.R
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
    onIngresoAdmin: (String) -> Unit,
    onRequiereCambioClave: (String, RolUsuario) -> Unit = { _, _ -> },
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.usuarioAutenticado) {
        val usuario = uiState.usuarioAutenticado ?: return@LaunchedEffect

        if (uiState.requiereCambioClave) {
            onRequiereCambioClave(usuario.correo, usuario.rol)
        } else {
            when (usuario.rol) {
                RolUsuario.OBSERVADOR -> onIngresoObservador(usuario.correo)
                RolUsuario.SUPERVISOR -> onIngresoSupervisor(usuario.correo)
                RolUsuario.ADMIN -> onIngresoAdmin(usuario.correo)
            }
        }
        loginViewModel.navegacionRealizada()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        LoginBackgroundDecoration(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = 80.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.logo_rincon_sur),
            contentDescription = stringResource(R.string.cd_logo),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 24.dp, top = 8.dp)
                .height(56.dp),
            contentScale = ContentScale.Fit,
            alignment = Alignment.CenterStart
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 80.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            RinconSurCampoTexto(
                value = uiState.correo,
                onValueChange = loginViewModel::actualizarCorreo,
                label = "Correo",
                placeholder = "usuario@gmail.com",
                errorMensaje = uiState.errorCorreo,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = if (uiState.errorCorreo != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RinconSurCampoTexto(
                value = uiState.password,
                onValueChange = loginViewModel::actualizarContrasena,
                label = "Contraseña",
                placeholder = "Ingresa tu contraseña",
                errorMensaje = uiState.errorPassword,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                visualTransformation = if (uiState.passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = if (uiState.errorPassword != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = loginViewModel::alternarVisibilidadPassword) {
                        Icon(
                            imageVector = if (uiState.passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = if (uiState.passwordVisible) {
                                stringResource(R.string.cd_ocultar_contrasena)
                            } else {
                                stringResource(R.string.cd_mostrar_contrasena)
                            }
                        )
                    }
                }
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onOlvidoContrasena,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "¿Olvidaste la contraseña?",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.mensajeError?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (uiState.cargando) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = loginViewModel::iniciarSesion,
                enabled = !uiState.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.alturaBoton)
            ) {
                Text(
                    text = if (uiState.cargando) "Conectando..." else "Ingresar",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Text(
                text = "Acceso seguro | Operación sincronizada",
                modifier = Modifier.padding(top = Dimens.espacioGrande),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onCambiarTema,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 8.dp, end = 16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(if (modoOscuro) Color(0xFF163E45) else Color.White)
        ) {
            Icon(
                imageVector = if (modoOscuro) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = if (modoOscuro) {
                    stringResource(R.string.cd_modo_claro)
                } else {
                    stringResource(R.string.cd_modo_oscuro)
                },
                tint = if (modoOscuro) Color.White else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoginBackgroundDecoration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 310.dp, height = 220.dp)
            .blur(42.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.20f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0f)
                    )
                ),
                shape = RoundedCornerShape(percent = 50)
            )
    )
}

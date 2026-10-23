package com.example.rinconsur.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.ui.components.RinconSurCampoTexto
import com.example.rinconsur.ui.styles.BotonPrincipalAnchoCompleto
import com.example.rinconsur.ui.styles.TarjetaDestacada
import com.example.rinconsur.viewmodel.RecuperarContrasenaViewModel

@Composable
fun RecuperarContrasenaScreen(
    onVolver: () -> Unit,
    onContrasenaCreada: () -> Unit,
    viewModel: RecuperarContrasenaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var correo by rememberSaveable { mutableStateOf("") }
    var errorCorreo by rememberSaveable { mutableStateOf<String?>(null) }
    var enlaceEnviado by rememberSaveable { mutableStateOf(false) }

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
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Spacer(Modifier.height(24.dp))

            Icon(
                imageVector = if (enlaceEnviado) {
                    Icons.Outlined.CheckCircle
                } else {
                    Icons.Outlined.LockReset
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (enlaceEnviado) {
                    "Revisa tu correo"
                } else {
                    "Recupera tu acceso"
                },
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (enlaceEnviado) {
                    "Enviamos las instrucciones a $correo. El enlace será válido durante 30 minutos."
                } else {
                    "Ingresa tu correo y te enviaremos un enlace para crear una nueva contraseña."
                },
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(28.dp))

            if (enlaceEnviado) {
                TarjetaDestacada(
                    modificador = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Solicitud Registrada",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Si no encuentras el mensaje, revisa tu bandeja de entrada",
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        enlaceEnviado = false
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Enviar Nuevamente")
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val creada = viewModel.onCrearContrasena()
                        if (creada) {
                            onContrasenaCreada()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear contraseña")
                }

                Spacer(Modifier.height(12.dp))

                BotonPrincipalAnchoCompleto(
                    texto = "Volver a iniciar sesión",
                    alPresionar = onVolver
                )
            } else {
                RinconSurCampoTexto(
                    value = correo,
                    onValueChange = {
                        correo = it
                        errorCorreo = null
                    },
                    label = "Correo",
                    placeholder = "nombre@rinconsur.cl",
                    errorMensaje = errorCorreo,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null
                        )
                    }
                )

                Spacer(Modifier.height(20.dp))

                BotonPrincipalAnchoCompleto(
                    texto = "Enviar Enlace",
                    alPresionar = {
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

                            !correoLimpio.endsWith(
                                "@rinconsur.cl",
                                ignoreCase = true
                            ) -> {
                                "Usa tu correo @rinconsur.cl."
                            }

                            else -> null
                        }

                        if (errorCorreo == null) {
                            correo = correoLimpio
                            enlaceEnviado = true
                            viewModel.onCorreoCambia(correoLimpio)
                        }
                    }
                )
            }
        }
    }
}

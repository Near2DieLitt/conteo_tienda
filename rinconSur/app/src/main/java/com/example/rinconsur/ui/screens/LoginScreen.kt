package com.example.rinconsur.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.R


@Composable
fun LoginScreen(
    modoOscuro: Boolean,
    onCambiarTema: () -> Unit,
    onLoginCorrecto: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("daniela.pizarro@rinconsur.cl") }
    var contrasena by rememberSaveable { mutableStateOf("") }

    var errorCorreo by rememberSaveable {mutableStateOf<String?>(null)}
    var errorContrasena by rememberSaveable { mutableStateOf<String?>(null) }
    var errorGeneral by rememberSaveable { mutableStateOf<String?>(null) }


    Column(
        modifier = modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ){
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.End
        ) {
            TextButton(onClick = onCambiarTema) {
                Text(if (modoOscuro) "Modo Claro" else "Modo Oscuro")
            }
        }
        Text(
            text = "Rincón Sur",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Iniciar Sesion",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Acceso para personal autorizado.",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(28.dp))
        errorGeneral?.let { mensaje ->
            Card(modifier = Modifier.fillMaxWidth()
                .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {

                Text(
                    text = mensaje ,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                errorCorreo = null
                errorGeneral = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo") },
            placeholder = {
                Text("nombre@rinconsur.cl") },
            singleLine = true,
            isError = errorCorreo != null,
            supportingText = {
                errorCorreo?.let { Text(it) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )
        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                errorContrasena = null
                errorGeneral = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            singleLine = true,
            isError = errorContrasena != null,
            supportingText = {
                errorContrasena?.let { Text(it) }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val correoLimpio = correo.trim()
                errorCorreo = when {
                    correoLimpio.isBlank() -> "Ingresa tu correo."
                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(correoLimpio)
                        .matches() -> "Ingresa un correo válido."
                    else -> null
                }
                errorContrasena = when {
                    contrasena.isBlank() -> "Ingresa tu contraseña"
                    contrasena.length < 8 -> "La contraseña debe tener almenos 8 caracteres."
                    else -> null
                }
                if(errorCorreo == null && errorContrasena == null) {
                    if (correoLimpio.equals(
                        "daniela.pizarro@rinconsur.cl", ignoreCase = true
                    )&& contrasena == "12345678"){
                        onLoginCorrecto()
                    } else {
                        errorGeneral = "Correo o contraseña Incorrectos."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Turquesa700
            )
        ) { }

    }
}



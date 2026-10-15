package com.example.rinconsur.ui.styles


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

object EstiloCampoTexto {
    val Forma = RoundedCornerShape(14.dp)
}

@Composable
fun CampoTextoRinconSur(
    valor: String,
    alCambiarValor: (String) -> Unit,
    etiqueta: String,
    modificador: Modifier = Modifier,
    textoAyuda: String? = null,
    mensajeError: String? = null,
    unaSolaLinea: Boolean = true,
    transformacionVisual: VisualTransformation =
        VisualTransformation.None
) {
    val tieneError = mensajeError != null

    OutlinedTextField(
        value = valor,
        onValueChange = alCambiarValor,
        modifier = modificador,
        label = {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        supportingText = {
            when {
                tieneError -> {
                    Text(
                        text = mensajeError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                textoAyuda != null -> {
                    Text(
                        text = textoAyuda,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        isError = tieneError,
        singleLine = unaSolaLinea,
        visualTransformation = transformacionVisual,
        shape = EstiloCampoTexto.Forma,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor =
                MaterialTheme.colorScheme.surface,
            unfocusedContainerColor =
                MaterialTheme.colorScheme.surface,
            errorContainerColor =
                MaterialTheme.colorScheme.surface,

            focusedIndicatorColor =
                MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor =
                MaterialTheme.colorScheme.outline,
            errorIndicatorColor =
                MaterialTheme.colorScheme.error,

            focusedLabelColor =
                MaterialTheme.colorScheme.primary,
            unfocusedLabelColor =
                MaterialTheme.colorScheme.onSurfaceVariant,
            errorLabelColor =
                MaterialTheme.colorScheme.error,

            cursorColor =
                MaterialTheme.colorScheme.primary,
            errorCursorColor =
                MaterialTheme.colorScheme.error
        )
    )
}

@Composable
fun CampoContrasena(
    valor: String,
    alCambiarValor: (String) -> Unit,
    modificador: Modifier = Modifier,
    mensajeError: String? = null
) {
    CampoTextoRinconSur(
        valor = valor,
        alCambiarValor = alCambiarValor,
        etiqueta = "Contraseña",
        modificador = modificador,
        mensajeError = mensajeError,
        transformacionVisual = PasswordVisualTransformation()
    )
}
package com.example.rinconsur.ui.styles
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object EstiloBoton {
    val FormaPrincipal = RoundedCornerShape(16.dp)
    val FormaSecundario = RoundedCornerShape(16.dp)

    val Alto = 52.dp

    val EspaciadoInterno = PaddingValues(
        horizontal = 24.dp,
        vertical = 12.dp
    )
}

@Composable
fun BotonPrincipal(
    texto: String,
    alPresionar: () -> Unit,
    modificador: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Button(
        onClick = alPresionar,
        modifier = modificador.height(EstiloBoton.Alto),
        enabled = habilitado,
        shape = EstiloBoton.FormaPrincipal,
        contentPadding = EstiloBoton.EspaciadoInterno,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor =
                MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun BotonPrincipalAnchoCompleto(
    texto: String,
    alPresionar: () -> Unit,
    modificador: Modifier = Modifier,
    habilitado: Boolean = true
) {
    BotonPrincipal(
        texto = texto,
        alPresionar = alPresionar,
        modificador = modificador.fillMaxWidth(),
        habilitado = habilitado
    )
}

@Composable
fun BotonSecundario(
    texto: String,
    alPresionar: () -> Unit,
    modificador: Modifier = Modifier,
    habilitado: Boolean = true
) {
    OutlinedButton(
        onClick = alPresionar,
        modifier = modificador.height(EstiloBoton.Alto),
        enabled = habilitado,
        shape = EstiloBoton.FormaSecundario,
        contentPadding = EstiloBoton.EspaciadoInterno,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor =
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = ButtonDefaults.outlinedButtonBorder(
            enabled = habilitado
        )
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
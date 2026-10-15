package com.example.rinconsur.ui.styles

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object EstiloEtiqueta {
    val Forma = RoundedCornerShape(50)
    val Espaciado = PaddingValues(
        horizontal = 12.dp,
        vertical = 6.dp
    )
}

@Composable
private fun EtiquetaBase(
    texto: String,
    colorFondo: Color,
    colorTexto: Color,
    modificador: Modifier = Modifier
) {
    Surface(
        modifier = modificador,
        shape = EstiloEtiqueta.Forma,
        color = colorFondo,
        contentColor = colorTexto
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(
                EstiloEtiqueta.Espaciado
            ),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun EtiquetaPrincipal(
    texto: String,
    modificador: Modifier = Modifier
) {
    EtiquetaBase(
        texto = texto,
        modificador = modificador,
        colorFondo =
            MaterialTheme.colorScheme.primaryContainer,
        colorTexto =
            MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
fun EtiquetaSecundaria(
    texto: String,
    modificador: Modifier = Modifier
) {
    EtiquetaBase(
        texto = texto,
        modificador = modificador,
        colorFondo =
            MaterialTheme.colorScheme.secondaryContainer,
        colorTexto =
            MaterialTheme.colorScheme.onSecondaryContainer
    )
}

@Composable
fun EtiquetaAdvertencia(
    texto: String,
    modificador: Modifier = Modifier
) {
    EtiquetaBase(
        texto = texto,
        modificador = modificador,
        colorFondo =
            MaterialTheme.colorScheme.tertiaryContainer,
        colorTexto =
            MaterialTheme.colorScheme.onTertiaryContainer
    )
}

@Composable
fun EtiquetaError(
    texto: String,
    modificador: Modifier = Modifier
) {
    EtiquetaBase(
        texto = texto,
        modificador = modificador,
        colorFondo =
            MaterialTheme.colorScheme.errorContainer,
        colorTexto =
            MaterialTheme.colorScheme.onErrorContainer
    )
}
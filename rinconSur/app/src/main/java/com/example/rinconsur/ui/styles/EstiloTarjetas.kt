package com.example.rinconsur.ui.styles


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object EstiloTarjeta {
    val FormaPrincipal = RoundedCornerShape(20.dp)
    val FormaCompacta = RoundedCornerShape(16.dp)

    val ElevacionPrincipal = 2.dp
    val ElevacionDestacada = 6.dp

    val EspaciadoPrincipal = PaddingValues(20.dp)
    val EspaciadoCompacto = PaddingValues(16.dp)
}

@Composable
fun TarjetaPrincipal(
    modificador: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modificador,
        shape = EstiloTarjeta.FormaPrincipal,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = EstiloTarjeta.ElevacionPrincipal
        )
    ) {
        Column(
            modifier = Modifier.padding(
                EstiloTarjeta.EspaciadoPrincipal
            ),
            content = contenido
        )
    }
}

@Composable
fun TarjetaDestacada(
    modificador: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modificador,
        shape = EstiloTarjeta.FormaPrincipal,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer,
            contentColor =
                MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = EstiloTarjeta.ElevacionDestacada
        )
    ) {
        Column(
            modifier = Modifier.padding(
                EstiloTarjeta.EspaciadoPrincipal
            ),
            content = contenido
        )
    }
}

@Composable
fun TarjetaCompacta(
    modificador: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modificador,
        shape = EstiloTarjeta.FormaCompacta,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor =
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(
                EstiloTarjeta.EspaciadoCompacto
            ),
            content = contenido
        )
    }
}
package com.example.rinconsur.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DestinoBarra(val etiqueta: String, val icono: ImageVector) {
    INICIO("Inicio", Icons.Outlined.Home),
    SESIONES("Sesiones", Icons.Outlined.CalendarToday),
    ANALITICA("Analítica", Icons.Outlined.BarChart),
    EQUIPO("Equipo", Icons.Outlined.Group),
    PERFIL("Perfil", Icons.Outlined.Person)
}

@Composable
fun BarraNavegacionInferior(
    seleccionado: DestinoBarra,
    onNavegar: (DestinoBarra) -> Unit,
    modoOscuro: Boolean = false
) {
    NavigationBar(
        containerColor = if (modoOscuro) Color(0xFF071F24) else Color(0xFF103336),
        contentColor = Color.White
    ) {
        listOf(
            DestinoBarra.INICIO,
            DestinoBarra.SESIONES,
            DestinoBarra.ANALITICA,
            DestinoBarra.EQUIPO,
            DestinoBarra.PERFIL
        ).forEach { destino ->
            val activo = destino == seleccionado

            val colorContenido by animateColorAsState(
                targetValue = if (activo) Color(0xFFE2A253) else Color.LightGray,
                animationSpec = tween(300),
                label = "colorItem"
            )
            val colorPildora by animateColorAsState(
                targetValue = if (activo) Color(0xFFE2A253).copy(alpha = 0.18f) else Color.Transparent,
                animationSpec = tween(300),
                label = "pildora"
            )
            val escala by animateFloatAsState(
                targetValue = if (activo) 1.15f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "escala"
            )

            NavigationBarItem(
                selected = activo,
                onClick = { if (!activo) onNavegar(destino) },
                icon = {
                    Box(
                        modifier = Modifier
                            .scale(escala)
                            .background(colorPildora, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = destino.icono,
                            contentDescription = destino.etiqueta,
                            tint = colorContenido
                        )
                    }
                },
                label = {
                    Text(
                        text = destino.etiqueta,
                        fontSize = 11.sp,
                        color = colorContenido,
                        fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}
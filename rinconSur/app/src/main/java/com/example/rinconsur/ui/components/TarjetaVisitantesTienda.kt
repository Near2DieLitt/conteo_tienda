package com.example.rinconsur.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.rinconsur.model.Accesos
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.model.ResumenVisitantes

@Composable
fun TarjetaVisitantesTienda(
    resumen: ResumenVisitantes,
    entradaActiva: String,
    colorFondoContenedor: Color,
    verdeResaltado: Color,
    onRegistrarEntrada: (rango: RangoEtario) -> Unit,
    onRegistrarSalida: () -> Unit,
    onEjecutarCierre: () -> Unit,
    onEscanearBoleta: () -> Unit,
    onFinalizarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rangoSeleccionado by remember { mutableStateOf(RangoEtario.ADULTO) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondoContenedor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Storefront, contentDescription = null, tint = verdeResaltado)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Visitantes en Tienda",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = verdeResaltado
                    )
                }

                Surface(
                    color = Color(0xFF1E7550).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Tiempo Real",
                        color = Color(0xFF1E7550),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = verdeResaltado.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Sesión Activa: $entradaActiva (${Accesos.nombre(entradaActiva)})",
                                color = verdeResaltado,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        TextButton(
                            onClick = onFinalizarSesion,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Text("Finalizar", color = Color(0xFFB85D19), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estado: En_Tienda",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${resumen.enTienda}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = verdeResaltado
                            )
                            Text(
                                text = "visitante(s) activo(s)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = onEscanearBoleta,
                            colors = ButtonDefaults.buttonColors(containerColor = verdeResaltado),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Boleta", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF4F7F6))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Compró", fontSize = 11.sp, color = Color.Gray)
                            Text("${resumen.compro}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E7550))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No Compró", fontSize = 11.sp, color = Color.Gray)
                            Text("${resumen.noCompro}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB85D19))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Hoy", fontSize = 11.sp, color = Color.Gray)
                            Text("${resumen.total}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = verdeResaltado)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Rango etario del visitante:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val rangos = listOf(
                            RangoEtario.JOVEN to "Joven (<30)",
                            RangoEtario.ADULTO to "Adulto (30-49)",
                            RangoEtario.PERSONA_MAYOR to "Mayor (50+)"
                        )
                        rangos.forEach { (rango, label) ->
                            val sel = rangoSeleccionado == rango
                            FilterChip(
                                selected = sel,
                                onClick = { rangoSeleccionado = rango },
                                label = { Text(label, fontSize = 10.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = verdeResaltado,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRegistrarEntrada(rangoSeleccionado) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Visitante", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onRegistrarSalida,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("- Visitante", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onEjecutarCierre,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB85D19))
                    ) {
                        Icon(Icons.Outlined.LockClock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cierre Diario", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

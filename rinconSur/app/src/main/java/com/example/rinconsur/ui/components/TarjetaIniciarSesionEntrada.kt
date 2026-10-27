package com.example.rinconsur.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rinconsur.model.Accesos

@Composable
fun TarjetaIniciarSesionEntrada(
    colorFondoContenedor: Color,
    verdeResaltado: Color,
    onIniciarSesion: (codigo: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var entradaSeleccionada by remember { mutableStateOf("A1") }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorFondoContenedor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Sensors, contentDescription = null, tint = verdeResaltado)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Visitantes en Tienda",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = verdeResaltado
                )
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
                    Text(
                        text = "Inicia sesión con una entrada válida para activar el control de visitantes en tiempo real.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Seleccionar entrada:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = verdeResaltado
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Accesos.lista.forEach { codigo ->
                            val seleccionado = entradaSeleccionada == codigo
                            FilterChip(
                                selected = seleccionado,
                                onClick = { entradaSeleccionada = codigo },
                                label = {
                                    Text(
                                        text = codigo,
                                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = verdeResaltado,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Acceso $entradaSeleccionada: ${Accesos.nombre(entradaSeleccionada)}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onIniciarSesion(entradaSeleccionada) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = verdeResaltado),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Iniciar Sesión en $entradaSeleccionada", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

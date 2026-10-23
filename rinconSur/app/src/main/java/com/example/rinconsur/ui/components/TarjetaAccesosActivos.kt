package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TarjetaAccesosActivos(
    totalFlujo: String,
    porcentajeMeta: Float,
    textoMeta: String,
    zonasFlujo: String,
    colorFondoContenedor: Color,
    verdeResaltado: Color,
    modifier: Modifier = Modifier
) {
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
                    Icon(Icons.Outlined.Group, contentDescription = null, tint = verdeResaltado)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accesos activos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = verdeResaltado
                    )
                }
                TextButton(onClick = { }) {
                    Text("Ver todas", color = verdeResaltado, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = false, onClick = { })
                            Column {
                                Text("Flujo general", fontWeight = FontWeight.Bold, color = verdeResaltado)
                                Text("Actualizado hace 2 min", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Text(totalFlujo, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = verdeResaltado)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { porcentajeMeta },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = verdeResaltado,
                        trackColor = Color(0xFFD3E3DF)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(zonasFlujo, fontSize = 10.sp, color = Color.Gray)
                        Text(textoMeta, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = verdeResaltado)
                    }
                }
            }
        }
    }
}
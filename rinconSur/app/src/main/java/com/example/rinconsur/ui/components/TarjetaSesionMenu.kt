package com.example.rinconsur.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rinconsur.viewmodel.ItemSessionUi

@Composable
fun TarjetaSesionMenu(
    sesion: ItemSessionUi,
    onCheckedChange: (Boolean) -> Unit,
    onClickDetalle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (colorFondoBadge, colorTextoBadge) = when (sesion.estado.lowercase()) {
        "finalizada" -> Color(0xFFF3E0D1) to Color(0xFFB85D19)
        "validada" -> Color(0xFFD4ECE2) to Color(0xFF1E7550)
        else -> Color(0xFFE2ECEC) to Color(0xFF336666) // Programada
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = sesion.completada,
                        onCheckedChange = onCheckedChange,
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD9773A))
                    )
                    Column {
                        Text(
                            text = sesion.titulo,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF12343B)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sesion.horaUbicacion,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Surface(
                    color = colorFondoBadge,
                    shape = RoundedCornerShape(20)
                ) {
                    Text(
                        text = sesion.estado,
                        color = colorTextoBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (sesion.mostrarBotonIr) {
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = onClickDetalle,
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F5961))
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Ir",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
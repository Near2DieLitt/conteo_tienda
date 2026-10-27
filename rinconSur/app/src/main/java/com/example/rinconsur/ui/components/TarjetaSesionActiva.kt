package com.example.rinconsur.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.SensorsOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TarjetaSinSesionActiva(
    colorFondoContenedor: Color,
    verdeResaltado: Color,
    onIrASesiones: () -> Unit,
    modoOscuro: Boolean = false,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.SensorsOff, contentDescription = null, tint = verdeResaltado)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Control de Visitantes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = verdeResaltado
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (modoOscuro) Color(0xFF1D4950) else Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sin sesión de entrada activa",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = verdeResaltado
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Para activar el panel de visitantes, crea/programa una sesión con código de acceso válido (A1, A2, A3 o A4) desde la sección 'Sesiones'.",
                        fontSize = 12.sp,
                        color = if (modoOscuro) Color(0xFFB9CACB) else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onIrASesiones,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = verdeResaltado),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ir a Sesiones", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

package com.example.rinconsur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rinconsur.ui.theme.Petroleo800
import com.example.rinconsur.ui.theme.TextoSecundario
import com.example.rinconsur.ui.theme.Turquesa100

@Composable
fun EtiquetaPerfil(texto: String) {
    Box(
        modifier = Modifier
            .background(Turquesa100, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(texto, color = Petroleo800, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ItemListaPerfil(icono: ImageVector, titulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(titulo, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Text(valor, color = TextoSecundario, fontSize = 14.sp)
    }
}
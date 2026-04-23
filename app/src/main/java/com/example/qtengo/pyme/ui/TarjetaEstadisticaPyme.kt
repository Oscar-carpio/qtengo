package com.example.qtengo.pyme.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente genérico para mostrar estadísticas o resúmenes (KPIs).
 */
@Composable
fun TarjetaEstadisticaPyme(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier,
    colorContenido: Color = Color.White,
    colorValor: Color? = null
) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorValor ?: colorContenido
            )
            Text(
                text = titulo,
                color = colorContenido,
                fontSize = 12.sp
            )
        }
    }
}

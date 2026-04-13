package com.example.qtengo.pyme.ui.finanzas.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tarjeta de resumen de balance financiero (Ingresos, Gastos, Neto).
 */
@Composable
fun BalanceCard(titulo: String, cantidad: Double, colorFondo: Color, esNeto: Boolean = false) {
    val colorTextoCantidad = if (esNeto && cantidad < 0) Color(0xFFFF5252) else Color.White
    Card(
        colors = CardDefaults.cardColors(containerColor = colorFondo), 
        modifier = Modifier.width(100.dp).height(70.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), 
            verticalArrangement = Arrangement.Center, 
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo, color = Color.White, fontSize = 10.sp)
            Text("${cantidad}€", color = colorTextoCantidad, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

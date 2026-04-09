package com.example.qtengo.familiar.ui.gastos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GastosResumenCard(
    totalGastos: Double,
    totalRecurrentes: Double,
    presupuesto: Double?,
    onCambiarPresupuesto: () -> Unit
) {
    val totalMes = totalGastos + totalRecurrentes
    val porcentaje = if ((presupuesto ?: 0.0) > 0)
        (totalMes / presupuesto!!).toFloat().coerceIn(0f, 1f) else 0f
    val colorBarra = when {
        porcentaje >= 1f -> Color(0xFFD32F2F)
        porcentaje >= 0.75f -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A6B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total estimado este mes",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "%.2f €".format(totalMes),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "Puntuales: %.2f €".format(totalGastos),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Fijos: %.2f €".format(totalRecurrentes),
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            if (presupuesto != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Presupuesto: %.2f €".format(presupuesto),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { porcentaje },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = colorBarra,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        porcentaje >= 1f -> "⚠️ Has superado el presupuesto"
                        porcentaje >= 0.75f -> "⚠️ Llevas el ${(porcentaje * 100).toInt()}% del presupuesto"
                        else -> "Llevas el ${(porcentaje * 100).toInt()}% del presupuesto"
                    },
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onCambiarPresupuesto,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Text(
                    text = if (presupuesto != null) "✏️ Cambiar presupuesto" else "＋ Establecer presupuesto",
                    fontSize = 13.sp
                )
            }
        }
    }
}
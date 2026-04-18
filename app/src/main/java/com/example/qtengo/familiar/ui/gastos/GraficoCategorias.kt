package com.example.qtengo.familiar.ui.gastos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val COLORES_CATEGORIAS = listOf(
    Color(0xFF1A3A6B),
    Color(0xFF2196F3),
    Color(0xFF4CAF50),
    Color(0xFFFF9800),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4),
    Color(0xFFFF5722),
    Color(0xFF607D8B),
    Color(0xFFFFC107)
)

@Composable
fun GraficoCategorias(
    gastosPorCategoria: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    if (gastosPorCategoria.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin datos para mostrar", color = Color.Gray)
        }
        return
    }

    val total = gastosPorCategoria.values.sum()
    val entradas = gastosPorCategoria.entries.sortedByDescending { it.value }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Gastos por categoría",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1A3A6B)
            )
            Text(
                text = "Total: %.2f€".format(total),
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gráfico de tarta
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                var startAngle = -90f
                entradas.forEachIndexed { index, (_, valor) ->
                    val sweepAngle = (valor / total * 360f).toFloat()
                    val color = COLORES_CATEGORIAS[index % COLORES_CATEGORIAS.size]
                    val radius = size.minDimension / 2f
                    val topLeft = Offset(
                        x = size.width / 2f - radius,
                        y = size.height / 2f - radius
                    )
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2)
                    )
                    startAngle += sweepAngle
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Leyenda
            entradas.forEachIndexed { index, (categoria, valor) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(
                                color = COLORES_CATEGORIAS[index % COLORES_CATEGORIAS.size],
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = categoria,
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "%.2f€".format(valor),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A3A6B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${(valor / total * 100).toInt()}%)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
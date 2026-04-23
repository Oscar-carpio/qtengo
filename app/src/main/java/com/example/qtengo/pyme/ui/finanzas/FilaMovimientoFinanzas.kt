package com.example.qtengo.pyme.ui.finanzas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qtengo.core.domain.models.FinanceMovement

/**
 * Fila individual para mostrar un movimiento financiero (Ingreso/Gasto).
 */
@Composable
fun FilaMovimientoFinanzas(
    movimiento: FinanceMovement,
    onEliminar: () -> Unit,
    onEditar: () -> Unit
) {
    val esGasto = movimiento.type == "GASTO"
    val colorMonto = if (esGasto) Color(0xFFC62828) else Color(0xFF2E7D32)
    val esNomina = movimiento.id.startsWith("nomina_")

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de tipo de movimiento
            Box(
                modifier = Modifier.size(40.dp).background(colorMonto.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (esGasto) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = colorMonto,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = movimiento.concept, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = movimiento.date, fontSize = 12.sp, color = Color.Gray)
                    if (esNomina) {
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.background(Color(0xFF1565C0).copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("NÓMINA", color = Color(0xFF1565C0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Text(
                text = "${if (esGasto) "-" else "+"}${String.format("%.2f€", movimiento.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colorMonto
            )

            // Solo permitir editar/borrar si NO es una nómina autogenerada
            if (!esNomina) {
                Row {
                    IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onEliminar) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) }
                }
            } else {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Lock, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

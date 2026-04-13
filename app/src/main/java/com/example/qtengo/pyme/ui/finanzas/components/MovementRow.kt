/**
 * Fila representativa de un movimiento financiero individual.
 * Muestra el concepto, fecha, importe (con distintivo visual para ingresos/gastos)
 * y proporciona acciones de edición y borrado para registros no automáticos.
 */
package com.example.qtengo.pyme.ui.finanzas.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qtengo.core.domain.models.FinanceMovement

@Composable
fun MovementRow(movement: FinanceMovement, onDelete: () -> Unit, onEdit: () -> Unit) {
    val isEditable = !movement.id.startsWith("nomina_")
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(movement.concept, fontWeight = FontWeight.Bold)
                    Text(movement.date, fontSize = 12.sp, color = Color.Gray)
                }
                
                Surface(
                    color = if (movement.type == "INGRESO") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = "${if (movement.type == "INGRESO") "+" else "-"}${movement.amount}€",
                            color = if (movement.type == "INGRESO") Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (isEditable) {
                    Row {
                        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) }
                    }
                }
            }
            if (movement.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(text = movement.notes, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

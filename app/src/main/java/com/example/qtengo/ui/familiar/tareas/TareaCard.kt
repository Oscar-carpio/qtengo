package com.example.qtengo.ui.familiar.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TareaCard(
    tarea: Tarea,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colorPrioridad = when (tarea.prioridad) {
        "Alta" -> Color(0xFFD32F2F)
        "Media" -> Color(0xFFF57C00)
        else -> Color(0xFF388E3C)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tarea.completada) Color(0xFFF5F5F5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de prioridad
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(
                        if (tarea.completada) Color.LightGray else colorPrioridad,
                        RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1A3A6B))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarea.titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tarea.completada) Color.Gray else Color(0xFF1A3A6B),
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else TextDecoration.None
                )
                if (tarea.descripcion.isNotBlank()) {
                    Text(
                        text = tarea.descripcion,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                if (tarea.fecha.isNotBlank()) {
                    Text(
                        text = "📅 ${tarea.fecha}",
                        fontSize = 11.sp,
                        color = if (tarea.completada) Color.LightGray else colorPrioridad
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar tarea",
                    tint = Color(0xFF1565C0)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar tarea",
                    tint = Color(0xFF1A3A6B)
                )
            }
        }
    }
}
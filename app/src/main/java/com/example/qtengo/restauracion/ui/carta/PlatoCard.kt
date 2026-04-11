package com.example.qtengo.restauracion.ui.carta

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlatoCard(
    plato: Plato,
    onToggleDisponible: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plato.disponible) Color.White else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plato.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (plato.disponible) Color(0xFF1A3A6B) else Color.Gray
                )
                if (plato.descripcion.isNotBlank()) {
                    Text(text = plato.descripcion, fontSize = 12.sp, color = Color.Gray)
                }
                Text(
                    text = "${"%.2f".format(plato.precio)} €",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )
            }
            Switch(
                checked = plato.disponible,
                onCheckedChange = { onToggleDisponible() },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1A3A6B))
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar plato", tint = Color(0xFF1A3A6B))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar plato", tint = Color.LightGray)
            }
        }
    }
}


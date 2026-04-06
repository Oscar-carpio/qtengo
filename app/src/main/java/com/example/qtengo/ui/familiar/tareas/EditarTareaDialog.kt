package com.example.qtengo.ui.familiar.tareas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditarTareaDialog(
    tarea: Tarea,
    onConfirm: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var titulo by remember { mutableStateOf(tarea.titulo) }
    var descripcion by remember { mutableStateOf(tarea.descripcion) }
    var fecha by remember { mutableStateOf(tarea.fecha) }
    val prioridades = listOf("Alta", "Media", "Baja")
    var prioridadSeleccionada by remember { mutableStateOf(tarea.prioridad) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (dd/MM/yyyy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Prioridad",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    prioridades.forEach { prioridad ->
                        FilterChip(
                            selected = prioridadSeleccionada == prioridad,
                            onClick = { prioridadSeleccionada = prioridad },
                            label = { Text(prioridad) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (prioridad) {
                                    "Alta" -> Color(0xFFD32F2F)
                                    "Media" -> Color(0xFFF57C00)
                                    else -> Color(0xFF388E3C)
                                },
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (titulo.isNotBlank()) {
                    onConfirm(titulo, descripcion, fecha, prioridadSeleccionada)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
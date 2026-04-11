package com.example.qtengo.familiar.ui.tareas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

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

    var errorTitulo by remember { mutableStateOf("") }
    var errorFecha by remember { mutableStateOf("") }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")) }

    // FIX WARN — valida formato y si la fecha es futura
    fun validarFecha(input: String): String {
        if (input.isBlank()) return "La fecha es obligatoria"
        sdf.isLenient = false
        val fechaParsed = runCatching { sdf.parse(input) }.getOrNull()
            ?: return "Formato inválido — usa dd/MM/yyyy"
        if (fechaParsed.before(Date())) return "La fecha ya ha pasado — no se reprogramará notificación"
        return ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = {
                        titulo = it
                        errorTitulo = ""
                    },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorTitulo.isNotEmpty(),
                    supportingText = {
                        if (errorTitulo.isNotEmpty())
                            Text(errorTitulo, color = MaterialTheme.colorScheme.error)
                    }
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
                    onValueChange = {
                        fecha = it
                        errorFecha = ""
                    },
                    label = { Text("Fecha (dd/MM/yyyy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorFecha.isNotEmpty(),
                    supportingText = {
                        if (errorFecha.isNotEmpty()) {
                            val esPasada = errorFecha.contains("pasado")
                            Text(
                                text = errorFecha,
                                color = if (esPasada) Color(0xFFF57C00)
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
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
                var valido = true

                if (titulo.isBlank()) {
                    errorTitulo = "El título no puede estar vacío"
                    valido = false
                }

                val errorFechaActual = validarFecha(fecha)
                if (errorFechaActual.isNotEmpty()) {
                    errorFecha = errorFechaActual
                    if (!errorFechaActual.contains("pasado")) valido = false
                }

                if (valido) {
                    onConfirm(titulo.trim(), descripcion.trim(), fecha.trim(), prioridadSeleccionada)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
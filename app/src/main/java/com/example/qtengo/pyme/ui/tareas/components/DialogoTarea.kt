/**
 * Diálogo para la creación y edición de tareas en la Agenda Pyme.
 * Incluye validaciones y mensajes de error específicos.
 */
package com.example.qtengo.pyme.ui.tareas.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.qtengo.core.domain.models.Task
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoTarea(
    titulo: String, 
    task: Task? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: "MEDIA") }
    var date by remember { mutableStateOf(task?.date ?: "") }
    
    var titleError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { }, // Evita el cierre al pulsar fuera
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { 
                        title = it 
                        if (it.isNotBlank()) titleError = null
                    }, 
                    label = { Text("Título *") }, 
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(it, color = Color.Red, fontSize = 12.sp) } }
                )
                
                OutlinedTextField(
                    value = description, 
                    onValueChange = { description = it }, 
                    label = { Text("Descripción") }, 
                    modifier = Modifier.fillMaxWidth()
                )
                
                Column {
                    Text("Prioridad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BAJA", "MEDIA", "ALTA").forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p) }
                            )
                        }
                    }
                }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { },
                        label = { Text("Fecha Programada (Opcional)") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { 
                            Icon(
                                imageVector = Icons.Default.CalendarToday, 
                                contentDescription = "Seleccionar fecha"
                            ) 
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { 
                                abrirCalendario(context, date) { nuevaFecha -> date = nuevaFecha } 
                            }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (title.isBlank()) {
                    titleError = "El título es obligatorio"
                } else {
                    onConfirm(title, description, priority, date)
                }
            }) { 
                Text("Guardar") 
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Cancelar") } 
        }
    )
}

private fun abrirCalendario(context: android.content.Context, fechaActual: String, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    if (fechaActual.isNotBlank()) {
        try {
            sdf.parse(fechaActual)?.let { calendar.time = it }
        } catch (_: Exception) {}
    }

    DatePickerDialog(
        context,
        { _, y, m, d ->
            calendar.set(y, m, d)
            onDateSelected(sdf.format(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

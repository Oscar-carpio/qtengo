package com.example.qtengo.ui.familiar.gastos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GastoRecurrenteDialog(
    gastoRecurrente: GastoRecurrente?,
    onConfirm: (String, Double, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var descripcion by remember { mutableStateOf(gastoRecurrente?.descripcion ?: "") }
    var cantidad by remember { mutableStateOf(gastoRecurrente?.cantidad?.toString() ?: "") }
    var fechaCobro by remember { mutableStateOf(gastoRecurrente?.fechaCobro ?: "") }
    val categorias = listOf("Alimentación", "Suministros", "Ocio", "Transporte", "Salud", "Otros")
    var categoriaSeleccionada by remember { mutableStateOf(gastoRecurrente?.categoria ?: "Suministros") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (gastoRecurrente == null) "Nuevo gasto fijo" else "Editar gasto fijo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (ej: Alquiler, Luz...)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad (€)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = fechaCobro,
                    onValueChange = { fechaCobro = it },
                    label = { Text("Fecha de cobro (dd/MM/yyyy)") },
                    singleLine = true
                )
                Text(
                    text = "Categoría",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categorias.forEach { categoria ->
                        FilterChip(
                            selected = categoriaSeleccionada == categoria,
                            onClick = { categoriaSeleccionada = categoria },
                            label = { Text(categoria, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1A3A6B),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cantidadDouble = cantidad.toDoubleOrNull()
                if (descripcion.isNotBlank() && cantidadDouble != null && fechaCobro.isNotBlank()) {
                    onConfirm(descripcion, cantidadDouble, categoriaSeleccionada, fechaCobro)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

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
fun EditarGastoDialog(
    gasto: Gasto,
    onConfirm: (String, Double, String) -> Unit,
    onDismiss: () -> Unit
) {
    var descripcion by remember { mutableStateOf(gasto.descripcion) }
    var cantidad by remember { mutableStateOf(gasto.cantidad.toString()) }
    val categorias = listOf("Alimentación", "Suministros", "Ocio", "Transporte", "Salud", "Otros")
    var categoriaSeleccionada by remember { mutableStateOf(gasto.categoria) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad (€)") },
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
                if (descripcion.isNotBlank() && cantidadDouble != null) {
                    onConfirm(descripcion, cantidadDouble, categoriaSeleccionada)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

package com.example.qtengo.familiar.ui.inventario

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditarInventarioDialog(
    item: InventarioItem,
    onConfirm: (nombre: String, cantidad: Int, ubicacion: String, minStock: Int, notas: String, fechaCaducidad: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(item.nombre) }
    var cantidad by remember { mutableStateOf(item.cantidad.toString()) }
    var minStock by remember { mutableStateOf(item.minStock.toString()) }
    var notas by remember { mutableStateOf(item.notas) }
    var fechaCaducidad by remember { mutableStateOf(item.fechaCaducidad ?: "") }
    var tieneFechaCaducidad by remember { mutableStateOf(item.fechaCaducidad != null) }

    var errorNombre by remember { mutableStateOf("") }
    var errorCantidad by remember { mutableStateOf("") }
    var errorMinStock by remember { mutableStateOf("") }
    var errorFecha by remember { mutableStateOf("") }

    val ubicaciones = listOf("Cocina", "Despensa", "Lavadero", "Trastero", "Baño", "Otros")
    var ubicacionSeleccionada by remember { mutableStateOf(item.ubicacion.ifBlank { "Cocina" }) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).apply { isLenient = false } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar artículo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorNombre = "" },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorNombre.isNotEmpty(),
                    supportingText = {
                        if (errorNombre.isNotEmpty())
                            Text(errorNombre, color = MaterialTheme.colorScheme.error)
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = {
                            cantidad = it; errorCantidad = when {
                            it.toIntOrNull() == null -> "Solo enteros"
                            it.toInt() <= 0 -> "Mayor que 0"
                            else -> ""
                        }
                        },
                        label = { Text("Cantidad") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = errorCantidad.isNotEmpty(),
                        supportingText = {
                            if (errorCantidad.isNotEmpty())
                                Text(errorCantidad, color = MaterialTheme.colorScheme.error)
                        }
                    )
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = {
                            minStock = it; errorMinStock = when {
                            it.toIntOrNull() == null -> "Solo enteros"
                            it.toInt() < 0 -> "No negativo"
                            else -> ""
                        }
                        },
                        label = { Text("Stock Mín.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = errorMinStock.isNotEmpty(),
                        supportingText = {
                            if (errorMinStock.isNotEmpty())
                                Text(errorMinStock, color = MaterialTheme.colorScheme.error)
                        }
                    )
                }
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Fecha de caducidad",
                        fontSize = 13.sp,
                        color = Color(0xFF1A3A6B),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = tieneFechaCaducidad,
                        onCheckedChange = {
                            tieneFechaCaducidad = it
                            if (!it) { fechaCaducidad = ""; errorFecha = "" }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1A3A6B))
                    )
                }
                if (tieneFechaCaducidad) {
                    OutlinedTextField(
                        value = fechaCaducidad,
                        onValueChange = {
                            fechaCaducidad = it
                            errorFecha = if (it.isNotBlank()) {
                                runCatching { sdf.parse(it) }.fold(
                                    onSuccess = { "" },
                                    onFailure = { "Formato inválido — usa dd/MM/yyyy" }
                                )
                            } else ""
                        },
                        label = { Text("Fecha (dd/MM/yyyy)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorFecha.isNotEmpty(),
                        supportingText = {
                            if (errorFecha.isNotEmpty())
                                Text(errorFecha, color = MaterialTheme.colorScheme.error)
                        }
                    )
                }
                Text(
                    text = "Ubicación",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ubicaciones.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { ubicacion ->
                                FilterChip(
                                    selected = ubicacionSeleccionada == ubicacion,
                                    onClick = { ubicacionSeleccionada = ubicacion },
                                    label = { Text(ubicacion, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1A3A6B),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                var valido = true

                if (nombre.isBlank()) { errorNombre = "El nombre no puede estar vacío"; valido = false }

                val cantidadInt = cantidad.toIntOrNull()
                if (cantidadInt == null || cantidadInt <= 0) { errorCantidad = "Introduce una cantidad válida mayor que 0"; valido = false }

                val minStockInt = minStock.toIntOrNull()
                if (minStockInt == null || minStockInt < 0) { errorMinStock = "Introduce un stock mínimo válido"; valido = false }

                if (tieneFechaCaducidad && fechaCaducidad.isNotBlank()) {
                    if (runCatching { sdf.parse(fechaCaducidad) }.isFailure) {
                        errorFecha = "Formato inválido — usa dd/MM/yyyy"; valido = false
                    }
                }

                if (valido && cantidadInt != null && minStockInt != null) {
                    onConfirm(
                        nombre.trim(),
                        cantidadInt,
                        ubicacionSeleccionada,
                        minStockInt,
                        notas.trim(),
                        fechaCaducidad.ifBlank { null }
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
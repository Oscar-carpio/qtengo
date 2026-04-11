package com.example.qtengo.restauracion.ui.carta

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditarPlatoDialog(plato: Plato, onConfirm: (Plato) -> Unit, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf(plato.nombre) }
    var descripcion by remember { mutableStateOf(plato.descripcion) }
    var precio by remember { mutableStateOf("%.2f".format(plato.precio)) }
    var categoriaSeleccionada by remember { mutableStateOf(plato.categoria) }
    var errorNombre by remember { mutableStateOf("") }
    var errorPrecio by remember { mutableStateOf("") }
    val categorias = listOf("Entrantes", "Principales", "Postres", "Bebidas")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar plato") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorNombre = "" },
                    label = { Text("Nombre del plato") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorNombre.isNotEmpty(),
                    supportingText = {
                        if (errorNombre.isNotEmpty())
                            Text(errorNombre, color = MaterialTheme.colorScheme.error)
                    }
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it; errorPrecio = "" },
                    label = { Text("Precio (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorPrecio.isNotEmpty(),
                    supportingText = {
                        if (errorPrecio.isNotEmpty())
                            Text(errorPrecio, color = MaterialTheme.colorScheme.error)
                    }
                )
                Text("Categoría", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                categorias.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { cat ->
                            FilterChip(
                                selected = categoriaSeleccionada == cat,
                                onClick = { categoriaSeleccionada = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1A3A6B),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                var valido = true
                if (nombre.isBlank()) { errorNombre = "El nombre es obligatorio"; valido = false }
                val precioDouble = precio.replace(",", ".").toDoubleOrNull()
                if (precioDouble == null || precioDouble < 0) { errorPrecio = "Introduce un precio válido"; valido = false }
                if (valido && precioDouble != null) {
                    onConfirm(Plato(
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim(),
                        precio = precioDouble,
                        categoria = categoriaSeleccionada,
                        disponible = plato.disponible
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

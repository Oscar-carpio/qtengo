package com.example.qtengo.familiar.ui.compra

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun EditarItemDialog(
    item: ShoppingItem,
    onConfirm: (String, String, String) -> Unit, // nombre, cantidad, precio
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(item.name) }
    var cantidad by remember { mutableStateOf(item.quantity) }
    var precio by remember { mutableStateOf(item.price) }
    var errorCantidad by remember { mutableStateOf(false) }
    var errorPrecio by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true
                )
                // Cantidad
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        cantidad = it
                        errorCantidad = it.toIntOrNull()?.let { v -> v < 0 } ?: false
                    },
                    label = { Text("Cantidad (unidades)") },
                    singleLine = true,
                    isError = errorCantidad,
                    supportingText = {
                        if (errorCantidad) Text("La cantidad no puede ser negativa", color = Color.Red)
                    }
                )
                // Precio
                OutlinedTextField(
                    value = precio,
                    onValueChange = {
                        precio = it
                        errorPrecio = it.toDoubleOrNull()?.let { v -> v < 0 } ?: false
                    },
                    label = { Text("Precio (€)") },
                    singleLine = true,
                    isError = errorPrecio,
                    supportingText = {
                        if (errorPrecio) Text("El precio no puede ser negativo", color = Color.Red)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cantidadValida = cantidad.toIntOrNull()?.let { it >= 0 } ?: true
                val precioValido = precio.toDoubleOrNull()?.let { it >= 0 } ?: true
                if (nombre.isNotBlank() && cantidadValida && precioValido) {
                    onConfirm(nombre, cantidad, precio)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancelar") }
        }
    )
}
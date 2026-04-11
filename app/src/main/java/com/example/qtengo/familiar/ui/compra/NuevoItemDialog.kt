package com.example.qtengo.familiar.ui.compra

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun NuevoItemDialog(
    onConfirm: (nombre: String, cantidad: String, precio: Double) -> Unit,  // FIX WARN — precio es Double
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var precioTexto by remember { mutableStateOf("") }  // texto del campo
    var errorCantidad by remember { mutableStateOf(false) }
    var errorPrecio by remember { mutableStateOf(false) }

    fun resetear() {
        nombre = ""; cantidad = ""; precioTexto = ""
        errorCantidad = false; errorPrecio = false
    }

    AlertDialog(
        onDismissRequest = { onDismiss(); resetear() },
        title = { Text("Añadir producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true
                )
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
                OutlinedTextField(
                    value = precioTexto,
                    onValueChange = { input ->
                        // FIX WARN — normalizamos coma europea a punto antes de validar
                        precioTexto = input
                        val normalizado = input.replace(",", ".")
                        errorPrecio = normalizado.toDoubleOrNull()?.let { it < 0 } ?: (input.isNotBlank())
                    },
                    label = { Text("Precio (€)") },
                    singleLine = true,
                    isError = errorPrecio,
                    supportingText = {
                        if (errorPrecio) Text("Introduce un precio válido y no negativo", color = Color.Red)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cantidadValida = cantidad.isBlank() || (cantidad.toIntOrNull()?.let { it >= 0 } ?: false)
                // FIX WARN — parseamos con normalización de coma europea
                val precioDouble = precioTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                val precioValido = precioTexto.isBlank() || precioDouble >= 0.0

                if (nombre.isNotBlank() && cantidadValida && precioValido) {
                    onConfirm(nombre.trim(), cantidad, precioDouble)
                    resetear()
                }
            }) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss(); resetear() }) { Text("Cancelar") }
        }
    )
}

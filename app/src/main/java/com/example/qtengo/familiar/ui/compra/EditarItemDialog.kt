package com.example.qtengo.familiar.ui.compra

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun EditarItemDialog(
    item: ShoppingItem,
    onConfirm: (nombre: String, cantidad: String, precio: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf(item.name) }
    var cantidad by remember { mutableStateOf(item.quantity) }
    var precioTexto by remember {
        mutableStateOf(if (item.price > 0.0) "%.2f".format(item.price) else "")
    }
    var errorNombre by remember { mutableStateOf(false) }
    var errorCantidad by remember { mutableStateOf(false) }
    var errorPrecio by remember { mutableStateOf(false) }

    // Extrae el número al inicio de un texto como "2 kg", "500 ml", "3"
    fun extraerNumero(texto: String): Double? {
        val numStr = texto.trim().split(" ", "kg", "Kg", "KG", "g", "G", "mg", "MG",
            "ml", "ML", "l", "L", "cl", "CL", "litros", "Litros",
            "cm", "CM", "m", "M", "mm", "MM").first().trim()
        return numStr.replace(",", ".").toDoubleOrNull()
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        if (errorNombre) errorNombre = it.isBlank()
                    },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    isError = errorNombre,
                    supportingText = {
                        if (errorNombre) Text("El nombre no puede estar vacío", color = Color.Red)
                    }
                )

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        cantidad = it
                        if (it.isNotBlank()) {
                            val num = extraerNumero(it)
                            errorCantidad = num == null || num < 0
                        } else {
                            errorCantidad = false
                        }
                    },
                    label = { Text("Cantidad (ej: 2, 500 g, 1 kg)") },
                    singleLine = true,
                    isError = errorCantidad,
                    supportingText = {
                        if (errorCantidad) Text("Introduce una cantidad válida (ej: 2, 1 kg, 500 g)", color = Color.Red)
                    }
                )

                OutlinedTextField(
                    value = precioTexto,
                    onValueChange = { input ->
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
                errorNombre = nombre.isBlank()

                val cantidadOk = if (cantidad.isBlank()) true else {
                    val num = extraerNumero(cantidad)
                    num != null && num >= 0
                }
                errorCantidad = !cantidadOk

                val precioDouble = precioTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                val precioValido = precioTexto.isBlank() || precioDouble >= 0.0
                errorPrecio = !precioValido && precioTexto.isNotBlank()

                if (!errorNombre && !errorCantidad && !errorPrecio) {
                    onConfirm(nombre.trim(), cantidad.trim(), precioDouble)
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancelar") }
        }
    )
}
package com.example.qtengo.ui.familiar.compra

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

@Composable
fun NuevoItemDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            nombre = ""
            cantidad = ""
        },
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
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad (ej: 2 litros)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isNotBlank()) {
                    onConfirm(nombre, cantidad)
                    nombre = ""
                    cantidad = ""
                }
            }) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                nombre = ""
                cantidad = ""
            }) { Text("Cancelar") }
        }
    )
}
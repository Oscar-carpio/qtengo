package com.example.qtengo.familiar.ui.compra

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun NuevaListaDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
            nombre = ""
        },
        title = { Text("Nueva lista") },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la lista") },
                singleLine = true
            )
        },
        //Botón de confirmación.
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isNotBlank()) {
                    onConfirm(nombre)
                    nombre = ""
                }
            }) { Text("Crear") }
        },
        //Botón de cancelar
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                nombre = ""
            }) { Text("Cancelar") }
        }
    )
}
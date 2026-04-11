package com.example.qtengo.restauracion.ui.carta

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MenuDiaDialog(menuActual: MenuDia?, onConfirm: (MenuDia) -> Unit, onDismiss: () -> Unit) {
    var primerPlato by remember { mutableStateOf(menuActual?.primerPlato ?: "") }
    var segundoPlato by remember { mutableStateOf(menuActual?.segundoPlato ?: "") }
    var postre by remember { mutableStateOf(menuActual?.postre ?: "") }
    var precio by remember {
        mutableStateOf(
            if ((menuActual?.precio ?: 0.0) > 0) "%.2f".format(menuActual!!.precio) else ""
        )
    }
    val fecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Menú del día — $fecha") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = primerPlato,
                    onValueChange = { primerPlato = it },
                    label = { Text("1er plato") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = segundoPlato,
                    onValueChange = { segundoPlato = it },
                    label = { Text("2º plato") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = postre,
                    onValueChange = { postre = it },
                    label = { Text("Postre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val precioDouble = precio.replace(",", ".").toDoubleOrNull() ?: 0.0
                onConfirm(MenuDia(
                    primerPlato = primerPlato,
                    segundoPlato = segundoPlato,
                    postre = postre,
                    precio = precioDouble,
                    fecha = fecha
                ))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

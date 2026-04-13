package com.example.qtengo.pyme.ui.finanzas.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qtengo.core.domain.models.FinanceMovement

@Composable
fun DialogoFinance(
    tipo: String,
    movement: FinanceMovement? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double) -> Unit
) {
    var concept by remember { mutableStateOf(movement?.concept ?: "") }
    var details by remember { mutableStateOf(movement?.notes ?: "") }
    var amount by remember { mutableStateOf(movement?.amount?.toString() ?: "") }

    var conceptError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { }, // Evita el cierre al pulsar fuera
        title = { Text(if (movement == null) "Añadir $tipo" else "Editar $tipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { 
                        concept = it
                        if (it.isNotBlank()) conceptError = null
                    },
                    label = { Text("Concepto") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = conceptError != null,
                    supportingText = {
                        conceptError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
                    }
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Detalles (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        if (it.toDoubleOrNull() != null) amountError = null
                    },
                    label = { Text("Cantidad (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountError != null,
                    supportingText = {
                        amountError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    
                    if (concept.isBlank()) {
                        conceptError = "El concepto no puede estar vacío"
                        hasError = true
                    }
                    
                    val amountDouble = amount.toDoubleOrNull()
                    if (amountDouble == null || amountDouble <= 0) {
                        amountError = "Introduce una cantidad válida mayor que 0"
                        hasError = true
                    }

                    if (!hasError) {
                        onConfirm(concept, details, amountDouble!!)
                    }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

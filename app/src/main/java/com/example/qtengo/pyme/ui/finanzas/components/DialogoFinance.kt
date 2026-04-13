/**
 * Diálogo interactivo para la gestión de movimientos financieros.
 * 
 * Se utiliza tanto para el registro de nuevos ingresos/gastos como para la edición
 * de registros existentes. Incluye validaciones básicas para el importe numérico.
 */
package com.example.qtengo.pyme.ui.finanzas.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.qtengo.core.domain.models.FinanceMovement

/**
 * Composable que muestra un AlertDialog con campos para concepto, detalles e importe.
 * 
 * @param tipo Indica si es "INGRESO" o "GASTO" para personalizar los textos.
 * @param movement Movimiento opcional a editar. Si es null, el diálogo actúa como creación.
 * @param onDismiss Callback para cerrar el diálogo sin guardar.
 * @param onConfirm Callback que devuelve los datos validados (concepto, notas, importe).
 */
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (movement == null) "Añadir $tipo" else "Editar $tipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text("Concepto / Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Detalles / Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        // Lógica de filtrado para permitir solo números y un único punto decimal
                        if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                            if (input.count { it == '.' } <= 1) {
                                amount = input
                            }
                        }
                    },
                    label = { Text("Importe") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val amountValue = amount.toDoubleOrNull()
                if (concept.isNotEmpty() && amountValue != null && amountValue > 0) {
                    onConfirm(concept, details, amountValue)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

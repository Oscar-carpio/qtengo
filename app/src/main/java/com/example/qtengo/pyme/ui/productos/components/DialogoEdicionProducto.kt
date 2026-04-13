package com.example.qtengo.pyme.ui.productos.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.qtengo.core.domain.models.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoEdicionProducto(product: Product? = null, onDismiss: () -> Unit, onConfirm: (Product) -> Unit) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var quantity by remember { mutableStateOf(product?.quantity?.toInt()?.toString() ?: "") }
    var minStock by remember { mutableStateOf(product?.minStock?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(product?.category ?: "") }
    val unitOptions = listOf("Uds", "kg", "litros", "barriles", "paquetes", "cajas")
    var selectedUnit by remember { mutableStateOf(product?.unit ?: unitOptions[0]) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Nuevo Producto" else "Editar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del item") }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidad de medida") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        unitOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) }, 
                                onClick = { selectedUnit = option; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { input -> if (input.all { it.isDigit() }) quantity = input },
                    label = { Text("Stock Actual") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = minStock,
                    onValueChange = { input -> if (input.all { it.isDigit() }) minStock = input },
                    label = { Text("Aviso de Stock Mínimo") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                val q = quantity.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank() && q >= 0) {
                    onConfirm(Product(
                        id = product?.id ?: "",
                        name = name,
                        quantity = q,
                        minStock = minStock.toDoubleOrNull() ?: 0.0,
                        category = category,
                        unit = selectedUnit,
                        profile = product?.profile ?: ""
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

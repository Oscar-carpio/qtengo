/**
 * Componente visual para representar un producto individual en la lista de inventario.
 * 
 * Muestra el nombre, categoría, stock actual y mínimo. Proporciona controles rápidos
 * para incrementar o decrementar la cantidad y acceso a la edición/borrado.
 */
package com.example.qtengo.pyme.ui.productos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qtengo.core.domain.models.Product

/**
 * Composable que renderiza la tarjeta informativa de un producto.
 * 
 * @param product Datos del producto a mostrar.
 * @param onUpdateQuantity Callback para cambiar el stock (relativo o absoluto).
 * @param onDelete Callback para eliminar el producto.
 * @param onEdit Callback para abrir el formulario de edición.
 */
@Composable
fun ProductoItemCard(product: Product, onUpdateQuantity: (Double) -> Unit, onDelete: () -> Unit, onEdit: (Product) -> Unit) {
    val isLowStock = product.quantity <= product.minStock
    var showQuickQuantityDialog by remember { mutableStateOf(false) }

    // Selector de icono basado en la unidad de medida
    val icon = when(product.unit.lowercase()) {
        "uds" -> "🏷️"
        "kg" -> "⚖️"
        "litros" -> "🥤"
        "barriles" -> "🛢️"
        "paquetes" -> "🛍️"
        "cajas" -> "📦"
        else -> "🎁"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isLowStock) Color(0xFFFFFBFA) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicador visual de estado/tipo
                Box(modifier = Modifier.size(40.dp).background(if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFF1F8E9), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text(text = if (isLowStock) "⚠️" else icon, fontSize = 18.sp)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Información descriptiva
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    if (product.category.isNotBlank()) {
                        Text(
                            text = product.category, 
                            fontSize = 11.sp, 
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Botones de acción (Editar/Borrar)
                Row {
                    IconButton(onClick = { onEdit(product) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(18.dp)) }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sección de control de stock e Info (ID + MIN)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Bloque ID y Mínimo en la misma línea que las cantidades
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (product.customId.isNotBlank()) {
                        Surface(color = Color(0xFFF4F7FB), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = "ID: ${product.customId}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A3A6B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                    Surface(color = Color(0xFFF4F7FB), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = "Min: ${product.minStock.toInt()}", 
                            fontSize = 10.sp, 
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Controles de cantidad
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Decrementar stock
                    IconButton(onClick = { if (product.quantity > 0) onUpdateQuantity(product.quantity - 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.RemoveCircleOutline, null, tint = Color(0xFF1A3A6B))
                    }
                    
                    // Visualización de cantidad actual (Clic para ajuste manual)
                    Surface(
                        modifier = Modifier.clickable { showQuickQuantityDialog = true },
                        color = if (isLowStock) Color(0xFFD32F2F) else Color(0xFF1A3A6B),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${product.quantity.toInt()} ${product.unit}", 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold, 
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    // Incrementar stock
                    IconButton(onClick = { onUpdateQuantity(product.quantity + 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF1A3A6B))
                    }
                }
            }
        }
    }

    // Diálogo de ajuste manual rápido
    if (showQuickQuantityDialog) {
        var inputVal by remember { mutableStateOf(product.quantity.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { showQuickQuantityDialog = false },
            title = { Text("Ajustar Cantidad") },
            text = {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { input -> if (input.all { it.isDigit() }) inputVal = input },
                    label = { Text("Nueva cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { 
                    onUpdateQuantity(inputVal.toDoubleOrNull() ?: product.quantity)
                    showQuickQuantityDialog = false
                }) { Text("Actualizar") }
            }
        )
    }
}

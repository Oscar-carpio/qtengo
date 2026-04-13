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

@Composable
fun ProductoItemCard(product: Product, onUpdateQuantity: (Double) -> Unit, onDelete: () -> Unit, onEdit: (Product) -> Unit) {
    val isLowStock = product.quantity <= product.minStock
    var showQuickQuantityDialog by remember { mutableStateOf(false) }

    val icon = when(product.unit.lowercase()) {
        "uds" -> "🏷️"
        "kg" -> "秤"
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
                Box(modifier = Modifier.size(40.dp).background(if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFF1F8E9), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text(text = if (isLowStock) "⚠️" else icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    if (product.category.isNotBlank()) {
                        Text(
                            text = product.category, 
                            fontSize = 12.sp, 
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row {
                    IconButton(onClick = { onEdit(product) }) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFF4F7FB), shape = RoundedCornerShape(8.dp)) {
                    Text(text = "Mín: ${product.minStock.toInt()}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = Color.Gray)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { if (product.quantity > 0) onUpdateQuantity(product.quantity - 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.RemoveCircleOutline, null, tint = Color(0xFF1A3A6B))
                    }
                    
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

                    IconButton(onClick = { onUpdateQuantity(product.quantity + 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF1A3A6B))
                    }
                }
            }
        }
    }

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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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

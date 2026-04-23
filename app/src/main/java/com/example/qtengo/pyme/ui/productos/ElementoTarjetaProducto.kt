/**
 * Componente visual para representar un producto individual en la lista de inventario.
 */
package com.example.qtengo.pyme.ui.productos

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
 */
@Composable
fun ElementoTarjetaProducto(
    producto: Product, 
    onActualizarCantidad: (Double) -> Unit, 
    onEliminar: () -> Unit, 
    onEditar: (Product) -> Unit
) {
    val stockBajo = producto.quantity <= producto.minStock
    var mostrarDialogoAjusteRapido by remember { mutableStateOf(false) }

    val icono = when(producto.unit.lowercase()) {
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
        colors = CardDefaults.cardColors(containerColor = if (stockBajo) Color(0xFFFFFBFA) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(if (stockBajo) Color(0xFFFFEBEE) else Color(0xFFF1F8E9), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Text(text = if (stockBajo) "⚠️" else icono, fontSize = 18.sp)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = producto.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    if (producto.category.isNotBlank()) {
                        Text(
                            text = producto.category, 
                            fontSize = 11.sp, 
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = { onEditar(producto) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }
                    IconButton(onClick = onEliminar, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(18.dp)) }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (producto.customId.isNotBlank()) {
                        Surface(color = Color(0xFFF4F7FB), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = "ID: ${producto.customId}",
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
                            text = "Min: ${producto.minStock.toInt()}", 
                            fontSize = 10.sp, 
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { if (producto.quantity > 0) onActualizarCantidad(producto.quantity - 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.RemoveCircleOutline, null, tint = Color(0xFF1A3A6B))
                    }
                    
                    Surface(
                        modifier = Modifier.clickable { mostrarDialogoAjusteRapido = true },
                        color = if (stockBajo) Color(0xFFD32F2F) else Color(0xFF1A3A6B),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${producto.quantity.toInt()} ${producto.unit}", 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold, 
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(onClick = { onActualizarCantidad(producto.quantity + 1) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.AddCircleOutline, null, tint = Color(0xFF1A3A6B))
                    }
                }
            }
        }
    }

    if (mostrarDialogoAjusteRapido) {
        var valorEntrada by remember { mutableStateOf(producto.quantity.toInt().toString()) }
        AlertDialog(
            onDismissRequest = { mostrarDialogoAjusteRapido = false },
            title = { Text("Ajustar Cantidad") },
            text = {
                OutlinedTextField(
                    value = valorEntrada,
                    onValueChange = { if (it.all { char -> char.isDigit() }) valorEntrada = it },
                    label = { Text("Nueva cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = { 
                    onActualizarCantidad(valorEntrada.toDoubleOrNull() ?: producto.quantity)
                    mostrarDialogoAjusteRapido = false
                }) { Text("Actualizar") }
            }
        )
    }
}

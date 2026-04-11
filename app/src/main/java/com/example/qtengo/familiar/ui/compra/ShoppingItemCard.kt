package com.example.qtengo.familiar.ui.compra

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    esFavorito: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: (nombre: String, cantidad: String, precio: Double) -> Unit,  // FIX WARN — precio Double
    onFavorito: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditarItemDialog(
            item = item,
            onConfirm = { nombre, cantidad, precio ->
                onEdit(nombre, cantidad, precio)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) Color(0xFFE3F2FD) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1A3A6B))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = if (item.isChecked) Color.Gray else Color(0xFF1A3A6B),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.isChecked) Color.Gray else Color(0xFF1A3A6B),
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(text = "Cantidad: ${item.quantity}", fontSize = 12.sp, color = Color.Gray)
                // FIX WARN — formateamos Double a "X.XX €", solo si tiene precio
                if (item.price > 0.0) {
                    Text(
                        text = "Precio: ${"%.2f".format(item.price)} €",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onFavorito) {
                Icon(
                    imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (esFavorito) "Quitar de favoritos" else "Guardar como favorito",
                    tint = if (esFavorito) Color(0xFFE53935) else Color.Gray
                )
            }
            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar producto",
                    tint = Color(0xFF1A3A6B)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar producto",
                    tint = Color(0xFF1A3A6B)
                )
            }
        }
    }
}

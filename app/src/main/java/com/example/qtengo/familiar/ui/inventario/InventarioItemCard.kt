package com.example.qtengo.familiar.ui.inventario

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InventarioItemCard(
    item: InventarioItem,
    onDelete: () -> Unit,
    onEdit: (InventarioItem) -> Unit
) {
    val context = LocalContext.current
    val bajoDeMínimos = item.cantidad <= item.minStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bajoDeMínimos) Color(0xFFFFF3E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (bajoDeMínimos) Color(0xFFFFE0B2) else Color(0xFFE3F2FD),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📦", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3A6B)
                )
                Text(
                    text = "${item.ubicacion}${if (item.fechaCaducidad != null) " · Cad: ${item.fechaCaducidad}" else ""}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (item.notas.isNotBlank()) {
                    Text(text = item.notas, fontSize = 11.sp, color = Color.Gray)
                }
                if (bajoDeMínimos) {
                    Text(
                        text = "⚠️ Stock bajo (mín. ${item.minStock})",
                        fontSize = 11.sp,
                        color = Color(0xFFF57C00),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .background(
                        if (bajoDeMínimos) Color(0xFFFFE0B2) else Color(0xFFE3F2FD),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${item.cantidad} ud.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (bajoDeMínimos) Color(0xFFF57C00) else Color(0xFF1A3A6B)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))

            // Botón Amazon — abre búsqueda del artículo en el navegador
            IconButton(onClick = {
                val query = Uri.encode(item.nombre)
                val url = "https://www.amazon.es/s?k=$query"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Buscar en Amazon",
                    tint = Color(0xFFFF9900)
                )
            }

            IconButton(onClick = { onEdit(item) }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar artículo",
                    tint = Color(0xFF1A3A6B)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar artículo",
                    tint = Color(0xFF1A3A6B)
                )
            }
        }
    }
}
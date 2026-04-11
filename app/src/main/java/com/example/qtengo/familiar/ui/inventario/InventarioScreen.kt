package com.example.qtengo.familiar.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun InventarioScreen(
    onAddItem: () -> Unit,
    onBack: () -> Unit,
    viewModel: InventarioViewModel = viewModel()
) {
    val items by viewModel.items.collectAsState()
    var itemAEditar by remember { mutableStateOf<InventarioItem?>(null) }

    // INFO — contamos artículos bajo mínimos para el header
    val bajoDeMínimos = items.count { it.cantidad <= it.minStock }

    LaunchedEffect(Unit) {
        viewModel.cargarItems()
    }

    // Diálogo de edición
    itemAEditar?.let { item ->
        EditarInventarioDialog(
            item = item,
            onConfirm = { nombre, cantidad, ubicacion, minStock, notas, fecha ->
                viewModel.editarItem(item.id, nombre, cantidad, ubicacion, minStock, notas, fecha)
                itemAEditar = null
            },
            onDismiss = { itemAEditar = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A3A6B))
                .padding(24.dp)
        ) {
            IconButton(
                onClick = { onBack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(text = "←", fontSize = 24.sp, color = Color.White)
            }
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(
                    text = "Inventario del hogar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // INFO — aviso de artículos bajo mínimos en el header
                if (bajoDeMínimos > 0) {
                    Text(
                        text = "⚠️ $bajoDeMínimos artículo${if (bajoDeMínimos > 1) "s" else ""} bajo mínimos",
                        fontSize = 12.sp,
                        color = Color(0xFFFFCC80)
                    )
                }
            }
        }

        InventarioResumenCard(
            totalArticulos = items.size,
            totalUnidades = items.sumOf { it.cantidad },
            totalConFecha = items.count { it.fechaCaducidad != null }
        )

        Text(
            text = "Artículos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A3A6B),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay artículos. ¡Añade uno!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(items) { item ->
                    InventarioItemCard(
                        item = item,
                        onDelete = { viewModel.eliminarItem(item.id) },
                        onEdit = { itemAEditar = it }
                    )
                }
            }
        }

        Button(
            onClick = { onAddItem() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Text(text = "+ Añadir artículo", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }
}
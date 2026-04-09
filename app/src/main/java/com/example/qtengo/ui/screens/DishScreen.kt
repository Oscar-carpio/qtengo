package com.example.qtengo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DishScreen(
    profile: String = "HOSTELERIA",
    onBack: () -> Unit
) {
    // Aquí se conectaría con un DishViewModel (pendiente de crear o integrar)
    // Por ahora simulamos la interfaz para cumplir el objetivo de diseño
    
    val dishes = remember { mutableStateListOf<Dish>() }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF1A3A6B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir plato")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F7FB))
        ) {
            // Header
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
                Text(
                    text = "Carta y Escandallos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Resumen de Beneficios
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Rentabilidad Media", color = Color.Gray, fontSize = 12.sp)
                        Text("65%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                    }
                    Divider(modifier = Modifier.height(40.dp).width(1.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Platos", color = Color.Gray, fontSize = 12.sp)
                        Text("${dishes.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Lista de Platos",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dishes) { dish ->
                    DishItem(dish)
                }
            }
        }
    }

    if (showAddDialog) {
        AddDishDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, cost, sell ->
                dishes.add(Dish(name = name, category = category, costPrice = cost, sellingPrice = sell))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DishItem(dish: Dish) {
    val profit = dish.sellingPrice - dish.costPrice
    val margin = if (dish.sellingPrice > 0) (profit / dish.sellingPrice * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = dish.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = dish.category, color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${dish.sellingPrice} €", fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                Text(
                    text = "Margen: $margin%",
                    color = if (margin > 50) Color(0xFF388E3C) else Color(0xFFD32F2F),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AddDishDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Principal") }
    var cost by remember { mutableStateOf("") }
    var sell by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Plato / Escandallo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del plato") })
                OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Coste Ingredientes (€)") })
                OutlinedTextField(value = sell, onValueChange = { sell = it }, label = { Text("Precio Venta (€)") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(name, category, cost.toDoubleOrNull() ?: 0.0, sell.toDoubleOrNull() ?: 0.0) 
            }) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

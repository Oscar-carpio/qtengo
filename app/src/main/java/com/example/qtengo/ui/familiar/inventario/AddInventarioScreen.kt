package com.example.qtengo.ui.familiar.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun AddInventarioScreen(
    onItemGuardado: () -> Unit,
    onBack: () -> Unit,
    viewModel: InventarioViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("1") }
    var notas by remember { mutableStateOf("") }
    var fechaCaducidad by remember { mutableStateOf("") }
    var tieneFechaCaducidad by remember { mutableStateOf(false) }

    val ubicaciones = listOf("Cocina", "Despensa", "Lavadero", "Trastero", "Baño", "Otros")
    var ubicacionSeleccionada by remember { mutableStateOf("Cocina") }

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
            Text(
                text = "Añadir artículo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del artículo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = minStock,
                    onValueChange = { minStock = it },
                    label = { Text("Stock Mín.") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Switch fecha de caducidad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tiene fecha de caducidad",
                    fontSize = 14.sp,
                    color = Color(0xFF1A3A6B),
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = tieneFechaCaducidad,
                    onCheckedChange = {
                        tieneFechaCaducidad = it
                        if (!it) fechaCaducidad = ""
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1A3A6B))
                )
            }

            if (tieneFechaCaducidad) {
                OutlinedTextField(
                    value = fechaCaducidad,
                    onValueChange = { fechaCaducidad = it },
                    label = { Text("Fecha caducidad (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text(
                text = "Ubicación / Categoría",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3A6B)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ubicaciones.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { ubicacion ->
                            FilterChip(
                                selected = ubicacionSeleccionada == ubicacion,
                                onClick = { ubicacionSeleccionada = ubicacion },
                                label = { Text(ubicacion, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1A3A6B),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (nombre.isNotBlank() && cantidad.isNotBlank()) {
                        viewModel.añadirItem(
                            nombre = nombre,
                            cantidad = cantidad.toIntOrNull() ?: 0,
                            ubicacion = ubicacionSeleccionada,
                            minStock = minStock.toIntOrNull() ?: 1,
                            notas = notas,
                            fechaCaducidad = fechaCaducidad.ifBlank { null }
                        )
                        onItemGuardado()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
            ) {
                Text(text = "Guardar artículo", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
package com.example.qtengo.ui.screens

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
fun AddGastoScreen(
    profile: String = "FAMILIA",
    viewModel: ExpenseViewModel = viewModel(),
    onGastoGuardado: () -> Unit,
    onBack: () -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf("GASTO") }
    
    val categoriasFamilia = listOf("Alimentación", "Suministros", "Ocio", "Transporte", "Salud", "Otros")
    val categoriasPyme = listOf("Ventas", "Suministros", "Nóminas", "Alquiler", "Impuestos", "Otros")
    
    val categorias = if (profile == "PYME") categoriasPyme else categoriasFamilia
    var categoriaSeleccionada by remember { mutableStateOf(categorias[0]) }

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                text = if (profile == "PYME") "Añadir movimiento" else "Añadir gasto",
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
            if (profile == "PYME") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { tipoSeleccionado = "GASTO" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tipoSeleccionado == "GASTO") Color(0xFFD32F2F) else Color.LightGray
                        )
                    ) {
                        Text("Gasto")
                    }
                    Button(
                        onClick = { tipoSeleccionado = "INGRESO" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tipoSeleccionado == "INGRESO") Color(0xFF388E3C) else Color.LightGray
                        )
                    ) {
                        Text("Ingreso")
                    }
                }
            }

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad (€)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = "Categoría",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3A6B)
            )

            // Chips de categoría
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categorias.forEach { categoria ->
                    FilterChip(
                        selected = categoriaSeleccionada == categoria,
                        onClick = { categoriaSeleccionada = categoria },
                        label = { Text(categoria, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1A3A6B),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amountValue = cantidad.toDoubleOrNull()
                    if (descripcion.isNotBlank() && amountValue != null) {
                        viewModel.insert(
                            name = descripcion,
                            details = "",
                            amount = amountValue,
                            category = categoriaSeleccionada,
                            type = tipoSeleccionado
                        )
                        onGastoGuardado()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
            ) {
                Text(
                    text = if (profile == "PYME") "Guardar movimiento" else "Guardar gasto",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}

package com.example.qtengo.ui.screens

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

data class Gasto(val id: Int, val descripcion: String, val cantidad: Double, val categoria: String, val fecha: String, val profile: String = "FAMILIA")

@Composable
fun GastosScreen(profile: String = "FAMILIA", onAddGasto: () -> Unit, onBack: () -> Unit) {

    // Simulación de datos filtrados por perfil
    var gastos by remember {
        mutableStateOf(
            listOf(
                Gasto(1, "Compra semanal", 85.50, "Alimentación", "10/03/2026", "FAMILIA"),
                Gasto(2, "Proveedor Fruta", 120.00, "Suministros", "08/03/2026", "HOSTELERIA"),
                Gasto(3, "Papelería", 15.99, "Ocio", "05/03/2026", "PYME"),
            ).filter { it.profile == profile }
        )
    }

    val totalMes = gastos.sumOf { it.cantidad }

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
                text = "Gastos - $profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Tarjeta resumen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total este mes ($profile)",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "%.2f €".format(totalMes),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(gastos) { gasto ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💰", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = gasto.descripcion, fontWeight = FontWeight.Bold)
                            Text(text = "${gasto.categoria} · ${gasto.fecha}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(text = "-%.2f €".format(gasto.cantidad), color = Color(0xFFD32F2F))
                    }
                }
            }
        }

        Button(
            onClick = { onAddGasto() },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Text(text = "+ Añadir gasto", modifier = Modifier.padding(8.dp))
        }
    }
}

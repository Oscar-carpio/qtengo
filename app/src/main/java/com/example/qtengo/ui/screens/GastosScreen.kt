package com.example.qtengo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.qtengo.data.model.Expense

@Composable
fun GastosScreen(
    profile: String = "FAMILIA",
    viewModel: ExpenseViewModel = viewModel(),
    onAddGasto: () -> Unit,
    onBack: () -> Unit
) {
    val expenses by viewModel.expenses.observeAsState(emptyList())
    val totalExpenses by viewModel.totalExpenses.observeAsState(0.0)
    val totalIncomes by viewModel.totalIncomes.observeAsState(0.0)

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
    }

    val balance = (totalIncomes ?: 0.0) - (totalExpenses ?: 0.0)

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
                text = "Control de gastos - $profile",
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
            colors = CardDefaults.cardColors(
                containerColor = if (balance >= 0) Color(0xFF1565C0) else Color(0xFFD32F2F)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (profile == "PYME") "Balance total" else "Total gastos este mes",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "%.2f €".format(if (profile == "PYME") balance else totalExpenses),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (profile == "PYME") {
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Text("Ingresos: +%.2f€".format(totalIncomes ?: 0.0), color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Gastos: -%.2f€".format(totalExpenses ?: 0.0), color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                    }
                }
            }
        }

        Text(
            text = "Últimos movimientos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A3A6B),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Lista de gastos
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(expenses) { expense ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (expense.category) {
                                    "Alimentación", "Suministros" -> "🛒"
                                    "Ocio" -> "🎬"
                                    "Transporte" -> "🚗"
                                    "Ventas" -> "📈"
                                    "Nóminas" -> "👥"
                                    else -> "💰"
                                },
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.description,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A3A6B)
                            )
                            Text(
                                text = "${expense.category} · ${expense.date}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = "${if (expense.type == "GASTO") "-" else "+"}%.2f €".format(expense.amount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (expense.type == "GASTO") Color(0xFFD32F2F) else Color(0xFF388E3C)
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onAddGasto() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Text(text = "+ Añadir movimiento", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

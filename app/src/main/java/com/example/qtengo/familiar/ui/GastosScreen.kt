package com.example.qtengo.familiar.ui

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
import com.example.qtengo.core.domain.models.Expense
import com.example.qtengo.core.ui.components.QtengoTopBar

/**
 * Pantalla para la gestión de gastos en el perfil Familiar.
 */
@Composable
fun GastosScreen(
    profile: String = "FAMILIA",
    viewModel: ExpenseViewModel = viewModel(),
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit,
    onBack: () -> Unit
) {
    val expenses by viewModel.expenses.observeAsState(emptyList())
    val totalExpenses by viewModel.totalExpenses.observeAsState(0.0)

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Control de Gastos",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        // Resumen
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total gastos este mes", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                Text("%.2f €".format(totalExpenses ?: 0.0), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(expenses) { expense ->
                ExpenseItemCard(expense)
            }
        }
    }
}

@Composable
fun ExpenseItemCard(expense: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.name, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                Text("${expense.category} · ${expense.date}", fontSize = 12.sp, color = Color.Gray)
            }
            Text("-%.2f €".format(expense.amount), fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
        }
    }
}

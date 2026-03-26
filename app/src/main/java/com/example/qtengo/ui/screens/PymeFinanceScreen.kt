package com.example.qtengo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.data.local.model.FinanceMovement
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PymeFinanceScreen(
    onBack: () -> Unit,
    viewModel: FinanceViewModel = viewModel()
) {
    val movements by viewModel.movements.observeAsState(emptyList())
    val ingresos by viewModel.totalIngresos.observeAsState(0.0)
    val gastos by viewModel.totalGastos.observeAsState(0.0)

    var showDialog by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("GASTO") }

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text("Gastos e Ingresos") },
            navigationIcon = {
                TextButton(onClick = onBack) {
                    Text("← Volver")
                }
            }
        )

        // RESUMEN
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FinanceCard("Ingresos", ingresos ?: 0.0, Color(0xFF2E7D32))
            FinanceCard("Gastos", gastos ?: 0.0, Color(0xFFC62828))
            FinanceCard("Beneficio", (ingresos ?: 0.0) - (gastos ?: 0.0), Color(0xFF1565C0))
        }

        // BOTONES
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                type = "INGRESO"
                showDialog = true
            }) {
                Text("+ Ingreso")
            }

            Button(onClick = {
                type = "GASTO"
                showDialog = true
            }) {
                Text("+ Gasto")
            }
        }

        // LISTA
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(movements) { movement ->
                MovementItem(movement, onDelete = { viewModel.delete(movement) })
            }
        }
    }

    if (showDialog) {
        AddMovementDialog(
            type = type,
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.insert(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun FinanceCard(title: String, amount: Double, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier
            .width(110.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color.White)
            Text("${amount}€", color = Color.White)
        }
    }
}

@Composable
fun MovementItem(movement: FinanceMovement, onDelete: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(movement.concept)
                Text(movement.date)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${movement.amount}€",
                    color = if (movement.type == "INGRESO") Color(0xFF2E7D32) else Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Text("🗑️")
                }
            }
        }
    }
}

@Composable
fun AddMovementDialog(
    type: String,
    onDismiss: () -> Unit,
    onConfirm: (FinanceMovement) -> Unit
) {
    var concept by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir $type") },
        text = {
            Column {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text("Concepto") }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Cantidad") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                onConfirm(
                    FinanceMovement(
                        concept = concept,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        type = type,
                        date = date,
                        profile = "PYME"
                    )
                )
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

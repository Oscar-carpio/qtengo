package com.example.qtengo.pyme.ui.finanzas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.ui.components.QtengoTopBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanzasPantalla(
    viewModel: FinanzasViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val movements by viewModel.movements.observeAsState(emptyList())
    val ingresos by viewModel.totalIngresos.observeAsState(0.0)
    val gastos by viewModel.totalGastos.observeAsState(0.0)

    var searchQuery by remember { mutableStateOf("") }
    var filtersExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var tipoSeleccionado by remember { mutableStateOf("INGRESO") }

    val filteredMovements = movements.filter {
        it.concept.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        QtengoTopBar(
            title = "Gastos e Ingresos",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        // Buscador
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { filtersExpanded = !filtersExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Buscar Movimiento", fontWeight = FontWeight.Bold)
                    }
                    Icon(if (filtersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                }
                AnimatedVisibility(visible = filtersExpanded) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Concepto...") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BalanceCard("Ingresos", ingresos ?: 0.0, Color(0xFF2E7D32))
            BalanceCard("Gastos", gastos ?: 0.0, Color(0xFFC62828))
            BalanceCard("Neto", (ingresos ?: 0.0) - (gastos ?: 0.0), Color(0xFF1565C0))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { tipoSeleccionado = "INGRESO"; showAddDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(Icons.Default.Add, null)
                Text("Ingreso")
            }
            Button(
                onClick = { tipoSeleccionado = "GASTO"; showAddDialog = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) {
                Icon(Icons.Default.Remove, null)
                Text("Gasto")
            }
        }

        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
            items(filteredMovements) { movement ->
                MovementRow(movement, onDelete = { viewModel.delete(movement.id) })
            }
        }
    }

    if (showAddDialog) {
        DialogoAnadirMovimiento(
            tipo = tipoSeleccionado,
            onDismiss = { showAddDialog = false },
            onConfirm = { concept, amount ->
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                viewModel.insert(FinanceMovement(
                    concept = concept,
                    amount = amount,
                    type = tipoSeleccionado,
                    date = date,
                    profile = "PYME"
                ))
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BalanceCard(title: String, amount: Double, color: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = color), modifier = Modifier.width(100.dp).height(70.dp)) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.White, fontSize = 10.sp)
            Text("${amount}€", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun MovementRow(movement: FinanceMovement, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(movement.concept, fontWeight = FontWeight.Bold)
                Text(movement.date, fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${movement.amount}€", color = if (movement.type == "INGRESO") Color(0xFF2E7D32) else Color.Red, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray) }
            }
        }
    }
}

@Composable
fun DialogoAnadirMovimiento(tipo: String, onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var concept by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir $tipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = concept, onValueChange = { concept = it }, label = { Text("Concepto") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Importe") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (concept.isNotEmpty() && amount.isNotEmpty()) {
                    onConfirm(concept, amount.toDoubleOrNull() ?: 0.0)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

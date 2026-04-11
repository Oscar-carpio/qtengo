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
    var movementToEdit by remember { mutableStateOf<FinanceMovement?>(null) }

    val filteredMovements = movements.filter {
        it.concept.contains(searchQuery, ignoreCase = true)
    }

    val neto = (ingresos ?: 0.0) - (gastos ?: 0.0)

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
            BalanceCard("Neto", neto, Color(0xFF1565C0), esNeto = true)
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
                MovementRow(
                    movement, 
                    onDelete = { viewModel.delete(movement.id) },
                    onEdit = { movementToEdit = movement }
                )
            }
        }
    }

    if (showAddDialog) {
        DialogoFinance(
            tipo = tipoSeleccionado,
            onDismiss = { showAddDialog = false },
            onConfirm = { concept, details, amount ->
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                viewModel.insert(FinanceMovement(
                    concept = concept,
                    amount = amount,
                    type = tipoSeleccionado,
                    date = date,
                    profile = "PYME",
                    notes = details
                ))
                showAddDialog = false
            }
        )
    }

    movementToEdit?.let { movement ->
        DialogoFinance(
            tipo = movement.type,
            movement = movement,
            onDismiss = { movementToEdit = null },
            onConfirm = { concept, details, amount ->
                viewModel.insert(movement.copy(concept = concept, amount = amount, notes = details))
                movementToEdit = null
            }
        )
    }
}

@Composable
fun BalanceCard(titulo: String, cantidad: Double, colorFondo: Color, esNeto: Boolean = false) {
    val colorTextoCantidad = if (esNeto && cantidad < 0) Color(0xFFFF5252) else Color.White
    Card(
        colors = CardDefaults.cardColors(containerColor = colorFondo), 
        modifier = Modifier.width(100.dp).height(70.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), 
            verticalArrangement = Arrangement.Center, 
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titulo, color = Color.White, fontSize = 10.sp)
            Text("${cantidad}€", color = colorTextoCantidad, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun MovementRow(movement: FinanceMovement, onDelete: () -> Unit, onEdit: () -> Unit) {
    val isEditable = !movement.id.startsWith("nomina_")
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(movement.concept, fontWeight = FontWeight.Bold)
                    Text(movement.date, fontSize = 12.sp, color = Color.Gray)
                }
                
                Surface(
                    color = if (movement.type == "INGRESO") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = "${if (movement.type == "INGRESO") "+" else "-"}${movement.amount}€",
                            color = if (movement.type == "INGRESO") Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (isEditable) {
                    Row {
                        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) }
                    }
                }
            }
            if (movement.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(text = movement.notes, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DialogoFinance(
    tipo: String, 
    movement: FinanceMovement? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, Double) -> Unit
) {
    var concept by remember { mutableStateOf(movement?.concept ?: "") }
    var details by remember { mutableStateOf(movement?.notes ?: "") }
    var amount by remember { mutableStateOf(movement?.amount?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (movement == null) "Añadir $tipo" else "Editar $tipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text("Concepto / Título") }
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Detalles / Descripción") }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                            if (input.count { it == '.' } <= 1) {
                                amount = input
                            }
                        }
                    },
                    label = { Text("Importe") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                val amountValue = amount.toDoubleOrNull()
                if (concept.isNotEmpty() && amountValue != null && amountValue > 0) {
                    onConfirm(concept, details, amountValue)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

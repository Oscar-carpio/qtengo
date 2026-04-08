package com.example.qtengo.pyme.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.ui.components.QtengoTopBar
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de gestión financiera para Pyme.
 * Muestra el balance de ingresos y gastos de forma visual y permite registrar nuevos movimientos.
 * 
 * @param viewModel Maneja la lógica de carga y persistencia de datos financieros.
 * @param onBack Callback para regresar al panel principal.
 * @param onLogout Callback para cerrar la sesión del usuario.
 * @param onChangeProfile Callback para cambiar el rol/plan actual.
 */
@Composable
fun PymeFinanceScreen(
    viewModel: FinanceViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val movements by viewModel.movements.observeAsState(emptyList())
    val ingresos by viewModel.totalIngresos.observeAsState(0.0)
    val gastos by viewModel.totalGastos.observeAsState(0.0)

    var showDialog by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("GASTO") }

    Column(modifier = Modifier.fillMaxSize()) {
        QtengoTopBar(
            title = "Gastos e Ingresos",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        // Sección de resumen: Ingresos, Gastos y Beneficio neto
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

        // Acciones rápidas para añadir movimientos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { type = "INGRESO"; showDialog = true }) { Text("+ Ingreso") }
            Button(onClick = { type = "GASTO"; showDialog = true }) { Text("+ Gasto") }
        }

        // Historial de movimientos registrados
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(movements) { movement ->
                MovementItem(movement, onDelete = { viewModel.delete(movement) })
            }
        }
    }

    // Diálogo emergente para la creación de nuevos registros
    if (showDialog) {
        AddMovementDialog(
            type = type,
            onDismiss = { showDialog = false },
            onConfirm = { viewModel.insert(it) }
        )
    }
}

/**
 * Componente visual para las tarjetas de resumen financiero.
 */
@Composable
fun FinanceCard(title: String, amount: Double, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.width(110.dp).height(80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color.White, fontSize = 12.sp)
            Text("${amount}€", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Fila individual que representa un movimiento en el listado.
 */
@Composable
fun MovementItem(movement: FinanceMovement, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(movement.concept, fontWeight = FontWeight.Bold)
                Text(movement.date, fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${movement.amount}€",
                    color = if (movement.type == "INGRESO") Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) { Text("🗑️") }
            }
        }
    }
}

/**
 * Diálogo para introducir los datos de un nuevo gasto o ingreso.
 */
@Composable
fun AddMovementDialog(type: String, onDismiss: () -> Unit, onConfirm: (FinanceMovement) -> Unit) {
    var concept by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir $type") },
        text = {
            Column {
                OutlinedTextField(value = concept, onValueChange = { concept = it }, label = { Text("Concepto") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Cantidad") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                onConfirm(FinanceMovement(concept = concept, amount = amount.toDoubleOrNull() ?: 0.0, type = type, date = date, profile = "PYME"))
                onDismiss()
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

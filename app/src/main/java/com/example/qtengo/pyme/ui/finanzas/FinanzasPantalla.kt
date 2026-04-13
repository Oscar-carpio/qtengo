package com.example.qtengo.pyme.ui.finanzas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.pyme.ui.finanzas.components.BalanceCard
import com.example.qtengo.pyme.ui.finanzas.components.DialogoFinance
import com.example.qtengo.pyme.ui.finanzas.components.FiltrosFinanzas
import com.example.qtengo.pyme.ui.finanzas.components.MovementRow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de Gestión Financiera.
 * 
 * Permite visualizar el flujo de caja, registrar nuevos movimientos (ingresos/gastos)
 * y consultar el balance neto de la empresa en el perfil PYME.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanzasPantalla(
    viewModel: FinanzasViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    // Estados observados desde el ViewModel
    val movements by viewModel.movements.observeAsState(emptyList())
    val ingresos by viewModel.totalIngresos.observeAsState(0.0)
    val gastos by viewModel.totalGastos.observeAsState(0.0)

    // Estados locales para la UI
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var tipoSeleccionado by remember { mutableStateOf("INGRESO") }
    var movementToEdit by remember { mutableStateOf<FinanceMovement?>(null) }

    // Filtrado en tiempo real basado en la búsqueda del usuario
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

        // Panel de búsqueda superior
        FiltrosFinanzas(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it }
        )

        // Tarjetas de balance (Ingresos, Gastos y Neto)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BalanceCard("Ingresos", ingresos ?: 0.0, Color(0xFF2E7D32))
            BalanceCard("Gastos", gastos ?: 0.0, Color(0xFFC62828))
            BalanceCard("Neto", neto, Color(0xFF1565C0), esNeto = true)
        }

        // Acciones rápidas para añadir movimientos
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

        // Listado de movimientos registrados
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

    // Lógica de diálogos para inserción y edición
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

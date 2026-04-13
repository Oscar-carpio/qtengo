/**
 * Pantalla de Gestión Financiera corregida con el sistema de ordenación unificado.
 */
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
import com.example.qtengo.pyme.ui.filtros.FiltrosFinanzas
import com.example.qtengo.pyme.ui.finanzas.components.BalanceCard
import com.example.qtengo.pyme.ui.finanzas.components.DialogoFinance
import com.example.qtengo.pyme.ui.finanzas.components.MovementRow
import java.text.SimpleDateFormat
import java.util.*

sealed class FinanceDialogState {
    data class Add(val tipo: String) : FinanceDialogState()
    data class Edit(val movement: FinanceMovement) : FinanceDialogState()
}

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
    var sortBy by remember { mutableStateOf("Nombre") }
    var isAscending by remember { mutableStateOf(true) }
    
    var activeDialog by remember { mutableStateOf<FinanceDialogState?>(null) }

    val filteredAndSortedMovements = remember(movements, searchQuery, sortBy, isAscending) {
        movements
            .filter { it.concept.contains(searchQuery, ignoreCase = true) }
            .sortedWith { m1, m2 ->
                val res = when (sortBy) {
                    "Nombre" -> m1.concept.compareTo(m2.concept, ignoreCase = true)
                    "Cantidad" -> {
                        val v1 = if (m1.type == "GASTO") -m1.amount else m1.amount
                        val v2 = if (m2.type == "GASTO") -m2.amount else m2.amount
                        v1.compareTo(v2)
                    }
                    else -> 0
                }
                if (isAscending) res else -res
            }
    }

    val neto = (ingresos ?: 0.0) - (gastos ?: 0.0)

    Column(modifier = Modifier.fillMaxSize()) {
        QtengoTopBar(
            title = "Gastos e Ingresos",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        FiltrosFinanzas(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            sortBy = sortBy,
            isAscending = isAscending,
            onSortChange = { s, a -> sortBy = s; isAscending = a }
        )

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
                onClick = { activeDialog = FinanceDialogState.Add("INGRESO") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Icon(Icons.Default.Add, null)
                Text("Ingreso")
            }
            Button(
                onClick = { activeDialog = FinanceDialogState.Add("GASTO") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) {
                Icon(Icons.Default.Remove, null)
                Text("Gasto")
            }
        }

        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp).weight(1f)) {
            items(filteredAndSortedMovements) { movement ->
                MovementRow(
                    movement,
                    onDelete = { viewModel.delete(movement.id) },
                    onEdit = { activeDialog = FinanceDialogState.Edit(movement) }
                )
            }
        }
    }

    when (val state = activeDialog) {
        is FinanceDialogState.Add -> {
            DialogoFinance(
                tipo = state.tipo,
                onDismiss = { activeDialog = null },
                onConfirm = { concept, details, amount ->
                    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                    viewModel.insert(FinanceMovement(
                        concept = concept,
                        amount = amount,
                        type = state.tipo,
                        date = date,
                        profile = "PYME",
                        notes = details
                    ))
                    activeDialog = null
                }
            )
        }
        is FinanceDialogState.Edit -> {
            DialogoFinance(
                tipo = state.movement.type,
                movement = state.movement,
                onDismiss = { activeDialog = null },
                onConfirm = { concept, details, amount ->
                    viewModel.insert(state.movement.copy(concept = concept, amount = amount, notes = details))
                    activeDialog = null
                }
            )
        }
        null -> {}
    }
}

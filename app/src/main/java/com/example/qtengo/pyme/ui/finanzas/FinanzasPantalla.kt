package com.example.qtengo.pyme.ui.finanzas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.pyme.ui.DialogoConfirmarEliminar
import com.example.qtengo.pyme.ui.TarjetaEstadisticaPyme
import com.example.qtengo.pyme.ui.filtros.FiltrosFinanzas
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla de Gestión Financiera (PYME).
 * Permite visualizar el flujo de caja, incluyendo ingresos, gastos manuales
 * y nóminas generadas automáticamente.
 */
@Composable
fun FinanzasPantalla(
    viewModel: FinanzasViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val movements by viewModel.movements.observeAsState(emptyList())
    val totalIngresos by viewModel.totalIngresos.observeAsState(0.0)
    val totalGastos by viewModel.totalGastos.observeAsState(0.0)

    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("") }
    var isAscending by remember { mutableStateOf(false) }
    var periodoFilter by remember { mutableStateOf("") }

    var showAddIngreso by remember { mutableStateOf(false) }
    var showAddGasto by remember { mutableStateOf(false) }
    var movementToEdit by remember { mutableStateOf<FinanceMovement?>(null) }
    var movementToDelete by remember { mutableStateOf<FinanceMovement?>(null) }

    val filteredMovements = movements.filter { movement ->
        val matchesSearch = movement.concept.contains(searchQuery, ignoreCase = true)
        val matchesPeriodo = when (periodoFilter) {
            "Diarios" -> movement.date != "Mensual"
            "Mensuales" -> movement.date == "Mensual"
            else -> true
        }
        matchesSearch && matchesPeriodo
    }.let { list ->
        if (sortBy == "") {
            list.sortedByDescending { it.timestamp }
        } else {
            list.sortedWith { m1, m2 ->
                val res = when (sortBy) {
                    "Cantidad" -> m1.amount.compareTo(m2.amount)
                    "Nombre" -> m1.concept.compareTo(m2.concept, ignoreCase = true)
                    else -> 0
                }
                if (res != 0) {
                    if (isAscending) res else -res
                } else {
                    m2.timestamp.compareTo(m1.timestamp)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB))) {
        QtengoTopBar(
            title = "Gastos e ingresos",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        FiltrosFinanzas(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            sortBy = sortBy,
            isAscending = isAscending,
            onSortChange = { s, a -> sortBy = s; isAscending = a },
            periodoFilter = periodoFilter,
            onPeriodoChange = { periodoFilter = it }
        )

        // Resumen Estadístico
        val balance = (totalIngresos ?: 0.0) - (totalGastos ?: 0.0)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TarjetaEstadisticaPyme(
                titulo = "Ingresos",
                valor = String.format("%.2f€", totalIngresos ?: 0.0),
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadisticaPyme(
                titulo = "Gastos",
                valor = String.format("%.2f€", totalGastos ?: 0.0),
                color = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadisticaPyme(
                titulo = "Balance",
                valor = String.format("%.2f€", balance),
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f),
                colorValor = if (balance >= 0) Color.White else Color(0xFFFF5252)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (filteredMovements.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay movimientos registrados", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredMovements) { movimiento ->
                    FilaMovimientoFinanzas(
                        movimiento = movimiento,
                        onEditar = { movementToEdit = movimiento },
                        onEliminar = { movementToDelete = movimiento }
                    )
                }
            }
        }
    }

    // Botones Flotantes de Acción
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
            SmallFloatingActionButton(
                onClick = { showAddIngreso = true },
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null)
                    Text("Añadir Ingreso")
                }
            }
            FloatingActionButton(
                onClick = { showAddGasto = true },
                containerColor = Color(0xFFC62828),
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null)
                    Text("Añadir Gasto")
                }
            }
        }
    }

    // Diálogos
    if (showAddIngreso) {
        DialogoFinanzas(
            tipo = "Ingreso",
            onDismiss = { showAddIngreso = false },
            onConfirm = { concept, notes, amount ->
                viewModel.insertar(FinanceMovement(
                    concept = concept,
                    notes = notes,
                    amount = amount,
                    type = "INGRESO",
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                    profile = "PYME"
                ))
                showAddIngreso = false
            }
        )
    }

    if (showAddGasto) {
        DialogoFinanzas(
            tipo = "Gasto",
            onDismiss = { showAddGasto = false },
            onConfirm = { concept, notes, amount ->
                viewModel.insertar(FinanceMovement(
                    concept = concept,
                    notes = notes,
                    amount = amount,
                    type = "GASTO",
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                    profile = "PYME"
                ))
                showAddGasto = false
            }
        )
    }

    movementToEdit?.let { movement ->
        DialogoFinanzas(
            tipo = movement.type.lowercase().replaceFirstChar { it.uppercase() },
            movement = movement,
            onDismiss = { movementToEdit = null },
            onConfirm = { concept, notes, amount ->
                viewModel.insertar(movement.copy(concept = concept, notes = notes, amount = amount))
                movementToEdit = null
            }
        )
    }

    movementToDelete?.let { movement ->
        DialogoConfirmarEliminar(
            titulo = "Eliminar movimiento",
            mensaje = "¿Deseas eliminar '${movement.concept}'? Esta acción no se puede deshacer.",
            onConfirmar = {
                viewModel.eliminar(movement.id)
                movementToDelete = null
            },
            onDescartar = { movementToDelete = null }
        )
    }
}

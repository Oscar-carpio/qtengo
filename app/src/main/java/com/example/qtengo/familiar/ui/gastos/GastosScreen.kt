package com.example.qtengo.familiar.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GastosScreen(
    onAddGasto: () -> Unit,
    onBack: () -> Unit,
    viewModel: GastosViewModel = viewModel()
) {
    val gastos by viewModel.gastos.collectAsState()
    val presupuesto by viewModel.presupuesto.collectAsState()
    val gastosRecurrentes by viewModel.gastosRecurrentes.collectAsState()

    var showPresupuestoDialog by remember { mutableStateOf(false) }
    var presupuestoInput by remember { mutableStateOf("") }
    var gastoAEditar by remember { mutableStateOf<Gasto?>(null) }
    var recurrenteAEditar by remember { mutableStateOf<GastoRecurrente?>(null) }
    var showAddRecurrenteDialog by remember { mutableStateOf(false) }

    // Totales reactivos donde se calculan directamente de los estados reactivos de firebase
    val mesActual = remember {
        SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
    }
    val totalGastos = gastos
        .filter { it.tipo == "GASTO" && it.fecha.endsWith(mesActual) }
        .sumOf { it.cantidad }
    val totalRecurrentes = gastosRecurrentes.sumOf { it.cantidad }

    LaunchedEffect(Unit) {
        viewModel.cargarGastos()
        viewModel.cargarPresupuesto()
        viewModel.cargarGastosRecurrentes()
    }

    // Diálogo presupuesto
    if (showPresupuestoDialog) {
        AlertDialog(
            onDismissRequest = { showPresupuestoDialog = false; presupuestoInput = "" },
            title = { Text("Presupuesto mensual") },
            text = {
                OutlinedTextField(
                    value = presupuestoInput,
                    onValueChange = { presupuestoInput = it },
                    label = { Text("Cantidad (€)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    presupuestoInput.toDoubleOrNull()?.let {
                        if (it > 0) {
                            viewModel.guardarPresupuesto(it)
                            showPresupuestoDialog = false
                            presupuestoInput = ""
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showPresupuestoDialog = false; presupuestoInput = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo editar gasto puntual
    gastoAEditar?.let { gasto ->
        EditarGastoDialog(
            gasto = gasto,
            onConfirm = { descripcion, cantidad, categoria ->
                viewModel.editarGasto(gasto.id, descripcion, cantidad, categoria)
                gastoAEditar = null
            },
            onDismiss = { gastoAEditar = null }
        )
    }

    // Diálogo añadir gasto recurrente
    if (showAddRecurrenteDialog) {
        GastoRecurrenteDialog(
            gastoRecurrente = null,
            onConfirm = { descripcion, cantidad, categoria, fechaCobro ->
                viewModel.añadirGastoRecurrente(descripcion, cantidad, categoria, fechaCobro)
                showAddRecurrenteDialog = false
            },
            onDismiss = { showAddRecurrenteDialog = false }
        )
    }

    // Diálogo editar gasto recurrente
    recurrenteAEditar?.let { recurrente ->
        GastoRecurrenteDialog(
            gastoRecurrente = recurrente,
            onConfirm = { descripcion, cantidad, categoria, fechaCobro ->
                viewModel.editarGastoRecurrente(recurrente.id, descripcion, cantidad, categoria, fechaCobro)
                recurrenteAEditar = null
            },
            onDismiss = { recurrenteAEditar = null }
        )
    }

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
                text = "Control de gastos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                GastosResumenCard(
                    totalGastos = totalGastos,
                    totalRecurrentes = totalRecurrentes,
                    presupuesto = presupuesto,
                    onCambiarPresupuesto = {
                        presupuestoInput = presupuesto?.toString() ?: ""
                        showPresupuestoDialog = true
                    }
                )
            }

            item {
                GastosFijosSection(
                    gastosRecurrentes = gastosRecurrentes,
                    onAdd = { showAddRecurrenteDialog = true },
                    onEdit = { recurrenteAEditar = it },
                    onDelete = { viewModel.eliminarGastoRecurrente(it) }
                )
            }

            item {
                GastosPuntualesSection(
                    gastos = gastos,
                    onEdit = { gastoAEditar = it },
                    onDelete = { viewModel.eliminarGasto(it) }
                )
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
            Text(text = "+ Añadir gasto", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }
}
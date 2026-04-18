package com.example.qtengo.familiar.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosScreen(
    onAddGasto: () -> Unit,
    onBack: () -> Unit,
    viewModel: GastosViewModel = viewModel()
) {
    val context = LocalContext.current
    val gastos by viewModel.gastos.collectAsState()
    val gastosFiltrados by viewModel.gastosFiltrados.collectAsState()
    val presupuesto by viewModel.presupuesto.collectAsState()
    val gastosRecurrentes by viewModel.gastosRecurrentes.collectAsState()
    val fechaInicio by viewModel.fechaInicio.collectAsState()
    val fechaFin by viewModel.fechaFin.collectAsState()
    val gastosPorCategoria by viewModel.gastosPorCategoria.collectAsState()

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var showPresupuestoDialog by remember { mutableStateOf(false) }
    var presupuestoInput by remember { mutableStateOf("") }
    var gastoAEditar by remember { mutableStateOf<Gasto?>(null) }
    var recurrenteAEditar by remember { mutableStateOf<GastoRecurrente?>(null) }
    var showAddRecurrenteDialog by remember { mutableStateOf(false) }
    var showFiltroDialog by remember { mutableStateOf(false) }
    var showGrafico by remember { mutableStateOf(false) }
    var showExportarDialog by remember { mutableStateOf(false) }

    // DatePicker states
    val datePickerStateInicio = rememberDatePickerState()
    val datePickerStateFin = rememberDatePickerState()
    var showDatePickerInicio by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }

    // Totales reactivos
    val mesActual = remember {
        SimpleDateFormat("MM/yyyy", Locale("es", "ES")).format(Date())
    }
    val totalGastos = gastos
        .filter { it.tipo == "GASTO" && it.fecha.endsWith(mesActual) }
        .sumOf { it.cantidad }
    val totalRecurrentes = gastosRecurrentes.sumOf { it.cantidad }

    val hayFiltroActivo = fechaInicio != null || fechaFin != null

    LaunchedEffect(Unit) {
        viewModel.cargarGastos()
        viewModel.cargarPresupuesto()
        viewModel.cargarGastosRecurrentes()
    }

    // DatePicker inicio
    if (showDatePickerInicio) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerInicio = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerStateInicio.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        viewModel.filtrarPorFechas(cal.time, fechaFin)
                    }
                    showDatePickerInicio = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerInicio = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerStateInicio)
        }
    }

    // DatePicker fin
    if (showDatePickerFin) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerFin = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerStateFin.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = it
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                        }
                        viewModel.filtrarPorFechas(fechaInicio, cal.time)
                    }
                    showDatePickerFin = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerFin = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerStateFin)
        }
    }

    // Diálogo filtro por fechas
    if (showFiltroDialog) {
        AlertDialog(
            onDismissRequest = { showFiltroDialog = false },
            title = { Text("Filtrar por fechas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showDatePickerInicio = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (fechaInicio != null) "Desde: ${sdf.format(fechaInicio!!)}"
                            else "Seleccionar fecha inicio"
                        )
                    }
                    OutlinedButton(
                        onClick = { showDatePickerFin = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (fechaFin != null) "Hasta: ${sdf.format(fechaFin!!)}"
                            else "Seleccionar fecha fin"
                        )
                    }
                    if (hayFiltroActivo) {
                        TextButton(
                            onClick = {
                                viewModel.limpiarFiltro()
                                showFiltroDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Limpiar filtro", color = Color.Red)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFiltroDialog = false }) { Text("Cerrar") }
            }
        )
    }

    // Diálogo exportar
    if (showExportarDialog) {
        AlertDialog(
            onDismissRequest = { showExportarDialog = false },
            title = { Text("Exportar gastos") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Se exportarán ${gastosFiltrados.size} movimientos${if (hayFiltroActivo) " (filtrados)" else ""}.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            ExportarGastosHelper.exportarCSV(context, gastosFiltrados)
                            showExportarDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Exportar CSV")
                    }
                    Button(
                        onClick = {
                            ExportarGastosHelper.exportarPDF(context, gastosFiltrados)
                            showExportarDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
                    ) {
                        Text("Exportar PDF")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportarDialog = false }) { Text("Cancelar") }
            }
        )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A3A6B))
                .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }) {
                Text(text = "←", fontSize = 24.sp, color = Color.White)
            }
            Text(
                text = "Control de gastos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = { showGrafico = !showGrafico }) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Ver gráfico",
                    tint = if (showGrafico) Color(0xFFFFC107) else Color.White
                )
            }
            IconButton(onClick = { showExportarDialog = true }) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Exportar",
                    tint = Color.White
                )
            }
            IconButton(onClick = { showFiltroDialog = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Filtrar por fechas",
                    tint = if (hayFiltroActivo) Color(0xFFFFC107) else Color.White
                )
            }

        }

        // Banner filtro activo
        if (hayFiltroActivo) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Filtro: ${if (fechaInicio != null) sdf.format(fechaInicio!!) else "..."} → ${if (fechaFin != null) sdf.format(fechaFin!!) else "..."}",
                        fontSize = 13.sp,
                        color = Color(0xFF1A3A6B),
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = { viewModel.limpiarFiltro() }) {
                        Text("Limpiar", color = Color.Red, fontSize = 12.sp)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Gráfico por categoría (visible solo si showGrafico = true)
            if (showGrafico) {
                item {
                    GraficoCategorias(gastosPorCategoria = gastosPorCategoria)
                }
            }

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
                    gastos = gastosFiltrados,
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
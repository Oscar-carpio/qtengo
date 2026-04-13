package com.example.qtengo.pyme.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Filtro unificado para la sección de Finanzas.
 */
@Composable
fun FiltrosFinanzasPyme(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    PymeFilterCard(
        title = "Buscar Movimiento",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    )
}

/**
 * Filtro unificado para la sección de Empleados.
 */
@Composable
fun FiltrosEmpleadosPyme(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortByAlphabetical: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    PymeFilterCard(
        title = "Buscar y Ordenar Plantilla",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = sortByAlphabetical, onCheckedChange = onSortChange)
            Text("Orden Alfabético (A-Z)", fontSize = 14.sp)
        }
    }
}

/**
 * Filtro unificado para la sección de Proveedores.
 */
@Composable
fun FiltrosProveedoresPyme(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortByAlphabetical: Boolean,
    onSortChange: (Boolean) -> Unit
) {
    PymeFilterCard(
        title = "Buscador de Proveedores",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = sortByAlphabetical, onCheckedChange = onSortChange)
            Text("Orden Alfabético (A-Z)", fontSize = 14.sp)
        }
    }
}

/**
 * Filtro unificado para la sección de Productos / Almacén.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltrosProductosPyme(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortBy: String,
    isAscending: Boolean,
    onSortChange: (String, Boolean) -> Unit,
    selectedUnits: Set<String>,
    onUnitsChange: (Set<String>) -> Unit,
    filterByLowStock: Boolean,
    onLowStockChange: (Boolean) -> Unit
) {
    val unitOptions = listOf("Uds", "kg", "litros", "barriles", "paquetes", "cajas")

    PymeFilterCard(
        title = "Buscador y Filtros de Stock",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text("Ordenar por:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Nombre", "Cantidad").forEach { option ->
                        FilterChip(
                            selected = sortBy == option,
                            onClick = {
                                if (sortBy == option) onSortChange(option, !isAscending)
                                else onSortChange(option, true)
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(option)
                                    if (sortBy == option) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Column {
                Text("Filtrar por Unidades:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    unitOptions.forEach { unit ->
                        val isSelected = selectedUnits.contains(unit)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onUnitsChange(if (isSelected) selectedUnits - unit else selectedUnits + unit)
                            },
                            label = { Text(unit) }
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = filterByLowStock, onCheckedChange = onLowStockChange)
                Text("Ver solo poco stock", fontSize = 14.sp)
            }
        }
    }
}

/**
 * Filtro unificado y avanzado para la sección de Tareas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosTareasPyme(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusFilter: String,
    onStatusChange: (String) -> Unit,
    filterMonth: Int,
    onMonthChange: (Int) -> Unit,
    filterYear: String,
    onYearChange: (String) -> Unit,
    dateFilterEnabled: Boolean,
    onDateFilterToggle: (Boolean) -> Unit,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val months = listOf("Todos", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val currentYearInt = Calendar.getInstance().get(Calendar.YEAR)
    val yearsList = listOf("Todos") + (currentYearInt downTo 2023).map { it.toString() }
    
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    PymeFilterCard(
        title = "Buscador y Filtros",
        searchQuery = searchQuery,
        onSearchChange = onSearchChange
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text("Estado:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todas", "Pendientes", "Realizadas").forEach { s ->
                        FilterChip(
                            selected = statusFilter == s,
                            onClick = { onStatusChange(s) },
                            label = { Text(s) }
                        )
                    }
                }
            }

            Column {
                Text("Filtrar por creación (Mes / Año):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = monthExpanded,
                        onExpandedChange = { monthExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = months[filterMonth],
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mes") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                        )
                        ExposedDropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                            months.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = { onMonthChange(index); monthExpanded = false }
                                )
                            }
                        }
                    }
                    
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = filterYear,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Año") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                        )
                        ExposedDropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                            yearsList.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year) },
                                    onClick = { onYearChange(year); yearExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = dateFilterEnabled, onCheckedChange = onDateFilterToggle)
                    Text("Filtrar por Fecha Programada", fontSize = 14.sp)
                }
                
                if (dateFilterEnabled) {
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendar.set(year, month, dayOfMonth)
                                    onDateSelected(sdf.format(calendar.time))
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
                    ) {
                        Icon(Icons.Default.CalendarMonth, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Fecha: $selectedDate")
                    }
                }
            }
        }
    }
}

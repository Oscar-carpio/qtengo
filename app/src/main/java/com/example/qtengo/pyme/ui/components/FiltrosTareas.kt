package com.example.qtengo.pyme.ui.components

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosTareas(
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
    var filtersExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val months = listOf("Todos", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val currentYearInt = Calendar.getInstance().get(Calendar.YEAR)
    val yearsList = listOf("Todos") + (currentYearInt downTo 2023).map { it.toString() }
    
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { filtersExpanded = !filtersExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FilterList, null, tint = Color(0xFF1A3A6B))
                    Spacer(Modifier.width(8.dp))
                    Text("Buscador y Filtros", fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                }
                Icon(if (filtersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }

            AnimatedVisibility(visible = filtersExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        label = { Text("Buscar...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
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
    }
}

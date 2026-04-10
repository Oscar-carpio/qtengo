package com.example.qtengo.pyme.ui.empleados

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.qtengo.core.domain.models.Employee
import com.example.qtengo.core.ui.components.QtengoTopBar

/**
 * Pantalla para la gestión de empleados del módulo Pyme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadosPantalla(
    profile: String = "PYME",
    viewModel: EmpleadosViewModel = viewModel(),
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit,
    onBack: () -> Unit
) {
    val employees by viewModel.employees.observeAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var empleadoParaEditar by remember { mutableStateOf<Employee?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    var sortByAlphabetical by remember { mutableStateOf(true) }
    var filtersExpanded by remember { mutableStateOf(false) }

    val filteredEmployees = employees.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }.let { list ->
        if (sortByAlphabetical) list.sortedBy { it.name }
        else list
    }

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Plantilla de Empleados",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { filtersExpanded = !filtersExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = Color(0xFF1A3A6B))
                        Spacer(Modifier.width(8.dp))
                        Text("Buscar y Ordenar", fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    }
                    Icon(if (filtersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                }

                AnimatedVisibility(visible = filtersExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Nombre del empleado...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = sortByAlphabetical, onCheckedChange = { sortByAlphabetical = it })
                            Text("Orden Alfabético (A-Z)", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        if (filteredEmployees.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No se encontraron resultados", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEmployees) { employee ->
                    EmpleadoCardItem(
                        employee = employee, 
                        onEdit = { empleadoParaEditar = employee },
                        onDelete = { viewModel.delete(employee.id) }
                    )
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Añadir Empleado")
        }
    }

    if (showAddDialog) {
        DialogoEmpleado(
            titulo = "Alta de Trabajador",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, pos, sal, phone ->
                viewModel.insert(name, pos, sal, phone, "hoy")
                showAddDialog = false
            }
        )
    }

    empleadoParaEditar?.let { employee ->
        DialogoEmpleado(
            titulo = "Editar Trabajador",
            employee = employee,
            onDismiss = { empleadoParaEditar = null },
            onConfirm = { name, pos, sal, phone ->
                viewModel.update(employee.copy(name = name, position = pos, salary = sal, phone = phone))
                empleadoParaEditar = null
            }
        )
    }
}

/**
 * Tarjeta individual para cada empleado con botones de editar y eliminar.
 */
@Composable
fun EmpleadoCardItem(employee: Employee, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color(0xFFE3F2FD)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AssignmentInd, null, tint = Color(0xFF1565C0))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(employee.name, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                Text(employee.position, fontSize = 13.sp, color = Color.Gray)
                Text("%.2f €".format(employee.salary), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        }
    }
}

/**
 * Diálogo para añadir o editar empleados.
 * Incluye validación de salario para evitar números negativos y letras.
 */
@Composable
fun DialogoEmpleado(
    titulo: String,
    employee: Employee? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var pos by remember { mutableStateOf(employee?.position ?: "") }
    var sal by remember { mutableStateOf(employee?.salary?.toString() ?: "") }
    var phone by remember { mutableStateOf(employee?.phone ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Completo") })
                OutlinedTextField(value = pos, onValueChange = { pos = it }, label = { Text("Cargo") })
                OutlinedTextField(
                    value = sal, 
                    onValueChange = { input ->
                        // Validar para que solo acepte números y un punto decimal, sin negativos ni letras
                        if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                            if (input.count { it == '.' } <= 1) {
                                sal = input
                            }
                        }
                    }, 
                    label = { Text("Salario Mensual") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            }
        },
        confirmButton = {
            Button(onClick = { 
                val salarioValor = sal.toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty() && salarioValor >= 0) {
                    onConfirm(name, pos, salarioValor, phone)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

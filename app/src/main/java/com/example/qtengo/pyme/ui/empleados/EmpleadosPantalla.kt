package com.example.qtengo.pyme.ui.empleados

import android.content.Intent
import android.net.Uri
import android.util.Patterns
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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
    val context = LocalContext.current
    
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
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${employee.phone}")
                            }
                            context.startActivity(intent)
                        },
                        onEmail = {
                            if (employee.email.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${employee.email}")
                                }
                                context.startActivity(intent)
                            }
                        },
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
            onConfirm = { name, pos, sal, phone, email, notes ->
                viewModel.insert(name, pos, sal, phone, email, notes)
                showAddDialog = false
            }
        )
    }

    empleadoParaEditar?.let { employee ->
        DialogoEmpleado(
            titulo = "Editar Trabajador",
            employee = employee,
            onDismiss = { empleadoParaEditar = null },
            onConfirm = { name, pos, sal, phone, email, notes ->
                viewModel.update(employee.copy(name = name, position = pos, salary = sal, phone = phone, email = email, details = notes))
                empleadoParaEditar = null
            }
        )
    }
}

/**
 * Tarjeta individual para cada empleado con visualización compacta y datos de contacto clicables.
 */
@Composable
fun EmpleadoCardItem(
    employee: Employee, 
    onCall: () -> Unit,
    onEmail: () -> Unit,
    onEdit: () -> Unit, 
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = Color(0xFFE3F2FD)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AssignmentInd, null, tint = Color(0xFF1565C0))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = employee.name, 
                        fontWeight = FontWeight.Bold, 
                        color = Color(0xFF1A3A6B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = employee.position, 
                        fontSize = 13.sp, 
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "%.2f €".format(employee.salary), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                }
            }
            
            // Sección de contacto y detalles
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (employee.phone.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .clickable { onCall() }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(14.dp), tint = Color(0xFF1565C0))
                        Spacer(Modifier.width(6.dp))
                        Text(text = employee.phone, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF1565C0))
                    }
                }
                if (employee.email.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .clickable { onEmail() }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(14.dp), tint = Color(0xFF1565C0))
                        Spacer(Modifier.width(6.dp))
                        Text(text = employee.email, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF1565C0))
                    }
                }
                if (employee.details.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Notes, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = employee.details, 
                            fontSize = 12.sp, 
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo para añadir o editar empleados con validaciones de contacto.
 */
@Composable
fun DialogoEmpleado(
    titulo: String,
    employee: Employee? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, Double, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(employee?.name ?: "") }
    var pos by remember { mutableStateOf(employee?.position ?: "") }
    var sal by remember { mutableStateOf(employee?.salary?.toString() ?: "") }
    var phone by remember { mutableStateOf(employee?.phone ?: "") }
    var email by remember { mutableStateOf(employee?.email ?: "") }
    var notes by remember { mutableStateOf(employee?.details ?: "") }

    val isEmailValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPhoneValid = phone.isEmpty() || phone.startsWith("+") || phone.all { it.isDigit() }

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
                        if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                            if (input.count { it == '.' } <= 1) {
                                sal = input
                            }
                        }
                    }, 
                    label = { Text("Salario Mensual") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '+' }) phone = it }, 
                    label = { Text("Teléfono (ej: +34...)") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = !isPhoneValid
                )

                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("Email de contacto") }, 
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = !isEmailValid,
                    supportingText = { if (!isEmailValid) Text("Formato inválido", color = Color.Red) }
                )

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas / Detalles") })
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val salarioValor = sal.toDoubleOrNull() ?: 0.0
                    if (name.isNotEmpty() && salarioValor >= 0 && isEmailValid && isPhoneValid) {
                        onConfirm(name, pos, salarioValor, phone, email, notes)
                    }
                },
                enabled = name.isNotEmpty() && isEmailValid && isPhoneValid && phone.isNotEmpty()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

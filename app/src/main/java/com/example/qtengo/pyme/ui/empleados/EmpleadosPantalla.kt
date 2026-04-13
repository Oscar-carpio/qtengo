/**
 * Pantalla de Gestión de Personal.
 */
package com.example.qtengo.pyme.ui.empleados

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.Employee
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.pyme.ui.empleados.components.DialogoEmpleado
import com.example.qtengo.pyme.ui.empleados.components.EmpleadoCardItem
import com.example.qtengo.pyme.ui.filtros.FiltrosEmpleados

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
    var sortBy by remember { mutableStateOf("Nombre") }
    var isAscending by remember { mutableStateOf(true) }

    val filteredEmployees = remember(employees, searchQuery, sortBy, isAscending) {
        employees.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }.sortedWith { e1, e2 ->
            val res = e1.name.compareTo(e2.name, ignoreCase = true)
            if (isAscending) res else -res
        }
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

        FiltrosEmpleados(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            sortBy = sortBy,
            isAscending = isAscending,
            onSortChange = { s, a -> sortBy = s; isAscending = a }
        )

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

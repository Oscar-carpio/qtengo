package com.example.qtengo.pyme.ui.empleados

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.qtengo.pyme.ui.DialogoConfirmarEliminar
import com.example.qtengo.pyme.ui.TarjetaEstadisticaPyme
import com.example.qtengo.pyme.ui.filtros.FiltrosEmpleados

/**
 * Pantalla de Gestión de Empleados (PYME).
 * Permite administrar la plantilla, ver salarios y detalles de contacto.
 */
@Composable
fun EmpleadosPantalla(
    profile: String = "PYME",
    viewModel: EmpleadosViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val context = LocalContext.current
    val employees by viewModel.employees.observeAsState(emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("") }
    var isAscending by remember { mutableStateOf(true) }

    var showAddDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    val filteredEmployees = employees.filter { employee ->
        employee.name.contains(searchQuery, ignoreCase = true) ||
        employee.position.contains(searchQuery, ignoreCase = true)
    }.let { list ->
        if (sortBy == "") {
            list.sortedByDescending { it.timestamp }
        } else {
            list.sortedWith { e1, e2 ->
                val res = when (sortBy) {
                    "Nombre" -> e1.name.compareTo(e2.name, ignoreCase = true)
                    else -> 0
                }
                if (res != 0) {
                    if (isAscending) res else -res
                } else {
                    e2.timestamp.compareTo(e1.timestamp)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB))) {
        QtengoTopBar(
            title = "Gestión de Empleados",
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

        // Resumen
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaEstadisticaPyme(
                titulo = "Plantilla",
                valor = "${employees.size}",
                color = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadisticaPyme(
                titulo = "Coste Salarial",
                valor = String.format("%.2f€", employees.sumOf { it.salary }),
                color = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (filteredEmployees.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay empleados registrados", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredEmployees) { empleado ->
                    ElementoTarjetaEmpleado(
                        empleado = empleado,
                        onLlamar = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${empleado.phone}"))
                            context.startActivity(intent)
                        },
                        onCorreo = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${empleado.email}"))
                            context.startActivity(intent)
                        },
                        onEditar = { employeeToEdit = empleado },
                        onEliminar = { employeeToDelete = empleado }
                    )
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B), contentColor = Color.White)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Añadir Empleado", color = Color.White)
        }
    }

    if (showAddDialog) {
        DialogoEmpleado(
            titulo = "Nuevo Empleado",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, pos, sal, phone, email, notes ->
                viewModel.insertar(name, pos, sal, phone, email, notes)
                showAddDialog = false
            }
        )
    }

    employeeToEdit?.let { emp ->
        DialogoEmpleado(
            titulo = "Editar Empleado",
            employee = emp,
            onDismiss = { employeeToEdit = null },
            onConfirm = { name, pos, sal, phone, email, notes ->
                viewModel.actualizar(emp.copy(name = name, position = pos, salary = sal, phone = phone, email = email, details = notes))
                employeeToEdit = null
            }
        )
    }

    employeeToDelete?.let { emp ->
        DialogoConfirmarEliminar(
            titulo = "Eliminar empleado",
            mensaje = "¿Deseas eliminar a '${emp.name}' de la plantilla? Esta acción no se puede deshacer.",
            onConfirmar = {
                viewModel.eliminar(emp.id)
                employeeToDelete = null
            },
            onDescartar = { employeeToDelete = null }
        )
    }
}

package com.example.qtengo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.data.local.model.Employee

/**
 * Pantalla para la gestión de la plantilla de empleados.
 */
@Composable
fun EmployeeScreen(
    profile: String = "PYME",
    viewModel: EmployeeViewModel = viewModel(),
    onBack: () -> Unit
) {
    val employees by viewModel.employees.observeAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        viewModel.loadProfile(profile)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        // Cabecera
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
                text = "Plantilla de Empleados",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Resumen rápido
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Plantilla", fontSize = 12.sp, color = Color.Gray)
                    Text("${employees.size} trabajadores", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Coste salarial", fontSize = 12.sp, color = Color.Gray)
                    Text("%.2f €".format(employees.sumOf { it.salary }), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                }
            }
        }

        if (employees.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay empleados registrados", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(employees) { employee ->
                    EmployeeCard(employee, onDelete = { viewModel.delete(employee) })
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Añadir Empleado")
        }
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, pos, sal, phone, date ->
                viewModel.insert(name, pos, sal, phone, date)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun EmployeeCard(employee: Employee, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono profesional centrado (AssignmentInd)
            Surface(
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                color = Color(0xFFE3F2FD)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AssignmentInd, 
                        contentDescription = null, 
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(employee.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A3A6B))
                Text(employee.position, fontSize = 13.sp, color = Color.Gray)
                Text("Tlf: ${employee.phone}", fontSize = 11.sp, color = Color.LightGray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("%.2f €".format(employee.salary), fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(24.dp)) {
                    Text("×", color = Color(0xFFD32F2F), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar empleado?") },
            text = { Text("¿Estás seguro de que deseas eliminar a ${employee.name} de la plantilla?") },
            confirmButton = {
                TextButton(onClick = { 
                    onDelete()
                    showDeleteConfirm = false 
                }) {
                    Text("Eliminar", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AddEmployeeDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf("") }
    var sal by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alta de Trabajador") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Completo") })
                OutlinedTextField(value = pos, onValueChange = { pos = it }, label = { Text("Cargo") })
                OutlinedTextField(value = sal, onValueChange = { sal = it }, label = { Text("Salario Mensual") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (name.isNotEmpty()) onConfirm(name, pos, sal.toDoubleOrNull() ?: 0.0, phone, "hoy")
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

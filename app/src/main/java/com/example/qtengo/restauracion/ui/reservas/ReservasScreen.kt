package com.example.qtengo.restauracion.ui.reservas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservasScreen(
    onBack: () -> Unit,
    viewModel: ReservasViewModel = viewModel()
) {
    val reservas by viewModel.reservas.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargarReservas() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Reservas") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Nueva Reserva")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reservas) { reserva ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reserva.nombreCliente, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${reserva.comensales} personas", style = MaterialTheme.typography.bodyMedium)
                            if (reserva.notas.isNotBlank()) {
                                Text(reserva.notas, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { viewModel.eliminarReserva(reserva.id) }) {
                            Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddReservaDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { n, c, nt -> viewModel.agregarReserva(n, c, nt); showAddDialog = false }
            )
        }
    }
}

@Composable
fun AddReservaDialog(onDismiss: () -> Unit, onAdd: (String, Int, String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var personas by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Reserva") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Cliente") })
                OutlinedTextField(value = personas, onValueChange = { personas = it }, label = { Text("Número personas") })
                OutlinedTextField(value = notas, onValueChange = { notas = it }, label = { Text("Notas") })
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(nombre, personas.toIntOrNull() ?: 1, notas) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
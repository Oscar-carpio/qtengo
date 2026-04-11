package com.example.qtengo.restauracion.ui.menu

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
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDiaScreen(
    onBack: () -> Unit,
    viewModel: MenuViewModel = viewModel()
) {
    val platos by viewModel.platos.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.cargarMenu() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carta y Menú") },
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
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, "Nuevo Plato")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(platos) { plato ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(plato.nombre, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${"%.2f".format(plato.precio)} €") },
                        trailingContent = {
                            IconButton(onClick = { viewModel.eliminarPlato(plato.id) }) {
                                Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }
        }

        if (showDialog) {
            AddPlatoDialog(
                onDismiss = { showDialog = false },
                onAdd = { n, p -> viewModel.agregarPlato(n, p); showDialog = false }
            )
        }
    }
}

@Composable
fun AddPlatoDialog(onDismiss: () -> Unit, onAdd: (String, Double) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Plato") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del plato") })
                OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") })
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(nombre, precio.toDoubleOrNull() ?: 0.0) }) { Text("Añadir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
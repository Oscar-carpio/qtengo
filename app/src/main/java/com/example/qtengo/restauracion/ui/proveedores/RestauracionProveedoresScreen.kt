package com.example.qtengo.restauracion.ui.proveedores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.restauracion.ui.proveedores.RestauracionProveedor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestauracionProveedoresScreen(
    onBack: () -> Unit,
    viewModel: ProveedoresRestauracionViewModel = viewModel()
) {
    val proveedores by viewModel.proveedoresFiltrados.collectAsState()
    val filtro by viewModel.filtro.collectAsState()


    val error by viewModel.error.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var proveedorAEditar by remember { mutableStateOf<RestauracionProveedor?>(null) }
    var proveedorAEliminar by remember { mutableStateOf<RestauracionProveedor?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarProveedores()
    }
    error?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proveedores Restauración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = filtro,
                onValueChange = { viewModel.actualizarFiltro(it) },
                label = { Text("Buscar proveedor") },
                placeholder = { Text("Nombre, teléfono, email o dirección") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (proveedores.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filtro.isBlank()) {
                            "No hay proveedores registrados."
                        } else {
                            "No se encontraron proveedores."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = proveedores,
                        key = { it.id }
                    ) { proveedor ->
                        ProveedorRestauracionCard(
                            proveedor = proveedor,
                            onEditar = {
                                proveedorAEditar = proveedor
                            },
                            onEliminar = {
                                proveedorAEliminar = proveedor
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            ProveedorRestauracionDialog(
                titulo = "Añadir Proveedor",
                proveedorInicial = null,
                onDismiss = { showAddDialog = false },
                onGuardar = { nombre, telefono, email, direccion ->
                    viewModel.agregarProveedor(
                        nombre = nombre,
                        telefono = telefono,
                        email = email,
                        direccion = direccion
                    )
                    showAddDialog = false
                }
            )
        }

        proveedorAEditar?.let { proveedor ->
            ProveedorRestauracionDialog(
                titulo = "Editar Proveedor",
                proveedorInicial = proveedor,
                onDismiss = { proveedorAEditar = null },
                onGuardar = { nombre, telefono, email, direccion ->
                    viewModel.editarProveedor(
                        id = proveedor.id,
                        nombre = nombre,
                        telefono = telefono,
                        email = email,
                        direccion = direccion
                    )
                    proveedorAEditar = null
                }
            )
        }

        proveedorAEliminar?.let { proveedor ->
            AlertDialog(
                onDismissRequest = { proveedorAEliminar = null },
                title = { Text("Eliminar proveedor") },
                text = {
                    Text("¿Deseas eliminar a \"${proveedor.nombre}\"?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.eliminarProveedor(proveedor.id)
                            proveedorAEliminar = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { proveedorAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ProveedorRestauracionCard(
    proveedor: RestauracionProveedor,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = proveedor.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tel: ${proveedor.telefono.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Email: ${proveedor.email.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Dir: ${proveedor.direccion.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedorRestauracionDialog(
    titulo: String,
    proveedorInicial: RestauracionProveedor?,
    onDismiss: () -> Unit,
    onGuardar: (String, String, String, String) -> Unit
) {
    var nombre by remember(proveedorInicial) {
        mutableStateOf(proveedorInicial?.nombre ?: "")
    }
    var telefono by remember(proveedorInicial) {
        mutableStateOf(proveedorInicial?.telefono ?: "")
    }
    var email by remember(proveedorInicial) {
        mutableStateOf(proveedorInicial?.email ?: "")
    }
    var direccion by remember(proveedorInicial) {
        mutableStateOf(proveedorInicial?.direccion ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.trim().isNotBlank()) {
                        onGuardar(
                            nombre.trim(),
                            telefono.trim(),
                            email.trim(),
                            direccion.trim()
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
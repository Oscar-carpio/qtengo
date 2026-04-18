package com.example.qtengo.restauracion.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.data.model.restauracion.RestauracionPlato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuDiaScreen(
    onBack: () -> Unit,
    viewModel: MenuViewModel = viewModel()
) {
    val platos by viewModel.platosFiltrados.collectAsState()
    val filtro by viewModel.filtro.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var platoAEditar by remember { mutableStateOf<RestauracionPlato?>(null) }
    var platoAEliminar by remember { mutableStateOf<RestauracionPlato?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarMenu()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carta y Menú") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Plato")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = filtro,
                onValueChange = { viewModel.actualizarFiltro(it) },
                label = { Text("Buscar plato") },
                placeholder = { Text("Nombre o precio") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (platos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (filtro.isBlank()) {
                            "No hay platos registrados."
                        } else {
                            "No se encontraron platos."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = platos,
                        key = { it.id }
                    ) { plato ->
                        PlatoCard(
                            plato = plato,
                            onEditar = { platoAEditar = plato },
                            onEliminar = { platoAEliminar = plato }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            PlatoDialog(
                titulo = "Añadir Plato",
                platoInicial = null,
                onDismiss = { showDialog = false },
                onGuardar = { nombre, precio, disponible ->
                    viewModel.agregarPlato(nombre, precio)
                    showDialog = false
                }
            )
        }

        platoAEditar?.let { plato ->
            PlatoDialog(
                titulo = "Editar Plato",
                platoInicial = plato,
                onDismiss = { platoAEditar = null },
                onGuardar = { nombre, precio, disponible ->
                    viewModel.editarPlato(
                        id = plato.id,
                        nombre = nombre,
                        precio = precio,
                        disponible = disponible
                    )
                    platoAEditar = null
                }
            )
        }

        platoAEliminar?.let { plato ->
            AlertDialog(
                onDismissRequest = { platoAEliminar = null },
                title = { Text("Eliminar plato") },
                text = { Text("¿Deseas eliminar \"${plato.nombre}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.eliminarPlato(plato.id)
                            platoAEliminar = null
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { platoAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun PlatoCard(
    plato: RestauracionPlato,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = plato.nombre,
                    fontWeight = FontWeight.Bold
                )
            },
            supportingContent = {
                Column {
                    Text("${"%.2f".format(plato.precio)} €")
                    Text(
                        if (plato.disponible) "Disponible" else "No disponible",
                        color = if (plato.disponible) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onEditar) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar"
                        )
                    }

                    IconButton(onClick = onEliminar) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun PlatoDialog(
    titulo: String,
    platoInicial: RestauracionPlato?,
    onDismiss: () -> Unit,
    onGuardar: (String, Double, Boolean) -> Unit
) {
    var nombre by remember(platoInicial) {
        mutableStateOf(platoInicial?.nombre ?: "")
    }
    var precio by remember(platoInicial) {
        mutableStateOf(
            if (platoInicial == null) "" else platoInicial.precio.toString()
        )
    }
    var disponible by remember(platoInicial) {
        mutableStateOf(platoInicial?.disponible ?: true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del plato") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = disponible,
                        onCheckedChange = { disponible = it }
                    )
                    Text("Disponible")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    if (nombre.trim().isNotBlank() && precioDouble > 0.0) {
                        onGuardar(
                            nombre.trim(),
                            precioDouble,
                            disponible
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
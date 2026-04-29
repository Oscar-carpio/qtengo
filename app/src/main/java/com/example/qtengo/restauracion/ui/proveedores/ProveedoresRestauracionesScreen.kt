package com.example.qtengo.restauracion.ui.proveedores

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.ui.components.QtengoTopBar

@Composable
fun ProveedoresRestauracionScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit,
    viewModel: ProveedoresRestauracionViewModel = viewModel()
) {
    val proveedores by viewModel.proveedoresFiltrados.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var proveedorAEditar by remember { mutableStateOf<Proveedor?>(null) }
    var proveedorAEliminar by remember { mutableStateOf<Proveedor?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarProveedores() }

    // ─── Diálogo de error ────────────────────────────────────────────────────
    error?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("Aceptar") }
            }
        )
    }

    // ─── Diálogo añadir ──────────────────────────────────────────────────────
    if (showAddDialog) {
        ProveedorDialog(
            proveedor = null,
            onConfirm = { viewModel.agregarProveedor(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    // ─── Diálogo editar ──────────────────────────────────────────────────────
    proveedorAEditar?.let { proveedor ->
        ProveedorDialog(
            proveedor = proveedor,
            onConfirm = { viewModel.editarProveedor(proveedor.id, it); proveedorAEditar = null },
            onDismiss = { proveedorAEditar = null }
        )
    }

    // ─── Diálogo confirmar eliminación ───────────────────────────────────────
    proveedorAEliminar?.let { proveedor ->
        AlertDialog(
            onDismissRequest = { proveedorAEliminar = null },
            title = { Text("Eliminar proveedor") },
            text = { Text("¿Deseas eliminar a \"${proveedor.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminarProveedor(proveedor.id); proveedorAEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { proveedorAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // ─── Layout principal ────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Proveedores",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        // Buscador
        OutlinedTextField(
            value = filtro,
            onValueChange = { viewModel.actualizarFiltro(it) },
            label = { Text("Buscar proveedor") },
            placeholder = { Text("Nombre, teléfono, email o dirección") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Indicador de carga
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Lista o estado vacío
        if (proveedores.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (filtro.isBlank()) "No hay proveedores. ¡Añade uno!" else "No se encontraron proveedores.",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(proveedores, key = { it.id }) { proveedor ->
                    ProveedorCard(
                        proveedor = proveedor,
                        onEdit = { proveedorAEditar = proveedor },
                        onDelete = { proveedorAEliminar = proveedor }   // confirmación antes de borrar
                    )
                }
            }
        }

        // Botón añadir
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir proveedor")
        }
    }
}

// ─── Card ────────────────────────────────────────────────────────────────────

@Composable
fun ProveedorCard(proveedor: Proveedor, onEdit: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Cabecera: icono + nombre + acciones
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🚚", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        proveedor.nombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A3A6B)
                    )
                    if (proveedor.direccion.isNotBlank()) {
                        Text(proveedor.direccion, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF1A3A6B))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.LightGray)
                }
            }

            // Contacto: teléfono y email con acciones directas
            if (proveedor.telefono.isNotBlank() || proveedor.email.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (proveedor.telefono.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${proveedor.telefono}"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(proveedor.telefono, fontSize = 12.sp)
                        }
                    }
                    if (proveedor.email.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${proveedor.email}"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(proveedor.email, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Notas
            if (proveedor.notas.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(proveedor.notas, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// ─── Dialog ──────────────────────────────────────────────────────────────────

@Composable
fun ProveedorDialog(
    proveedor: Proveedor?,
    onConfirm: (Proveedor) -> Unit,
    onDismiss: () -> Unit
) {
    var nombre by remember(proveedor) { mutableStateOf(proveedor?.nombre ?: "") }
    var telefono by remember(proveedor) { mutableStateOf(proveedor?.telefono ?: "") }
    var email by remember(proveedor) { mutableStateOf(proveedor?.email ?: "") }
    var direccion by remember(proveedor) { mutableStateOf(proveedor?.direccion ?: "") }
    var notas by remember(proveedor) { mutableStateOf(proveedor?.notas ?: "") }
    var errorNombre by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (proveedor == null) "Nuevo proveedor" else "Editar proveedor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorNombre = "" },
                    label = { Text("Nombre del proveedor *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorNombre.isNotEmpty(),
                    supportingText = {
                        if (errorNombre.isNotEmpty())
                            Text(errorNombre, color = MaterialTheme.colorScheme.error)
                    }
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = { Text("Dirección (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nombre.isBlank()) {
                    errorNombre = "El nombre es obligatorio"
                    return@TextButton
                }
                onConfirm(
                    Proveedor(
                        id = proveedor?.id ?: "",
                        nombre = nombre.trim(),
                        telefono = telefono.trim(),
                        email = email.trim(),
                        direccion = direccion.trim(),
                        notas = notas.trim()
                    )
                )
            }) {
                Text(if (proveedor == null) "Añadir" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
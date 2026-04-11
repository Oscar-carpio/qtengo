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
    viewModel: ProveedoresViewModel = viewModel()
) {
    val proveedores by viewModel.proveedores.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var proveedorAEditar by remember { mutableStateOf<Proveedor?>(null) }

    LaunchedEffect(Unit) { viewModel.cargarProveedores() }

    if (showAddDialog) {
        ProveedorDialog(
            proveedor = null,
            onConfirm = { viewModel.añadirProveedor(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    proveedorAEditar?.let { proveedor ->
        ProveedorDialog(
            proveedor = proveedor,
            onConfirm = { viewModel.editarProveedor(proveedor.id, it); proveedorAEditar = null },
            onDismiss = { proveedorAEditar = null }
        )
    }

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

        if (proveedores.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay proveedores. ¡Añade uno!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(proveedores) { proveedor ->
                    ProveedorCard(
                        proveedor = proveedor,
                        onEdit = { proveedorAEditar = proveedor },
                        onDelete = { viewModel.eliminarProveedor(proveedor.id) }
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
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Añadir proveedor")
        }
    }
}

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
                    Text(proveedor.nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    if (proveedor.productos.isNotBlank()) {
                        Text(proveedor.productos, fontSize = 12.sp, color = Color.Gray)
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF1A3A6B))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.LightGray)
                }
            }

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

            if (proveedor.notas.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(proveedor.notas, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ProveedorDialog(proveedor: Proveedor?, onConfirm: (Proveedor) -> Unit, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf(proveedor?.nombre ?: "") }
    var telefono by remember { mutableStateOf(proveedor?.telefono ?: "") }
    var email by remember { mutableStateOf(proveedor?.email ?: "") }
    var productos by remember { mutableStateOf(proveedor?.productos ?: "") }
    var notas by remember { mutableStateOf(proveedor?.notas ?: "") }
    var errorNombre by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (proveedor == null) "Nuevo proveedor" else "Editar proveedor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorNombre = "" },
                    label = { Text("Nombre del proveedor") },
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
                    value = productos,
                    onValueChange = { productos = it },
                    label = { Text("Productos que suministra") },
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
                if (nombre.isBlank()) { errorNombre = "El nombre es obligatorio"; return@TextButton }
                onConfirm(Proveedor(
                    nombre = nombre.trim(),
                    telefono = telefono.trim(),
                    email = email.trim(),
                    productos = productos.trim(),
                    notas = notas.trim()
                ))
            }) { Text(if (proveedor == null) "Añadir" else "Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
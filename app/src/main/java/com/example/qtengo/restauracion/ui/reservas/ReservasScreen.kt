package com.example.qtengo.restauracion.ui.reservas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
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
fun ReservasScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit,
    viewModel: ReservasViewModel = viewModel()
) {
    val reservas by viewModel.reservas.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var filtroEstado by remember { mutableStateOf("Todas") }

    val estados = listOf("Todas", "Confirmada", "Pendiente", "Cancelada")
    val reservasFiltradas = if (filtroEstado == "Todas") reservas else reservas.filter { it.estado == filtroEstado }

    LaunchedEffect(Unit) { viewModel.cargarReservas() }

    if (showAddDialog) {
        AddReservaDialog(
            onConfirm = { viewModel.añadirReserva(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Reservas",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("Total", reservas.size, Color(0xFF1A3A6B)),
                Triple("Confirmadas", reservas.count { it.estado == "Confirmada" }, Color(0xFF388E3C)),
                Triple("Pendientes", reservas.count { it.estado == "Pendiente" }, Color(0xFFF57C00))
            ).forEach { (label, count, color) ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = color)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }

        TabRow(
            selectedTabIndex = estados.indexOf(filtroEstado),
            containerColor = Color.White,
            contentColor = Color(0xFF1A3A6B)
        ) {
            estados.forEach { estado ->
                Tab(
                    selected = filtroEstado == estado,
                    onClick = { filtroEstado = estado },
                    text = { Text(estado, fontSize = 12.sp) }
                )
            }
        }

        if (reservasFiltradas.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay reservas${if (filtroEstado != "Todas") " $filtroEstado" else ""}.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reservasFiltradas) { reserva ->
                    ReservaCard(
                        reserva = reserva,
                        onCambiarEstado = { viewModel.actualizarEstado(reserva.id, it) },
                        onDelete = { viewModel.eliminarReserva(reserva.id) }
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
            Text("Nueva reserva")
        }
    }
}

@Composable
fun ReservaCard(
    reserva: Reserva,
    onCambiarEstado: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val colorEstado = when (reserva.estado) {
        "Confirmada" -> Color(0xFF388E3C)
        "Pendiente" -> Color(0xFFF57C00)
        "Cancelada" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }
    var showEstadoMenu by remember { mutableStateOf(false) }

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
                        .size(40.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1A3A6B), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(reserva.nombreCliente, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    Text(
                        "${reserva.fecha} · ${reserva.hora} · ${reserva.comensales} pax${if (reserva.mesa.isNotBlank()) " · Mesa ${reserva.mesa}" else ""}",
                        fontSize = 12.sp, color = Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.LightGray)
                }
            }

            if (reserva.notas.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(reserva.notas, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider()
            Spacer(modifier = Modifier.height(10.dp))

            // Teléfono y email con botones de acción
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (reserva.telefono.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${reserva.telefono}"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📞 ${reserva.telefono}", fontSize = 12.sp, maxLines = 1)
                    }
                }
                if (reserva.email.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${reserva.email}"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("✉️ ${reserva.email}", fontSize = 12.sp, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badge de estado — toca para cambiar
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Box {
                    Surface(
                        color = colorEstado,
                        shape = RoundedCornerShape(20.dp),
                        onClick = { showEstadoMenu = true }
                    ) {
                        Text(
                            reserva.estado,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DropdownMenu(expanded = showEstadoMenu, onDismissRequest = { showEstadoMenu = false }) {
                        listOf("Confirmada", "Pendiente", "Cancelada").forEach { estado ->
                            DropdownMenuItem(
                                text = { Text(estado) },
                                onClick = { onCambiarEstado(estado); showEstadoMenu = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddReservaDialog(onConfirm: (Reserva) -> Unit, onDismiss: () -> Unit) {
    var nombreCliente by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var comensales by remember { mutableStateOf("2") }
    var mesa by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var estadoSeleccionado by remember { mutableStateOf("Confirmada") }
    var errorNombre by remember { mutableStateOf("") }
    var errorFecha by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva reserva") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombreCliente,
                    onValueChange = { nombreCliente = it; errorNombre = "" },
                    label = { Text("Nombre del cliente") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorNombre.isNotEmpty(),
                    supportingText = {
                        if (errorNombre.isNotEmpty())
                            Text(errorNombre, color = MaterialTheme.colorScheme.error)
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it; errorFecha = "" },
                        label = { Text("Fecha (dd/MM/yyyy)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = errorFecha.isNotEmpty(),
                        supportingText = {
                            if (errorFecha.isNotEmpty())
                                Text(errorFecha, color = MaterialTheme.colorScheme.error)
                        }
                    )
                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text("Hora (HH:mm)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = comensales,
                        onValueChange = { comensales = it },
                        label = { Text("Comensales") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mesa,
                        onValueChange = { mesa = it },
                        label = { Text("Mesa") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Estado", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Confirmada", "Pendiente", "Cancelada").forEach { estado ->
                        FilterChip(
                            selected = estadoSeleccionado == estado,
                            onClick = { estadoSeleccionado = estado },
                            label = { Text(estado, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (estado) {
                                    "Confirmada" -> Color(0xFF388E3C)
                                    "Pendiente" -> Color(0xFFF57C00)
                                    else -> Color(0xFFD32F2F)
                                },
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                var valido = true
                if (nombreCliente.isBlank()) { errorNombre = "El nombre es obligatorio"; valido = false }
                if (fecha.isBlank()) { errorFecha = "La fecha es obligatoria"; valido = false }
                if (valido) {
                    onConfirm(Reserva(
                        nombreCliente = nombreCliente.trim(),
                        telefono = telefono.trim(),
                        email = email.trim(),
                        fecha = fecha.trim(),
                        hora = hora.trim(),
                        comensales = comensales.toIntOrNull() ?: 2,
                        mesa = mesa.trim(),
                        estado = estadoSeleccionado,
                        notas = notas.trim()
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
package com.example.qtengo.restauracion.ui.reservas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.restauracion.ui.reservas.RestauracionReserva
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReservasScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit,
    viewModel: ReservasViewModel = viewModel()
) {
    val reservas by viewModel.reservasFiltradas.collectAsState()
    val filtro by viewModel.filtro.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var reservaAEditar by remember { mutableStateOf<RestauracionReserva?>(null) }
    var reservaAEliminar by remember { mutableStateOf<RestauracionReserva?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarReservas()
    }

    val error by viewModel.error.collectAsState()
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

        OutlinedTextField(
            value = filtro,
            onValueChange = { viewModel.actualizarFiltro(it) },
            label = { Text("Buscar reserva") },
            placeholder = { Text("Cliente, notas o comensales") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (reservas.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (filtro.isBlank()) {
                        "No hay reservas registradas."
                    } else {
                        "No se encontraron reservas."
                    },
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = reservas,
                    key = { it.id }
                ) { reserva ->
                    ReservaCard(
                        reserva = reserva,
                        onEditar = { reservaAEditar = reserva },
                        onEliminar = { reservaAEliminar = reserva }
                    )
                }
            }
        }

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
            Text("Añadir reserva")
        }

        if (showAddDialog) {
            ReservaDialog(
                titulo = "Añadir reserva",
                reservaInicial = null,
                onDismiss = { showAddDialog = false },
                onGuardar = { nombre, comensales, notas, fecha ->
                    viewModel.agregarReserva(
                        nombre = nombre,
                        comensales = comensales,
                        notas = notas,
                        fecha = fecha
                    )
                    showAddDialog = false
                }
            )
        }

        reservaAEditar?.let { reserva ->
            ReservaDialog(
                titulo = "Editar reserva",
                reservaInicial = reserva,
                onDismiss = { reservaAEditar = null },
                onGuardar = { nombre, comensales, notas, fecha ->
                    viewModel.editarReserva(
                        id = reserva.id,
                        nombre = nombre,
                        comensales = comensales,
                        notas = notas,
                        fecha = fecha
                    )
                    reservaAEditar = null
                }
            )
        }

        reservaAEliminar?.let { reserva ->
            AlertDialog(
                onDismissRequest = { reservaAEliminar = null },
                title = { Text("Eliminar reserva") },
                text = { Text("¿Deseas eliminar la reserva de \"${reserva.nombreCliente}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.eliminarReserva(reserva.id)
                            reservaAEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reservaAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ReservaCard(
    reserva: RestauracionReserva,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = reserva.nombreCliente,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3A6B)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Fecha: ${formatearFecha(reserva.fecha)}",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Text(
                text = "Comensales: ${reserva.comensales}",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Text(
                text = "Notas: ${reserva.notas.ifBlank { "-" }}",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFF1A3A6B)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun ReservaDialog(
    titulo: String,
    reservaInicial: RestauracionReserva?,
    onDismiss: () -> Unit,
    onGuardar: (String, Int, String, Long) -> Unit
) {
    var nombre by remember(reservaInicial) {
        mutableStateOf(reservaInicial?.nombreCliente ?: "")
    }
    var comensalesTexto by remember(reservaInicial) {
        mutableStateOf(
            if (reservaInicial == null) "" else reservaInicial.comensales.toString()
        )
    }
    var notas by remember(reservaInicial) {
        mutableStateOf(reservaInicial?.notas ?: "")
    }

    var errorNombre by remember { mutableStateOf("") }
    var errorComensales by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it; errorNombre = "" },
                    label = { Text("Nombre del cliente *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorNombre.isNotEmpty(),
                    supportingText = {
                        if (errorNombre.isNotEmpty()) {
                            Text(errorNombre, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                OutlinedTextField(
                    value = comensalesTexto,
                    onValueChange = { valor ->
                        if (valor.all { it.isDigit() }) {
                            comensalesTexto = valor
                            errorComensales = ""
                        }
                    },
                    label = { Text("Comensales *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorComensales.isNotEmpty(),
                    supportingText = {
                        if (errorComensales.isNotEmpty()) {
                            Text(errorComensales, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                OutlinedTextField(
                    value = notas,
                    onValueChange = { notas = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    val comensales = comensalesTexto.toIntOrNull() ?: 0

                    if (nombre.trim().isBlank()) {
                        errorNombre = "El nombre es obligatorio"
                        hasError = true
                    }
                    if (comensales <= 0) {
                        errorComensales = "Introduce un número válido"
                        hasError = true
                    }

                    if (!hasError) {
                        onGuardar(
                            nombre.trim(),
                            comensales,
                            notas.trim(),
                            reservaInicial?.fecha ?: System.currentTimeMillis()
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

private fun formatearFecha(timestamp: Long): String {
    if (timestamp <= 0L) return "-"
    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formato.format(Date(timestamp))
}
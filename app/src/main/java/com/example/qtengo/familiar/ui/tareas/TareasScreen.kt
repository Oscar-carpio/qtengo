package com.example.qtengo.familiar.ui.tareas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TareasScreen(
    onBack: () -> Unit,
    viewModel: TareasViewModel = viewModel()
) {
    val tareas by viewModel.tareas.collectAsState()

    var showNuevaTareaDialog by remember { mutableStateOf(false) }
    var tareaAEditar by remember { mutableStateOf<Tarea?>(null) }

    val tareasPendientes = tareas.filter { !it.completada }
    val tareasCompletadas = tareas.filter { it.completada }

    LaunchedEffect(Unit) {
        viewModel.cargarTareas()
    }

    if (showNuevaTareaDialog) {
        NuevaTareaDialog(
            onConfirm = { titulo, descripcion, fecha, prioridad ->
                viewModel.añadirTarea(titulo, descripcion, fecha, prioridad)
                showNuevaTareaDialog = false
            },
            onDismiss = { showNuevaTareaDialog = false }
        )
    }

    tareaAEditar?.let { tarea ->
        EditarTareaDialog(
            tarea = tarea,
            onConfirm = { titulo, descripcion, fecha, prioridad ->
                viewModel.editarTarea(tarea.id, titulo, descripcion, fecha, prioridad)
                tareaAEditar = null
            },
            onDismiss = { tareaAEditar = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        // Header
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
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(
                    text = "Tareas y recordatorios",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${tareasPendientes.size} pendientes · ${tareasCompletadas.size} completadas",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Resumen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A3A6B))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${tareasPendientes.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pendientes",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF388E3C))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${tareasCompletadas.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Completadas",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${tareasPendientes.count { it.prioridad == "Alta" }}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Urgentes",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (tareas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay tareas. ¡Añade una!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (tareasPendientes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Pendientes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A3A6B),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(tareasPendientes) { tarea ->
                        TareaCard(
                            tarea = tarea,
                            onToggle = { viewModel.toggleTarea(tarea.id, it) },
                            onEdit = { tareaAEditar = tarea },
                            onDelete = { viewModel.eliminarTarea(tarea.id) }
                        )
                    }
                }

                if (tareasCompletadas.isNotEmpty()) {
                    item {
                        Text(
                            text = "Completadas",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(tareasCompletadas) { tarea ->
                        TareaCard(
                            tarea = tarea,
                            onToggle = { viewModel.toggleTarea(tarea.id, it) },
                            onEdit = { tareaAEditar = tarea },
                            onDelete = { viewModel.eliminarTarea(tarea.id) }
                        )
                    }
                }
            }
        }

        Button(
            onClick = { showNuevaTareaDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Text(text = "+ Nueva tarea", fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }
}
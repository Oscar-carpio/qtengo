package com.example.qtengo.pyme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import com.example.qtengo.core.domain.models.Task
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.core.ui.screens.TaskViewModel

@Composable
fun TaskScreen(
    viewModel: TaskViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    // En la Pyme, ahora mostramos todas las tareas sin filtro de calendario por petición del usuario
    val tasks by viewModel.tasksByDate.observeAsState(emptyList()) // Ajustar en VM para que cargue todas si es necesario
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        QtengoTopBar(
            title = "Agenda de Tareas",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        val completedCount = tasks.count { it.isCompleted }
        
        // Resumen de progreso
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Progreso de tareas", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = if (tasks.isEmpty()) 0f else (completedCount.toFloat() / tasks.size),
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Color(0xFF1565C0),
                    trackColor = Color(0xFFE3F2FD)
                )
                Text(
                    text = "$completedCount de ${tasks.size} completadas",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(tasks) { task ->
                TaskItemCard(
                    task = task,
                    onToggle = { viewModel.updateTask(task.copy(isCompleted = !task.isCompleted)) },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }

        Button(
            onClick = { showAddTaskDialog = true },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Nueva Tarea")
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, priority ->
                viewModel.insertTask(title, desc, priority, "General") 
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskItemCard(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.isCompleted) Color(0xFF388E3C) else Color.Gray
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (task.isCompleted) Color.Gray else Color.Black
                )
                Text(text = task.description, fontSize = 12.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Text("🗑️")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIA") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descripción") })
                Text("Prioridad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("BAJA", "MEDIA", "ALTA").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if(title.isNotEmpty()) onConfirm(title, desc, priority) }) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

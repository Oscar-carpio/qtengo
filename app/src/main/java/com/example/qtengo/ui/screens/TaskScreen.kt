package com.example.qtengo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.data.local.model.Task
import com.example.qtengo.data.local.model.FinanceMovement
import com.example.qtengo.data.local.model.StockMovement
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = viewModel(),
    onBack: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.observeAsState("")
    val tasks by viewModel.tasksByDate.observeAsState(emptyList())
    val finances by viewModel.financeByDate.observeAsState(emptyList())
    val stockChanges by viewModel.stockByDate.observeAsState(emptyList())
    
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F7FB))
    ) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A3A6B))
                .padding(top = 40.dp, bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Text(text = "←", fontSize = 24.sp, color = Color.White)
            }
            Text(
                text = "Agenda y Actividad",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Calendario Horizontal Simple
        CalendarStrip(
            selectedDate = selectedDate,
            onDateSelected = { viewModel.selectDate(it) }
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            // SECCIÓN TAREAS
            item {
                SectionHeader("Tareas del día", onAdd = { showAddTaskDialog = true })
            }
            
            if (tasks.isEmpty()) {
                item { EmptyState("No hay tareas para este día") }
            } else {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { viewModel.updateTask(task.copy(isCompleted = !task.isCompleted)) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }

            // SECCIÓN FINANZAS
            item { SectionHeader("Actividad Económica") }
            
            if (finances.isEmpty()) {
                item { EmptyState("Sin movimientos económicos") }
            } else {
                items(finances) { movement ->
                    FinanceMiniCard(movement)
                }
            }

            // SECCIÓN STOCK
            item { SectionHeader("Cambios en Almacén") }
            
            if (stockChanges.isEmpty()) {
                item { EmptyState("Sin cambios de stock") }
            } else {
                items(stockChanges) { change ->
                    StockMiniCard(change)
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            date = selectedDate,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, priority ->
                viewModel.insertTask(title, desc, priority, selectedDate)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun CalendarStrip(selectedDate: String, onDateSelected: (String) -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -7) // Mostrar desde hace una semana

    val dates = remember {
        List(21) { // 3 semanas en total
            val d = sdf.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            d
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val dayNum = date.split("/")[0]
            val monthStr = when(date.split("/")[1]) {
                "01" -> "ENE" "02" -> "FEB" "03" -> "MAR" "04" -> "ABR"
                "05" -> "MAY" "06" -> "JUN" "07" -> "JUL" "08" -> "AGO"
                "09" -> "SEP" "10" -> "OCT" "11" -> "NOV" "12" -> "DIC"
                else -> ""
            }

            Column(
                modifier = Modifier
                    .width(55.dp)
                    .background(
                        if (isSelected) Color(0xFF1A3A6B) else Color.White,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = monthStr, fontSize = 10.sp, color = if (isSelected) Color.White else Color.Gray)
                Text(text = dayNum, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Black)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onAdd: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
        if (onAdd != null) {
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF1A3A6B))
            }
        }
    }
}

@Composable
fun EmptyState(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = Color.LightGray,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun FinanceMiniCard(movement: FinanceMovement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(if(movement.type == "INGRESO") Color(0xFF388E3C) else Color(0xFFD32F2F), CircleShape))
            Spacer(Modifier.width(12.dp))
            Text(text = movement.concept, modifier = Modifier.weight(1f), fontSize = 14.sp)
            Text(
                text = "${if(movement.type == "GASTO") "-" else "+"} ${movement.amount}€",
                fontWeight = FontWeight.Bold,
                color = if(movement.type == "INGRESO") Color(0xFF388E3C) else Color(0xFFD32F2F),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun StockMiniCard(change: StockMovement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "📦", fontSize = 16.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = change.productName, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(text = "Nuevo total: ${change.newQuantity.toInt()} ud.", fontSize = 11.sp, color = Color.Gray)
            }
            Text(
                text = "${if(change.quantityChanged > 0) "+" else ""}${change.quantityChanged.toInt()}",
                fontWeight = FontWeight.Bold,
                color = if(change.quantityChanged > 0) Color(0xFF1565C0) else Color(0xFFF57C00),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TaskCard(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                if (task.description.isNotEmpty()) {
                    Text(text = task.description, fontSize = 12.sp, color = Color.Gray)
                }
            }
            IconButton(onClick = onDelete) {
                Text("🗑️")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(date: String, onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIA") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea ($date)") },
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

package com.example.qtengo.pyme.ui.tareas

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.Task
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.core.ui.screens.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla principal de la Agenda de Tareas para PYME.
 * Gestiona tareas con prioridades, estados y filtros avanzados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasPantalla(
    viewModel: TaskViewModel = viewModel(),
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onChangeProfile: () -> Unit
) {
    val allTasks by viewModel.allTasks.observeAsState(emptyList())
    val selectedDate by viewModel.selectedDate.observeAsState("")
    val creationFilter by viewModel.creationFilter.observeAsState("Todas")
    
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filtersExpanded by remember { mutableStateOf(false) }
    
    // Por defecto ver todas las tareas
    var statusFilter by remember { mutableStateOf("Todas") } 
    var dateFilterEnabled by remember { mutableStateOf(false) }

    // Estados para el filtro de creación (Elección de Mes y Año específicos)
    val months = listOf("Todos", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val currentYearInt = Calendar.getInstance().get(Calendar.YEAR)
    val yearsList = listOf("Todos") + (currentYearInt downTo 2023).map { it.toString() }
    
    var filterMonth by remember { mutableIntStateOf(0) } // 0 = Todos
    var filterYear by remember { mutableStateOf("Todos") }
    
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val today = Date()

    // Lógica de filtrado y ordenación consolidada
    val filteredTasks = allTasks.filter { task ->
        // Búsqueda por texto
        val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) || 
                           task.description.contains(searchQuery, ignoreCase = true)
        
        // Filtro por estado
        val matchesStatus = when (statusFilter) {
            "Pendientes" -> !task.isCompleted
            "Realizadas" -> task.isCompleted
            else -> true
        }

        // Filtro opcional por fecha programada
        val matchesDate = if (dateFilterEnabled) task.date == selectedDate else true

        // Filtro por fecha de creación (Mes y Año seleccionables simultáneamente)
        val matchesCreation = run {
            val createdDate = try { sdf.parse(task.createdAt) } catch (_: Exception) { null }
            if (createdDate == null) {
                filterMonth == 0 && filterYear == "Todos"
            } else {
                val cal = Calendar.getInstance().apply { time = createdDate }
                val mMatch = if (filterMonth == 0) true else cal.get(Calendar.MONTH) == (filterMonth - 1)
                val yMatch = if (filterYear == "Todos") true else cal.get(Calendar.YEAR).toString() == filterYear
                mMatch && yMatch
            }
        }

        matchesSearch && matchesStatus && matchesDate && matchesCreation
    }.sortedWith(
        // 1. Tareas con fecha van ARRIBA de las que no tienen fecha
        compareBy<Task> { it.date.isEmpty() }
        // 2. Orden por prioridad interna (aunque no se muestre): ALTA > MEDIA > BAJA
        .thenBy { 
            when (it.priority.uppercase()) {
                "ALTA" -> 0
                "MEDIA" -> 1
                "BAJA" -> 2
                else -> 3
            }
        }
        // 3. Más recientes primero por fecha de creación
        .thenByDescending { it.createdAt }
    )

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB))) {
        QtengoTopBar(
            title = "Agenda de Tareas",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        // Panel de Filtros
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { filtersExpanded = !filtersExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, null, tint = Color(0xFF1A3A6B))
                        Spacer(Modifier.width(8.dp))
                        Text("Buscador y Filtros", fontWeight = FontWeight.Bold, color = Color(0xFF1A3A6B))
                    }
                    Icon(if (filtersExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
                }

                AnimatedVisibility(visible = filtersExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Buscar...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Column {
                            Text("Estado:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Todas", "Pendientes", "Realizadas").forEach { s ->
                                    FilterChip(
                                        selected = statusFilter == s,
                                        onClick = { statusFilter = s },
                                        label = { Text(s) }
                                    )
                                }
                            }
                        }

                        Column {
                            Text("Filtrar por creación (Mes / Año):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                // Selector de Mes específico
                                ExposedDropdownMenuBox(
                                    expanded = monthExpanded,
                                    onExpandedChange = { monthExpanded = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = months[filterMonth],
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Mes") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                                    )
                                    ExposedDropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                                        months.forEachIndexed { index, name ->
                                            DropdownMenuItem(
                                                text = { Text(name) },
                                                onClick = { filterMonth = index; monthExpanded = false }
                                            )
                                        }
                                    }
                                }
                                
                                // Selector de Año específico
                                ExposedDropdownMenuBox(
                                    expanded = yearExpanded,
                                    onExpandedChange = { yearExpanded = it },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = filterYear,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Año") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                                    )
                                    ExposedDropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                                        yearsList.forEach { year ->
                                            DropdownMenuItem(
                                                text = { Text(year) },
                                                onClick = { filterYear = year; yearExpanded = false }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = dateFilterEnabled, onCheckedChange = { dateFilterEnabled = it })
                                Text("Filtrar por Fecha Programada", fontSize = 14.sp)
                            }
                            
                            if (dateFilterEnabled) {
                                Button(
                                    onClick = {
                                        val calendar = Calendar.getInstance()
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                calendar.set(year, month, dayOfMonth)
                                                viewModel.selectDate(sdf.format(calendar.time))
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3A6B))
                                ) {
                                    Icon(Icons.Default.CalendarMonth, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Fecha Programada: $selectedDate")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Resumen rápido
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResumenCardTareas(title = "Visibles", value = "${filteredTasks.size}", color = Color(0xFF1565C0), modifier = Modifier.weight(1f))
            ResumenCardTareas(title = "Pendientes", value = "${allTasks.count { !it.isCompleted }}", color = Color(0xFFD32F2F), modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        if (filteredTasks.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay tareas coincidentes", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredTasks) { task ->
                    TareaCardItem(
                        task = task,
                        today = today,
                        onToggle = { viewModel.updateTask(task.copy(isCompleted = !task.isCompleted)) },
                        onEdit = { taskToEdit = task },
                        onDelete = { viewModel.deleteTask(task) }
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
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Nueva Tarea")
        }
    }

    if (showAddDialog) {
        DialogoTarea(
            titulo = "Nueva Tarea",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, priority, date ->
                viewModel.insertTask(title, desc, priority, date)
                showAddDialog = false
            }
        )
    }

    taskToEdit?.let { task ->
        DialogoTarea(
            titulo = "Editar Tarea",
            task = task,
            onDismiss = { taskToEdit = null },
            onConfirm = { title, desc, priority, date ->
                viewModel.updateTask(task.copy(title = title, description = desc, priority = priority, date = date))
                taskToEdit = null
            }
        )
    }
}

@Composable
fun ResumenCardTareas(title: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier.height(70.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = title, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun TareaCardItem(task: Task, today: Date, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val scheduledDate = try { sdf.parse(task.date) } catch (_: Exception) { null }
    
    val cal = Calendar.getInstance().apply { 
        time = today
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val isExpired = !task.isCompleted && scheduledDate != null && scheduledDate.before(cal.time)

    val backgroundColor = when {
        task.isCompleted -> Color(0xFFF1F1F1)
        isExpired -> Color(0xFFFFEBEE)
        task.priority.uppercase() == "ALTA" -> Color(0xFFFFF3E0)
        else -> Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isExpired) BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)) else null
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.isCompleted) Color(0xFF388E3C) else if (isExpired) Color.Red else Color.Gray
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title, 
                        fontWeight = FontWeight.Bold, 
                        color = if (task.isCompleted) Color.Gray else if (isExpired) Color(0xFFB71C1C) else Color.Black,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isExpired) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description, 
                        fontSize = 12.sp, 
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (task.date.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "📅 Programada: ${task.date}", 
                        fontSize = 10.sp, 
                        color = if (isExpired) Color.Red else Color.Gray,
                        fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp)) }
            }
        }
    }
}

@Composable
fun DialogoTarea(
    titulo: String,
    task: Task? = null,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var desc by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: "MEDIA") }
    var scheduledDate by remember { mutableStateOf(task?.date ?: "") }
    
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("¿Qué hay que hacer?") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Detalles (opcional)") })
                
                // Selector de fecha programada corregido para ser clicable
                Box(modifier = Modifier.fillMaxWidth().clickable {
                    val calendar = Calendar.getInstance()
                    if (scheduledDate.isNotEmpty()) {
                        try {
                            val parts = scheduledDate.split("/")
                            calendar.set(Calendar.DAY_OF_MONTH, parts[0].toInt())
                            calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                            calendar.set(Calendar.YEAR, parts[2].toInt())
                        } catch (_: Exception) {}
                    }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth)
                            scheduledDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    OutlinedTextField(
                        value = if (scheduledDate.isEmpty()) "Sin fecha" else scheduledDate,
                        onValueChange = { },
                        label = { Text("Fecha programada") },
                        readOnly = true,
                        enabled = false, // Necesario para que el clic lo capture el Box padre
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (scheduledDate.isNotEmpty()) {
                                    IconButton(onClick = { scheduledDate = "" }) {
                                        Icon(Icons.Default.Clear, null, tint = Color.Red)
                                    }
                                }
                                Icon(Icons.Default.CalendarMonth, null)
                            }
                        }
                    )
                }

                Text("Prioridad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("BAJA", "MEDIA", "ALTA").forEach { p ->
                        FilterChip(
                            selected = priority.uppercase() == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }

                if (task != null && task.createdAt.isNotEmpty()) {
                    Text(text = "Creada el: ${task.createdAt}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            Button(onClick = { if(title.isNotEmpty()) onConfirm(title, desc, priority, scheduledDate) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

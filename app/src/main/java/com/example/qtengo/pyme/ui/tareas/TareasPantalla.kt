/**
 * Pantalla principal de la Agenda de Tareas.
 * 
 * Gestiona el ciclo de vida de la UI para la visualización, filtrado y gestión 
 * de tareas diarias. Permite organizar el flujo de trabajo mediante prioridades 
 * y estados de cumplimiento.
 */
package com.example.qtengo.pyme.ui.tareas

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qtengo.core.domain.models.Task
import com.example.qtengo.core.ui.components.QtengoTopBar
import com.example.qtengo.core.ui.screens.TaskViewModel
import com.example.qtengo.pyme.ui.filtros.FiltrosTareas
import com.example.qtengo.pyme.ui.tareas.components.DialogoTarea
import com.example.qtengo.pyme.ui.tareas.components.ResumenCardTareas
import com.example.qtengo.pyme.ui.tareas.components.TareaCardItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable principal del módulo de Tareas.
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
    
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    var statusFilter by remember { mutableStateOf("Todas") } 
    var dateFilterEnabled by remember { mutableStateOf(false) }
    var filterMonth by remember { mutableIntStateOf(0) }
    var filterYear by remember { mutableStateOf("Todos") }
    var sortBy by remember { mutableStateOf("Nombre") }
    var isAscending by remember { mutableStateOf(true) }
    
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val today = Date()

    val filteredTasks = allTasks.filter { task ->
        val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) || 
                           task.description.contains(searchQuery, ignoreCase = true)
        
        val matchesStatus = when (statusFilter) {
            "Pendientes" -> !task.isCompleted
            "Realizadas" -> task.isCompleted
            else -> true
        }

        val matchesDate = if (dateFilterEnabled) task.date == selectedDate else true

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
    }.sortedWith { t1, t2 ->
        val res = when (sortBy) {
            "Nombre" -> t1.title.compareTo(t2.title, ignoreCase = true)
            else -> 0
        }
        if (res != 0) {
            if (isAscending) res else -res
        } else {
            compareBy<Task> { it.date.isEmpty() }
                .thenBy { 
                    when (it.priority.uppercase()) {
                        "ALTA" -> 0
                        "MEDIA" -> 1
                        "BAJA" -> 2
                        else -> 3
                    }
                }
                .thenByDescending { it.createdAt }
                .compare(t1, t2)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7FB))) {
        QtengoTopBar(
            title = "Agenda de Tareas",
            onBack = onBack,
            onLogout = onLogout,
            onChangeProfile = onChangeProfile
        )

        // Panel de búsqueda y filtrado unificado en la carpeta filtros/
        FiltrosTareas(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            statusFilter = statusFilter,
            onStatusChange = { statusFilter = it },
            filterMonth = filterMonth,
            onMonthChange = { filterMonth = it },
            filterYear = filterYear,
            onYearChange = { filterYear = it },
            dateFilterEnabled = dateFilterEnabled,
            onDateFilterToggle = { dateFilterEnabled = it },
            selectedDate = selectedDate,
            onDateSelected = { viewModel.selectDate(it) },
            sortBy = sortBy,
            isAscending = isAscending,
            onSortChange = { s, a -> sortBy = s; isAscending = a }
        )

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

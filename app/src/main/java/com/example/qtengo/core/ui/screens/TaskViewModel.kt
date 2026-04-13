package com.example.qtengo.core.ui.screens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.qtengo.core.domain.models.Task
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.domain.models.StockMovement
import com.example.qtengo.core.data.repositories.TaskRepository
import com.example.qtengo.core.data.repositories.FinanceRepository
import com.example.qtengo.core.data.repositories.StockMovementRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel que centraliza la actividad diaria y la agenda de tareas.
 * 
 * Gestiona la sincronización entre tareas programadas, movimientos financieros
 * y cambios de stock para una fecha específica en el perfil PYME.
 */
class TaskViewModel(
    private val taskRepository: TaskRepository = TaskRepository(),
    private val financeRepository: FinanceRepository = FinanceRepository(),
    private val stockRepository: StockMovementRepository = StockMovementRepository()
) : ViewModel() {

    private val _selectedDate = MutableLiveData<String>()
    
    /**
     * Fecha actualmente seleccionada en el calendario (formato dd/MM/yyyy).
     */
    val selectedDate: LiveData<String> = _selectedDate

    private val _creationFilter = MutableLiveData<String>("Todas")
    val creationFilter: LiveData<String> = _creationFilter

    /**
     * Lista reactiva de todas las tareas asociadas al perfil PYME.
     */
    val allTasks: LiveData<List<Task>> = taskRepository.getByProfileFlow("PYME").asLiveData()

    /**
     * Tareas programadas para la fecha seleccionada en [_selectedDate].
     */
    val tasksByDate: LiveData<List<Task>> = _selectedDate.switchMap { date ->
        taskRepository.getByDate(date, "PYME").asLiveData()
    }

    /**
     * Flujo de caja registrado para la fecha seleccionada.
     */
    val financeByDate: LiveData<List<FinanceMovement>> = _selectedDate.switchMap { date ->
        financeRepository.getByDate(date, "PYME").asLiveData()
    }

    /**
     * Historial de movimientos de inventario ocurridos en la fecha seleccionada.
     */
    val stockByDate: LiveData<List<StockMovement>> = _selectedDate.switchMap { date ->
        stockRepository.getMovementsByDate(date, "PYME").asLiveData()
    }

    init {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        _selectedDate.value = today
    }

    /**
     * Actualiza la fecha de consulta global para los flujos de tareas, finanzas y stock.
     * @param date Nueva fecha seleccionada.
     */
    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    /**
     * Establece el filtro de visualización según la fecha de creación.
     * @param filter Valores posibles: "Todas", "Mes", "Año".
     */
    fun setCreationFilter(filter: String) {
        _creationFilter.value = filter
    }

    /**
     * Crea y persiste una nueva tarea.
     * Automáticamente asigna la fecha de creación actual y el perfil PYME.
     */
    fun insertTask(title: String, description: String, priority: String, date: String) = viewModelScope.launch {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val task = Task(
            title = title, 
            description = description, 
            priority = priority, 
            profile = "PYME",
            date = date,
            createdAt = today
        )
        taskRepository.insert(task)
    }

    /**
     * Actualiza una tarea existente (título, descripción, prioridad o estado de completado).
     */
    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.update(task)
    }

    /**
     * Elimina permanentemente una tarea de la base de datos.
     */
    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepository.delete(task.id)
    }
}

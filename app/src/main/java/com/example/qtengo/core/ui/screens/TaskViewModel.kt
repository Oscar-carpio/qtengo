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
 * ViewModel para gestionar las tareas y la actividad diaria usando Firebase.
 */
class TaskViewModel(
    private val taskRepository: TaskRepository = TaskRepository(),
    private val financeRepository: FinanceRepository = FinanceRepository(),
    private val stockRepository: StockMovementRepository = StockMovementRepository()
) : ViewModel() {

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    private val _creationFilter = MutableLiveData<String>("Todas") // "Todas", "Mes", "Año"
    val creationFilter: LiveData<String> = _creationFilter

    /**
     * Lista de todas las tareas del perfil para filtrar por creación si es necesario.
     */
    val allTasks: LiveData<List<Task>> = taskRepository.getByProfileFlow("PYME").asLiveData()

    /**
     * Lista de tareas para la fecha seleccionada (programadas).
     */
    val tasksByDate: LiveData<List<Task>> = _selectedDate.switchMap { date ->
        taskRepository.getByDate(date, "PYME").asLiveData()
    }

    /**
     * Lista de movimientos financieros para la fecha seleccionada.
     */
    val financeByDate: LiveData<List<FinanceMovement>> = _selectedDate.switchMap { date ->
        financeRepository.getByDate(date, "PYME").asLiveData()
    }

    /**
     * Lista de movimientos de stock para la fecha seleccionada.
     */
    val stockByDate: LiveData<List<StockMovement>> = _selectedDate.switchMap { date ->
        stockRepository.getMovementsByDate(date, "PYME").asLiveData()
    }

    init {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        _selectedDate.value = today
    }

    /**
     * Cambia la fecha de consulta en el calendario.
     */
    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun setCreationFilter(filter: String) {
        _creationFilter.value = filter
    }

    /**
     * Inserta una nueva tarea vinculada a una fecha y perfil específicos.
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
     * Actualiza el estado o contenido de una tarea.
     */
    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.update(task)
    }

    /**
     * Elimina una tarea de la base de datos.
     */
    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepository.delete(task.id)
    }
}

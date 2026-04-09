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
class TaskViewModel : ViewModel() {
    private val taskRepository = TaskRepository()
    private val financeRepository = FinanceRepository()
    private val stockRepository = StockMovementRepository()

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    /**
     * Lista de tareas para la fecha seleccionada.
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

    /**
     * Inserta una nueva tarea vinculada a una fecha y perfil específicos.
     */
    fun insertTask(title: String, description: String, priority: String, date: String) = viewModelScope.launch {
        val task = Task(
            title = title, 
            description = description, 
            priority = priority, 
            profile = "PYME",
            date = date
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
        taskRepository.delete(task.id) // Corregido: pasar solo el ID
    }
}

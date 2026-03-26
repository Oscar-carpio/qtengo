package com.example.qtengo.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.local.database.AppDatabase
import com.example.qtengo.data.local.model.Task
import com.example.qtengo.data.local.model.FinanceMovement
import com.example.qtengo.data.local.model.StockMovement
import com.example.qtengo.data.repository.TaskRepository
import com.example.qtengo.data.repository.FinanceRepository
import com.example.qtengo.data.repository.StockMovementRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para gestionar las tareas y el calendario de actividad.
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskRepository: TaskRepository
    private val financeRepository: FinanceRepository
    private val stockRepository: StockMovementRepository

    private val _selectedDate = MutableLiveData<String>()
    val selectedDate: LiveData<String> = _selectedDate

    val tasksByDate: LiveData<List<Task>> = _selectedDate.switchMap { date ->
        taskRepository.getByDate(date, "PYME")
    }

    val financeByDate: LiveData<List<FinanceMovement>> = _selectedDate.switchMap { date ->
        financeRepository.getByDate(date, "PYME")
    }

    val stockByDate: LiveData<List<StockMovement>> = _selectedDate.switchMap { date ->
        stockRepository.getMovementsByDate(date, "PYME")
    }

    init {
        val db = AppDatabase.getDatabase(application)
        taskRepository = TaskRepository(db.taskDao())
        financeRepository = FinanceRepository(db.financeDao())
        stockRepository = StockMovementRepository(db.stockMovementDao())
        
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        _selectedDate.value = today
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

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

    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.update(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepository.delete(task)
    }
}

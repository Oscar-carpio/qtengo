package com.example.qtengo.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.database.AppDatabase
import com.example.qtengo.data.model.Task
import com.example.qtengo.data.repository.TaskRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar las tareas pendientes de la Pyme.
 */
class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    lateinit var tasks: LiveData<List<Task>>

    init {
        val dao = AppDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(dao)
        tasks = repository.getByProfile("PYME")
    }

    fun insert(title: String, description: String, priority: String) = viewModelScope.launch {
        val task = Task(title = title, description = description, priority = priority, profile = "PYME")
        repository.insert(task)
    }

    fun update(task: Task) = viewModelScope.launch {
        repository.update(task)
    }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
    }
}

package com.example.qtengo.data.repository

import androidx.lifecycle.LiveData
import com.example.qtengo.data.dao.TaskDao
import com.example.qtengo.data.model.Task

/**
 * Repositorio para la gestión de tareas pendientes.
 */
class TaskRepository(private val taskDao: TaskDao) {
    fun getByProfile(profile: String): LiveData<List<Task>> = taskDao.getByProfile(profile)
    suspend fun insert(task: Task) = taskDao.insert(task)
    suspend fun update(task: Task) = taskDao.update(task)
    suspend fun delete(task: Task) = taskDao.delete(task)
}

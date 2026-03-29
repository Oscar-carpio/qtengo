package com.example.qtengo.core.data.repositories

import androidx.lifecycle.LiveData
import com.example.qtengo.core.data.database.dao.TaskDao
import com.example.qtengo.core.domain.models.Task

class TaskRepository(private val taskDao: TaskDao) {
    fun getByProfile(profile: String): LiveData<List<Task>> = taskDao.getByProfile(profile)
    fun getByDate(date: String, profile: String): LiveData<List<Task>> = taskDao.getByDate(date, profile)
    suspend fun insert(task: Task) = taskDao.insert(task)
    suspend fun update(task: Task) = taskDao.update(task)
    suspend fun delete(task: Task) = taskDao.delete(task)
}

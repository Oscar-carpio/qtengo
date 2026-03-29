package com.example.qtengo.core.data.repositories

import androidx.lifecycle.LiveData
import com.example.qtengo.core.data.database.dao.EmployeeDao
import com.example.qtengo.core.domain.models.Employee

class EmployeeRepository(private val employeeDao: EmployeeDao) {
    fun getByProfile(profile: String): LiveData<List<Employee>> = employeeDao.getByProfile(profile)
    suspend fun insert(employee: Employee) = employeeDao.insert(employee)
    suspend fun update(employee: Employee) = employeeDao.update(employee)
    suspend fun delete(employee: Employee) = employeeDao.delete(employee)
}

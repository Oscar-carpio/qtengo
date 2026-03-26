package com.example.qtengo.data.repository

import androidx.lifecycle.LiveData
import com.example.qtengo.data.local.dao.EmployeeDao
import com.example.qtengo.data.local.model.Employee

/**
 * Repositorio para gestionar la comunicación entre el ViewModel y el DAO de empleados.
 */
class EmployeeRepository(private val employeeDao: EmployeeDao) {

    // Obtiene la lista de empleados filtrada por perfil
    fun getByProfile(profile: String): LiveData<List<Employee>> {
        return employeeDao.getByProfile(profile)
    }

    // Inserta un nuevo empleado en la base de datos de forma asíncrona
    suspend fun insert(employee: Employee) {
        employeeDao.insert(employee)
    }

    // Actualiza un empleado existente
    suspend fun update(employee: Employee) {
        employeeDao.update(employee)
    }

    // Eliminar un empleado
    suspend fun delete(employee: Employee) {
        employeeDao.delete(employee)
    }
}

package com.example.qtengo.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.database.AppDatabase
import com.example.qtengo.data.model.Employee
import com.example.qtengo.data.repository.EmployeeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la lógica de la interfaz de empleados.
 */
class EmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmployeeRepository
    private var currentProfile: String = "PYME"

    // Lista de empleados observada por la interfaz
    lateinit var employees: LiveData<List<Employee>>

    init {
        val dao = AppDatabase.getDatabase(application).employeeDao()
        repository = EmployeeRepository(dao)
        loadProfile("PYME")
    }

    /**
     * Filtra la lista de empleados según el perfil activo.
     */
    fun loadProfile(profile: String) {
        currentProfile = profile
        employees = repository.getByProfile(profile)
    }

    /**
     * Inserta un nuevo empleado en la base de datos.
     */
    fun insert(name: String, position: String, salary: Double, phone: String, startDate: String) = viewModelScope.launch {
        val employee = Employee(
            name = name,
            position = position,
            salary = salary,
            phone = phone,
            startDate = startDate,
            profile = currentProfile
        )
        repository.insert(employee)
    }

    /**
     * Elimina un empleado de la base de datos.
     */
    fun delete(employee: Employee) = viewModelScope.launch {
        repository.delete(employee)
    }
}

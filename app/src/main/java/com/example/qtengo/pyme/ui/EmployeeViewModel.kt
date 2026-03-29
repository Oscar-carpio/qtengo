package com.example.qtengo.pyme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.qtengo.core.data.database.AppDatabase
import com.example.qtengo.core.domain.models.Employee
import com.example.qtengo.core.data.repositories.EmployeeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la lógica de la plantilla de empleados en el módulo Pyme.
 */
class EmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmployeeRepository
    private var currentProfile: String = "PYME"

    lateinit var employees: LiveData<List<Employee>>

    init {
        val dao = AppDatabase.getDatabase(application).employeeDao()
        repository = EmployeeRepository(dao)
        loadProfile("PYME")
    }

    /**
     * Carga la lista de empleados filtrada por el perfil activo.
     */
    fun loadProfile(profile: String) {
        currentProfile = profile
        employees = repository.getByProfile(profile)
    }

    /**
     * Registra un nuevo empleado en el sistema.
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
     * Elimina a un empleado de la base de datos.
     */
    fun delete(employee: Employee) = viewModelScope.launch {
        repository.delete(employee)
    }
}

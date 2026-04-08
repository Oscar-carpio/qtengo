package com.example.qtengo.pyme.ui

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.Employee
import com.example.qtengo.core.data.repositories.EmployeeRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la lógica de la plantilla de empleados en el módulo Pyme usando Firebase.
 */
class EmployeeViewModel : ViewModel() {

    private val repository = EmployeeRepository()
    private val profileFilter = MutableLiveData<String>()

    /**
     * Carga la lista de empleados filtrada por el perfil activo.
     */
    val employees: LiveData<List<Employee>> = profileFilter.switchMap { profile ->
        repository.getByProfileFlow(profile).asLiveData()
    }

    init {
        loadProfile("PYME")
    }

    fun loadProfile(profile: String) {
        profileFilter.value = profile
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
            profile = profileFilter.value ?: "PYME"
        )
        repository.insert(employee)
    }

    /**
     * Elimina a un empleado de la base de datos.
     */
    fun delete(employee: Employee) = viewModelScope.launch {
        repository.delete(employee.id)
    }
}

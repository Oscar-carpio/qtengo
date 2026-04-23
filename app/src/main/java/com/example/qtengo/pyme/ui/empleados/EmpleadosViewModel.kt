/**
 * ViewModel encargado de la gestión de la plantilla de empleados y su impacto financiero.
 */
package com.example.qtengo.pyme.ui.empleados

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.Employee
import com.example.qtengo.core.data.repositories.EmployeeRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EmpleadosViewModel(
    private val repositorioEmpleados: EmployeeRepository = EmployeeRepository()
) : ViewModel() {

    private val filtroPerfil = MutableLiveData<String>()

    val employees: LiveData<List<Employee>> = filtroPerfil.switchMap { perfil ->
        repositorioEmpleados.getByProfileFlow(perfil).asLiveData()
    }

    init {
        cargarPerfil("PYME")
    }

    fun cargarPerfil(perfil: String) {
        filtroPerfil.value = perfil
    }

    fun insertar(nombre: String, cargo: String, salario: Double, telefono: String, email: String, detalles: String) = viewModelScope.launch {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val empleado = Employee(
            name = nombre,
            position = cargo,
            salary = salario,
            phone = telefono,
            email = email,
            startDate = fechaActual,
            profile = filtroPerfil.value ?: "PYME",
            details = detalles
        )
        repositorioEmpleados.insert(empleado)
    }

    fun actualizar(empleado: Employee) = viewModelScope.launch {
        repositorioEmpleados.update(empleado)
    }

    fun eliminar(empleadoId: String) = viewModelScope.launch {
        repositorioEmpleados.delete(empleadoId)
    }
}

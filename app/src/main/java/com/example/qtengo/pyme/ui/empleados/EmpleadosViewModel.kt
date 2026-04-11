package com.example.qtengo.pyme.ui.empleados

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.Employee
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.data.repositories.EmployeeRepository
import com.example.qtengo.core.data.repositories.FinanceRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para gestionar la lógica de la plantilla de empleados y su impacto financiero.
 */
class EmpleadosViewModel(
    private val repositorioEmpleados: EmployeeRepository = EmployeeRepository(),
    private val repositorioFinanzas: FinanceRepository = FinanceRepository()
) : ViewModel() {

    private val filtroPerfil = MutableLiveData<String>()

    /**
     * Lista de empleados observada desde la base de datos.
     */
    val employees: LiveData<List<Employee>> = filtroPerfil.switchMap { perfil ->
        repositorioEmpleados.getByProfileFlow(perfil).asLiveData()
    }

    init {
        loadProfile("PYME")
    }

    fun loadProfile(perfil: String) {
        filtroPerfil.value = perfil
    }

    /**
     * Registra un nuevo empleado y genera automáticamente un gasto de nómina.
     */
    fun insert(nombre: String, cargo: String, salario: Double, telefono: String, email: String, detalles: String) = viewModelScope.launch {
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
        
        // Registrar la nómina como un gasto en Finanzas
        registrarNominaComoGasto(nombre, salario)
    }

    /**
     * Actualiza la información de un empleado.
     */
    fun update(empleado: Employee) = viewModelScope.launch {
        repositorioEmpleados.update(empleado)
    }

    /**
     * Elimina a un empleado.
     */
    fun delete(empleadoId: String) = viewModelScope.launch {
        repositorioEmpleados.delete(empleadoId)
    }

    /**
     * Función privada para registrar el salario como un movimiento de gasto.
     */
    private suspend fun registrarNominaComoGasto(nombreEmpleado: String, salario: Double) {
        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val movimiento = FinanceMovement(
            concept = "Nómina de $nombreEmpleado",
            amount = salario,
            type = "GASTO",
            date = fechaActual,
            profile = filtroPerfil.value ?: "PYME"
        )
        repositorioFinanzas.insert(movimiento)
    }
}

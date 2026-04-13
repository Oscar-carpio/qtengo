/**
 * ViewModel encargado de la gestión de la plantilla de empleados y su impacto financiero.
 * 
 * Centraliza la lógica de contratación, actualización de fichas y la generación 
 * automática de asientos de nómina en el módulo de finanzas.
 */
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
 * ViewModel que conecta la UI de Empleados con Firestore.
 * 
 * @param repositorioEmpleados Repositorio para operaciones CRUD de empleados.
 * @param repositorioFinanzas Repositorio para registrar gastos derivados (nóminas).
 */
class EmpleadosViewModel(
    private val repositorioEmpleados: EmployeeRepository = EmployeeRepository(),
    private val repositorioFinanzas: FinanceRepository = FinanceRepository()
) : ViewModel() {

    private val filtroPerfil = MutableLiveData<String>()

    /**
     * Lista reactiva de empleados, filtrada por el perfil actual (ej: PYME).
     */
    val employees: LiveData<List<Employee>> = filtroPerfil.switchMap { perfil ->
        repositorioEmpleados.getByProfileFlow(perfil).asLiveData()
    }

    init {
        loadProfile("PYME")
    }

    /**
     * Cambia el contexto de perfil para cargar los empleados correspondientes.
     */
    fun loadProfile(perfil: String) {
        filtroPerfil.value = perfil
    }

    /**
     * Registra un nuevo empleado en el sistema.
     * Al insertar, genera automáticamente un movimiento de gasto en Finanzas.
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
        
        // Efecto secundario: registro contable
        registrarNominaComoGasto(nombre, salario)
    }

    /**
     * Actualiza la información profesional o de contacto de un empleado.
     */
    fun update(empleado: Employee) = viewModelScope.launch {
        repositorioEmpleados.update(empleado)
    }

    /**
     * Elimina la ficha de un empleado del sistema.
     */
    fun delete(empleadoId: String) = viewModelScope.launch {
        repositorioEmpleados.delete(empleadoId)
    }

    /**
     * Registra internamente el salario como un movimiento de gasto.
     * Se ejecuta de forma privada tras una inserción exitosa.
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

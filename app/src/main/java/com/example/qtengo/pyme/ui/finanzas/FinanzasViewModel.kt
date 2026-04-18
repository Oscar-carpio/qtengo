package com.example.qtengo.pyme.ui.finanzas

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.data.repositories.FinanceRepository
import com.example.qtengo.core.data.repositories.EmployeeRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica financiera de la empresa.
 */
class FinanzasViewModel(
    private val repository: FinanceRepository = FinanceRepository(),
    private val employeeRepository: EmployeeRepository = EmployeeRepository()
) : ViewModel() {

    /**
     * Lista reactiva de movimientos financieros. Combina registros de caja con gastos de personal.
     */
    val movements: LiveData<List<FinanceMovement>> = combine(
        repository.getAllFlow("PYME"),
        employeeRepository.getByProfileFlow("PYME")
    ) { finanzas, empleados ->
        val movimientosNominas = empleados.map { emp ->
            FinanceMovement(
                id = "nomina_${emp.id}",
                concept = "Nómina de ${emp.name}",
                amount = emp.salary,
                type = "GASTO",
                date = "Mensual",
                profile = "PYME",
                notes = "Cargo generado automáticamente por empleado activo"
            )
        }
        finanzas + movimientosNominas
    }.asLiveData(viewModelScope.coroutineContext) // Usar el contexto del ViewModel para mayor estabilidad en tests

    /**
     * Calcula el total de entradas registradas.
     */
    val totalIngresos: LiveData<Double?> = movements.map { list ->
        list.filter { it.type == "INGRESO" }.sumOf { it.amount }
    }

    /**
     * Calcula el total de salidas registradas.
     */
    val totalGastos: LiveData<Double?> = movements.map { list ->
        list.filter { it.type == "GASTO" }.sumOf { it.amount }
    }

    /**
     * Registra un nuevo movimiento en Firebase.
     */
    fun insert(movement: FinanceMovement) = viewModelScope.launch {
        repository.insert(movement)
    }

    /**
     * Elimina un movimiento si no es una nómina autogenerada.
     */
    fun delete(movementId: String) = viewModelScope.launch {
        if (!movementId.startsWith("nomina_")) {
            repository.delete(movementId)
        }
    }
}

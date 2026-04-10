package com.example.qtengo.pyme.ui.finanzas

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.data.repositories.FinanceRepository
import com.example.qtengo.core.data.repositories.EmployeeRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica financiera combinando movimientos reales y nóminas de empleados.
 */
class FinanzasViewModel(
    private val repository: FinanceRepository = FinanceRepository(),
    private val employeeRepository: EmployeeRepository = EmployeeRepository()
) : ViewModel() {

    /**
     * Combina los movimientos de la base de datos con los salarios de los empleados
     * para que las nóminas se reflejen automáticamente en la lista de gastos.
     */
    val movements: LiveData<List<FinanceMovement>> = combine(
        repository.getAllFlow("PYME"),
        employeeRepository.getByProfileFlow("PYME")
    ) { finanzas, empleados ->
        // Convertimos cada empleado en un movimiento de tipo GASTO (Nómina)
        val movimientosNominas = empleados.map { emp ->
            FinanceMovement(
                id = "nomina_${emp.id}",
                concept = "Nómina de ${emp.name}",
                amount = emp.salary,
                type = "GASTO",
                date = "Mensual", // Indicador de que es un gasto recurrente de nómina
                profile = "PYME"
            )
        }
        finanzas + movimientosNominas
    }.asLiveData()

    val totalIngresos: LiveData<Double?> = movements.map { list ->
        val suma = list.filter { it.type == "INGRESO" }.sumOf { it.amount }
        if (suma > 0) suma else 0.0
    }

    val totalGastos: LiveData<Double?> = movements.map { list ->
        val suma = list.filter { it.type == "GASTO" }.sumOf { it.amount }
        if (suma > 0) suma else 0.0
    }

    /**
     * Registra un nuevo movimiento financiero manual.
     */
    fun insert(movement: FinanceMovement) = viewModelScope.launch {
        repository.insert(movement)
    }

    /**
     * Elimina un movimiento financiero (solo si no es una nómina virtual).
     */
    fun delete(movementId: String) = viewModelScope.launch {
        if (!movementId.startsWith("nomina_")) {
            repository.delete(movementId)
        }
    }
}

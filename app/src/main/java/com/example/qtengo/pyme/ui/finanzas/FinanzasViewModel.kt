/**
 * ViewModel responsable de la lógica financiera del perfil PYME.
 * 
 * Gestiona el flujo de caja combinando los registros manuales de movimientos
 * con las nóminas generadas automáticamente a partir de la lista de empleados.
 * Proporciona estados reactivos para los totales de ingresos, gastos y la lista unificada.
 */
package com.example.qtengo.pyme.ui.finanzas

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.data.repositories.FinanceRepository
import com.example.qtengo.core.data.repositories.EmployeeRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FinanzasViewModel(
    private val repository: FinanceRepository = FinanceRepository(),
    private val employeeRepository: EmployeeRepository = EmployeeRepository()
) : ViewModel() {

    /**
     * Lista unificada de movimientos financieros.
     * Combina en tiempo real:
     * 1. Movimientos manuales desde [FinanceRepository].
     * 2. Nóminas calculadas desde [EmployeeRepository].
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
    }.asLiveData(viewModelScope.coroutineContext)

    /**
     * Cálculo reactivo del sumatorio de todos los ingresos.
     */
    val totalIngresos: LiveData<Double?> = movements.map { list ->
        list.filter { it.type == "INGRESO" }.sumOf { it.amount }
    }

    /**
     * Cálculo reactivo del sumatorio de todos los gastos (incluyendo nóminas).
     */
    val totalGastos: LiveData<Double?> = movements.map { list ->
        list.filter { it.type == "GASTO" }.sumOf { it.amount }
    }

    /**
     * Inserta un nuevo movimiento (Ingreso o Gasto) en el repositorio.
     * @param movement El objeto con los datos del movimiento.
     */
    fun insertar(movement: FinanceMovement) = viewModelScope.launch {
        repository.insert(movement)
    }

    /**
     * Elimina un movimiento si no es una nómina autogenerada.
     * Las nóminas están protegidas para evitar inconsistencias con la lista de empleados.
     * @param movementId El identificador del documento a borrar.
     */
    fun eliminar(movementId: String) = viewModelScope.launch {
        if (!movementId.startsWith("nomina_")) {
            repository.delete(movementId)
        }
    }
}

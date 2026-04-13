package com.example.qtengo.pyme.ui.finanzas

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.data.repositories.FinanceRepository
import com.example.qtengo.core.data.repositories.EmployeeRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la gestión financiera del perfil PYME.
 * 
 * Centraliza la lógica de ingresos, gastos y la generación automática de
 * movimientos basados en la plantilla de empleados (nóminas).
 */
class FinanzasViewModel(
    private val repository: FinanceRepository = FinanceRepository(),
    private val employeeRepository: EmployeeRepository = EmployeeRepository()
) : ViewModel() {

    /**
     * Flujo unificado de movimientos financieros.
     * Combina los registros manuales de Firestore con las nóminas generadas dinámicamente
     * a partir de los salarios de los empleados activos.
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
     * Suma total de todos los movimientos marcados como 'INGRESO'.
     */
    val totalIngresos: LiveData<Double?> = movements.map { list ->
        list.filter { it.type == "INGRESO" }.sumOf { it.amount }
    }

    /**
     * Suma total de todos los movimientos marcados como 'GASTO', incluyendo nóminas.
     */
    val totalGastos: LiveData<Double?> = movements.map { list ->
        list.filter { it.type == "GASTO" }.sumOf { it.amount }
    }

    /**
     * Registra un nuevo movimiento financiero (Ingreso o Gasto).
     * @param movement Datos del movimiento a persistir.
     */
    fun insert(movement: FinanceMovement) = viewModelScope.launch {
        repository.insert(movement)
    }

    /**
     * Elimina un registro financiero por su ID.
     * Bloquea la eliminación de nóminas autogeneradas para mantener la integridad contable.
     * @param movementId Identificador único del documento en Firestore.
     */
    fun delete(movementId: String) = viewModelScope.launch {
        if (!movementId.startsWith("nomina_")) {
            repository.delete(movementId)
        }
    }
}

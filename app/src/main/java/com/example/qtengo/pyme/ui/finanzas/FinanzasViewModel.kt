package com.example.qtengo.pyme.ui.finanzas

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.data.repositories.FinanceRepository
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica financiera (ingresos y gastos) del módulo Pyme usando Firebase.
 */
class FinanzasViewModel(
    private val repository: FinanceRepository = FinanceRepository()
) : ViewModel() {

    val movements: LiveData<List<FinanceMovement>> = repository.getAllFlow("PYME").asLiveData()

    val totalIngresos: LiveData<Double?> = movements.map { list ->
        list.filter { it.type == "INGRESO" }.sumOf { it.amount }.takeIf { it > 0.0 }
    }

    val totalGastos: LiveData<Double?> = movements.map { list ->
        list.filter { it.type == "GASTO" }.sumOf { it.amount }.takeIf { it > 0.0 }
    }

    /**
     * Registra un nuevo movimiento financiero.
     */
    fun insert(movement: FinanceMovement) = viewModelScope.launch {
        repository.insert(movement)
    }

    /**
     * Elimina un movimiento financiero existente.
     */
    fun delete(movementId: String) = viewModelScope.launch {
        repository.delete(movementId)
    }
}

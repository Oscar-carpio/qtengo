package com.example.qtengo.pyme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.qtengo.core.data.database.AppDatabase
import com.example.qtengo.core.domain.models.FinanceMovement
import com.example.qtengo.core.data.repositories.FinanceRepository
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona la lógica financiera (ingresos y gastos) del módulo Pyme.
 */
class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    val movements: LiveData<List<FinanceMovement>>
    val totalIngresos: LiveData<Double?>
    val totalGastos: LiveData<Double?>

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
        movements = repository.getAll("PYME")
        totalIngresos = repository.getTotalIngresos("PYME")
        totalGastos = repository.getTotalGastos("PYME")
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
    fun delete(movement: FinanceMovement) = viewModelScope.launch {
        repository.delete(movement)
    }
}

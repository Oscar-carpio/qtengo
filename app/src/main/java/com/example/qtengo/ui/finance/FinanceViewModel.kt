package com.example.qtengo.ui.finance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.local.database.AppDatabase
import com.example.qtengo.data.local.model.FinanceMovement
import com.example.qtengo.data.repository.FinanceRepository
import kotlinx.coroutines.launch

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

    fun insert(movement: FinanceMovement) = viewModelScope.launch {
        repository.insert(movement)
    }

    fun delete(movement: FinanceMovement) = viewModelScope.launch {
        repository.delete(movement)
    }
}

package com.example.qtengo.ui.finance

import android.app.Application
import androidx.lifecycle.*
import com.example.qtengo.data.local.database.AppDatabase
import com.example.qtengo.data.local.model.FinanceMovement
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).financeDao()

    private val profile = MutableLiveData("PYME")

    val movements: LiveData<List<FinanceMovement>> = profile.switchMap {
        dao.getAll(it)
    }

    val totalIngresos: LiveData<Double> = profile.switchMap {
        dao.getTotalIngresos(it)
    }.map { it ?: 0.0 }

    val totalGastos: LiveData<Double> = profile.switchMap {
        dao.getTotalGastos(it)
    }.map { it ?: 0.0 }

    fun insert(movement: FinanceMovement) = viewModelScope.launch {
        dao.insert(movement)
    }

    fun delete(movement: FinanceMovement) = viewModelScope.launch {
        dao.delete(movement)
    }
}
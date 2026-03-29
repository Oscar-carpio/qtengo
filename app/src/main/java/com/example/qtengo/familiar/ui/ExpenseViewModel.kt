package com.example.qtengo.familiar.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.qtengo.core.data.database.AppDatabase
import com.example.qtengo.core.domain.models.Expense
import com.example.qtengo.core.data.repositories.ExpenseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para gestionar los gastos e ingresos de forma reactiva en el perfil Familiar.
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val _currentProfile = MutableLiveData<String>("FAMILIA")

    /**
     * Lista de gastos filtrada por el perfil actual.
     */
    val expenses: LiveData<List<Expense>> = _currentProfile.switchMap { profile ->
        repository.getByProfile(profile)
    }
    
    /**
     * Total de gastos acumulados.
     */
    val totalExpenses: LiveData<Double?> = _currentProfile.switchMap { profile ->
        repository.getTotalExpenses(profile)
    }

    init {
        val dao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(dao)
    }

    /**
     * Carga el perfil del usuario para filtrar los datos.
     */
    fun loadProfile(profile: String) {
        if (_currentProfile.value != profile) {
            _currentProfile.value = profile
        }
    }

    /**
     * Inserta un nuevo gasto en la base de datos.
     */
    fun insert(name: String, details: String, amount: Double, category: String, type: String = "GASTO") = viewModelScope.launch {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        
        val expense = Expense(
            name = name,
            details = details,
            amount = amount,
            category = category,
            date = currentDate,
            type = type,
            profile = _currentProfile.value ?: "FAMILIA"
        )
        repository.insert(expense)
    }

    /**
     * Elimina un registro de gasto.
     */
    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }
}

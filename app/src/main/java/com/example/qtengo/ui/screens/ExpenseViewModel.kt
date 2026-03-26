package com.example.qtengo.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.local.database.AppDatabase
import com.example.qtengo.data.local.model.Expense
import com.example.qtengo.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para gestionar los gastos e ingresos de forma reactiva.
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val _currentProfile = MutableLiveData<String>("FAMILIA")

    val expenses: LiveData<List<Expense>>
    val totalExpenses: LiveData<Double?>
    val totalIncomes: LiveData<Double?>

    init {
        val dao = AppDatabase.getDatabase(application).expenseDao()
        repository = ExpenseRepository(dao)

        expenses = _currentProfile.switchMap { profile ->
            repository.getByProfile(profile)
        }
        
        totalExpenses = _currentProfile.switchMap { profile ->
            repository.getTotalExpenses(profile)
        }
        
        totalIncomes = _currentProfile.switchMap { profile ->
            repository.getTotalIncomes(profile)
        }
    }

    fun loadProfile(profile: String) {
        if (_currentProfile.value != profile) {
            _currentProfile.value = profile
        }
    }

    /**
     * Inserta un nuevo movimiento con los nuevos campos de Nombre y Detalles.
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
     * Actualiza un movimiento existente.
     */
    fun update(expense: Expense) = viewModelScope.launch {
        repository.update(expense)
    }

    /**
     * Elimina un movimiento.
     */
    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }
}

package com.example.qtengo.core.data.repositories

import androidx.lifecycle.LiveData
import com.example.qtengo.core.data.database.dao.ExpenseDao
import com.example.qtengo.core.domain.models.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    fun getByProfile(profile: String): LiveData<List<Expense>> = expenseDao.getByProfile(profile)
    fun getTotalExpenses(profile: String): LiveData<Double?> = expenseDao.getTotalExpenses(profile)
    fun getTotalIncomes(profile: String): LiveData<Double?> = expenseDao.getTotalIncomes(profile)
    suspend fun insert(expense: Expense) = expenseDao.insert(expense)
    suspend fun update(expense: Expense) = expenseDao.update(expense)
    suspend fun delete(expense: Expense) = expenseDao.delete(expense)
}

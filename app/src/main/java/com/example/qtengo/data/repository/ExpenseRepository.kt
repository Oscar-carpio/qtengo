package com.example.qtengo.data.repository

import androidx.lifecycle.LiveData
import com.example.qtengo.data.dao.ExpenseDao
import com.example.qtengo.data.model.Expense

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getByProfile(profile: String): LiveData<List<Expense>> {
        return expenseDao.getByProfile(profile)
    }

    fun getTotalExpenses(profile: String): LiveData<Double?> {
        return expenseDao.getTotalExpenses(profile)
    }

    fun getTotalIncomes(profile: String): LiveData<Double?> {
        return expenseDao.getTotalIncomes(profile)
    }

    suspend fun insert(expense: Expense) {
        expenseDao.insert(expense)
    }

    suspend fun update(expense: Expense) {
        expenseDao.update(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }
}

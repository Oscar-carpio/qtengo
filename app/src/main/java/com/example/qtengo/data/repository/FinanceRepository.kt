package com.example.qtengo.data.repository

import androidx.lifecycle.LiveData
import com.example.qtengo.data.local.dao.FinanceDao
import com.example.qtengo.data.local.model.FinanceMovement

class FinanceRepository(private val financeDao: FinanceDao) {

    fun getAll(profile: String): LiveData<List<FinanceMovement>> {
        return financeDao.getAll(profile)
    }

    fun getByDate(date: String, profile: String): LiveData<List<FinanceMovement>> {
        return financeDao.getByDate(date, profile)
    }

    fun getTotalIngresos(profile: String): LiveData<Double?> {
        return financeDao.getTotalIngresos(profile)
    }

    fun getTotalGastos(profile: String): LiveData<Double?> {
        return financeDao.getTotalGastos(profile)
    }

    suspend fun insert(movement: FinanceMovement) {
        financeDao.insert(movement)
    }

    suspend fun delete(movement: FinanceMovement) {
        financeDao.delete(movement)
    }
}

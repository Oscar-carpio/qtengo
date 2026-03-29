package com.example.qtengo.core.data.repositories

import androidx.lifecycle.LiveData
import com.example.qtengo.core.data.database.dao.FinanceDao
import com.example.qtengo.core.domain.models.FinanceMovement

class FinanceRepository(private val financeDao: FinanceDao) {
    fun getAll(profile: String): LiveData<List<FinanceMovement>> = financeDao.getAll(profile)
    fun getByDate(date: String, profile: String): LiveData<List<FinanceMovement>> = financeDao.getByDate(date, profile)
    fun getTotalIngresos(profile: String): LiveData<Double?> = financeDao.getTotalIngresos(profile)
    fun getTotalGastos(profile: String): LiveData<Double?> = financeDao.getTotalGastos(profile)
    suspend fun insert(movement: FinanceMovement) = financeDao.insert(movement)
    suspend fun delete(movement: FinanceMovement) = financeDao.delete(movement)
}

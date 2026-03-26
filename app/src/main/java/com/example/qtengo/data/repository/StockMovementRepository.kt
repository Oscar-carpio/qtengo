package com.example.qtengo.data.repository

import androidx.lifecycle.LiveData
import com.example.qtengo.data.local.dao.StockMovementDao
import com.example.qtengo.data.local.model.StockMovement

class StockMovementRepository(private val stockMovementDao: StockMovementDao) {
    fun getMovementsByDate(date: String, profile: String): LiveData<List<StockMovement>> {
        return stockMovementDao.getMovementsByDate(date, profile)
    }

    suspend fun insert(movement: StockMovement) {
        stockMovementDao.insert(movement)
    }
}

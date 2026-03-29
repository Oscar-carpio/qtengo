package com.example.qtengo.core.data.repositories

import androidx.lifecycle.LiveData
import com.example.qtengo.core.data.database.dao.StockMovementDao
import com.example.qtengo.core.domain.models.StockMovement

class StockMovementRepository(private val stockMovementDao: StockMovementDao) {
    fun getMovementsByDate(date: String, profile: String): LiveData<List<StockMovement>> {
        return stockMovementDao.getMovementsByDate(date, profile)
    }

    suspend fun insert(movement: StockMovement) {
        stockMovementDao.insert(movement)
    }
}

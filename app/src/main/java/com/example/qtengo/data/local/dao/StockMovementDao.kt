package com.example.qtengo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.local.model.StockMovement

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements WHERE date = :date AND profile = :profile")
    fun getMovementsByDate(date: String, profile: String): LiveData<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: StockMovement)
}

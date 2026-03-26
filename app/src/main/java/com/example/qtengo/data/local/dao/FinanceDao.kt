package com.example.qtengo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.local.model.FinanceMovement

@Dao
interface FinanceDao {

    @Query("SELECT * FROM finance_movements WHERE profile = :profile ORDER BY date DESC")
    fun getAll(profile: String): LiveData<List<FinanceMovement>>

    @Query("SELECT * FROM finance_movements WHERE date = :date AND profile = :profile")
    fun getByDate(date: String, profile: String): LiveData<List<FinanceMovement>>

    @Query("SELECT SUM(amount) FROM finance_movements WHERE type = 'INGRESO' AND profile = :profile")
    fun getTotalIngresos(profile: String): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM finance_movements WHERE type = 'GASTO' AND profile = :profile")
    fun getTotalGastos(profile: String): LiveData<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: FinanceMovement)

    @Delete
    suspend fun delete(movement: FinanceMovement)
}
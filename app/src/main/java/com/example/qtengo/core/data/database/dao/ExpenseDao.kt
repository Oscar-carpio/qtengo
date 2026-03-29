package com.example.qtengo.core.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.core.domain.models.Expense

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE profile = :profile")
    fun getByProfile(profile: String): LiveData<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE profile = :profile AND type = 'GASTO'")
    fun getTotalExpenses(profile: String): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE profile = :profile AND type = 'INGRESO'")
    fun getTotalIncomes(profile: String): LiveData<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}
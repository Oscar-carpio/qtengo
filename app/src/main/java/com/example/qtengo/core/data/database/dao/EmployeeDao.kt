package com.example.qtengo.core.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.core.domain.models.Employee

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE profile = :profile")
    fun getByProfile(profile: String): LiveData<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: Employee)

    @Update
    suspend fun update(employee: Employee)

    @Delete
    suspend fun delete(employee: Employee)
}
package com.example.qtengo.core.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.core.domain.models.Supplier

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers WHERE profile = :profile")
    fun getByProfile(profile: String): LiveData<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: Supplier)

    @Update
    suspend fun update(supplier: Supplier)

    @Delete
    suspend fun delete(supplier: Supplier)
}
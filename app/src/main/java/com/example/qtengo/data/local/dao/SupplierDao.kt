package com.example.qtengo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.local.model.Supplier

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
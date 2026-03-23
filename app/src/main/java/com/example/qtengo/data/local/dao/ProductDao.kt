package com.example.qtengo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.local.model.Product

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE profile = :profile ORDER BY name ASC")
    fun getByProfile(profile: String): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE stock <= 5 AND profile = :profile")
    fun getLowStock(profile: String): LiveData<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE profile = :profile")
    fun countProducts(profile: String): LiveData<Int>

    @Query("SELECT * FROM products WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("DELETE FROM products WHERE profile = :profile")
    suspend fun deleteByProfile(profile: String)
}
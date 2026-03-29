package com.example.qtengo.core.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.core.domain.models.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE profile = :profile ORDER BY name ASC")
    fun getByProfile(profile: String): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE quantity <= minStock AND profile = :profile")
    fun getLowStock(profile: String): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :search || '%' AND profile = :profile")
    fun searchProducts(search: String, profile: String): LiveData<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("SELECT COUNT(*) FROM products WHERE profile = :profile")
    fun countProducts(profile: String): LiveData<Int>
}
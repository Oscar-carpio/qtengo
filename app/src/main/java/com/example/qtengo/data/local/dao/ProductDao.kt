package com.example.qtengo.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.local.model.Product

@Dao
interface ProductDao {

    // Obtener todos los productos de un perfil
    @Query("SELECT * FROM products WHERE profile = :profile ORDER BY name ASC")
    fun getByProfile(profile: String): LiveData<List<Product>>

    // Obtener productos con stock bajo
    @Query("SELECT * FROM products WHERE quantity <= minStock AND profile = :profile")
    fun getLowStock(profile: String): LiveData<List<Product>>

    // Buscar productos por nombre
    @Query("SELECT * FROM products WHERE name LIKE '%' || :search || '%' AND profile = :profile")
    fun searchProducts(search: String, profile: String): LiveData<List<Product>>

    // Añadir producto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    // Actualizar producto
    @Update
    suspend fun update(product: Product)

    // Eliminar producto
    @Delete
    suspend fun delete(product: Product)

    // Contar total de productos
    @Query("SELECT COUNT(*) FROM products WHERE profile = :profile")
    fun countProducts(profile: String): LiveData<Int>
}
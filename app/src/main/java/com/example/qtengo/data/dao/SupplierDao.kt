package com.example.qtengo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.qtengo.data.model.Supplier

/**
 * Interfaz para definir las operaciones de base de datos para los proveedores.
 */
@Dao
interface SupplierDao {

    // Obtener todos los proveedores de un perfil específico (Pyme o Hostelería)
    @Query("SELECT * FROM suppliers WHERE profile = :profile ORDER BY name ASC")
    fun getByProfile(profile: String): LiveData<List<Supplier>>

    // Insertar un nuevo proveedor (si ya existe, lo reemplaza)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: Supplier)

    // Actualizar los datos de un proveedor existente
    @Update
    suspend fun update(supplier: Supplier)

    // Eliminar un proveedor de la base de datos
    @Delete
    suspend fun delete(supplier: Supplier)
}

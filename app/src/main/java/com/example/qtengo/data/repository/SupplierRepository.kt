package com.example.qtengo.data.repository

import androidx.lifecycle.LiveData
import com.example.qtengo.data.model.Supplier

/**
 * Repositorio para gestionar la comunicación entre el ViewModel y el DAO de proveedores.
 */
class SupplierRepository(private val supplierDao: SupplierDao) {

    // Obtiene la lista de proveedores filtrada por perfil
    fun getByProfile(profile: String): LiveData<List<Supplier>> {
        return supplierDao.getByProfile(profile)
    }

    // Inserta un nuevo proveedor en la base de datos de forma asíncrona
    suspend fun insert(supplier: Supplier) {
        supplierDao.insert(supplier)
    }

    // Actualiza un proveedor existente
    suspend fun update(supplier: Supplier) {
        supplierDao.update(supplier)
    }

    // Elimina un proveedor
    suspend fun delete(supplier: Supplier) {
        supplierDao.delete(supplier)
    }
}

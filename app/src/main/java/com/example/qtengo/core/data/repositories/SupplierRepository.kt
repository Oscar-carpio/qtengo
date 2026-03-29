package com.example.qtengo.core.data.repositories

import androidx.lifecycle.LiveData
import com.example.qtengo.core.data.database.dao.SupplierDao
import com.example.qtengo.core.domain.models.Supplier

class SupplierRepository(private val supplierDao: SupplierDao) {
    fun getByProfile(profile: String): LiveData<List<Supplier>> = supplierDao.getByProfile(profile)
    suspend fun insert(supplier: Supplier) = supplierDao.insert(supplier)
    suspend fun update(supplier: Supplier) = supplierDao.update(supplier)
    suspend fun delete(supplier: Supplier) = supplierDao.delete(supplier)
}

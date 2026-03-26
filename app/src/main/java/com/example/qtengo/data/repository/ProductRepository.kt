package com.example.qtengo.data.repository

import androidx.lifecycle.LiveData
import com.example.qtengo.data.local.dao.ProductDao
import com.example.qtengo.data.local.model.Product

class ProductRepository(private val productDao: ProductDao) {

    // Obtener productos por perfil
    fun getByProfile(profile: String): LiveData<List<Product>> {
        return productDao.getByProfile(profile)
    }

    // Obtener productos con stock bajo
    fun getLowStock(profile: String): LiveData<List<Product>> {
        return productDao.getLowStock(profile)
    }

    // Buscar productos
    fun searchProducts(search: String, profile: String): LiveData<List<Product>> {
        return productDao.searchProducts(search, profile)
    }

    // Contar productos
    fun countProducts(profile: String): LiveData<Int> {
        return productDao.countProducts(profile)
    }

    // Añadir producto
    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    // Actualizar producto
    suspend fun update(product: Product) {
        productDao.update(product)
    }

    // Eliminar producto
    suspend fun delete(product: Product) {
        productDao.delete(product)
    }
}
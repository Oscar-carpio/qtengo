package com.example.qtengo.core.data.repositories

import androidx.lifecycle.LiveData
import com.example.qtengo.core.data.database.dao.ProductDao
import com.example.qtengo.core.domain.models.Product

class ProductRepository(private val productDao: ProductDao) {

    fun getByProfile(profile: String): LiveData<List<Product>> {
        return productDao.getByProfile(profile)
    }

    fun getLowStock(profile: String): LiveData<List<Product>> {
        return productDao.getLowStock(profile)
    }

    fun searchProducts(search: String, profile: String): LiveData<List<Product>> {
        return productDao.searchProducts(search, profile)
    }

    fun countProducts(profile: String): LiveData<Int> {
        return productDao.countProducts(profile)
    }

    suspend fun insert(product: Product) {
        productDao.insert(product)
    }

    suspend fun update(product: Product) {
        productDao.update(product)
    }

    suspend fun delete(product: Product) {
        productDao.delete(product)
    }
}
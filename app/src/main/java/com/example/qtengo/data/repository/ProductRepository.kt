package com.example.qtengo.data.repository

import com.example.qtengo.data.local.dao.ProductDao
import com.example.qtengo.data.local.model.Product
import com.example.qtengo.data.remote.RetrofitInstance

class ProductRepository(private val dao: ProductDao) {

    private val api = RetrofitInstance.api

    fun getByProfile(profile: String) = dao.getByProfile(profile)
    fun getLowStock(profile: String) = dao.getLowStock(profile)
    fun countProducts(profile: String) = dao.countProducts(profile)

    // ========================
    // 🔄 SYNC API → ROOM
    // ========================
    suspend fun syncProducts(profile: String) {
        try {
            val remoteProducts = api.getProducts(profile)
            dao.deleteByProfile(profile)
            dao.insertAll(remoteProducts)

            // Después sincronizamos pendientes
            syncPending()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ========================
    // 🔄 SYNC PENDIENTES
    // ========================
    private suspend fun syncPending() {
        val pending = dao.getPendingSync()

        for (product in pending) {
            try {
                if (product.id == 0) {
                    api.insertProduct(product)
                } else {
                    api.updateProduct(product.id, product)
                }

                dao.update(product.copy(pendingSync = false))

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ========================
    // ➕ INSERT
    // ========================
    suspend fun insert(product: Product) {
        dao.insert(product.copy(pendingSync = true))

        try {
            api.insertProduct(product)
        } catch (e: Exception) {
            // se sincronizará luego
        }
    }

    // ========================
    // ✏️ UPDATE
    // ========================
    suspend fun update(product: Product) {
        dao.update(product.copy(pendingSync = true))

        try {
            api.updateProduct(product.id, product)
        } catch (e: Exception) {
        }
    }

    // ========================
    // ❌ DELETE
    // ========================
    suspend fun delete(product: Product) {
        dao.delete(product)

        try {
            api.deleteProduct(product.id)
        } catch (e: Exception) {
        }
    }
}
package com.example.qtengo.ui.products

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.qtengo.data.database.AppDatabase
import com.example.qtengo.data.model.Product
import com.example.qtengo.data.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    private var currentProfile: String = "FAMILIA"

    lateinit var products: LiveData<List<Product>>
    lateinit var lowStockProducts: LiveData<List<Product>>
    lateinit var productCount: LiveData<Int>

    init {
        val dao = AppDatabase.getDatabase(application).productDao()
        repository = ProductRepository(dao)
        loadProfile("FAMILIA")
    }

    // Cargar datos según el perfil activo
    fun loadProfile(profile: String) {
        currentProfile = profile
        products = repository.getByProfile(profile)
        lowStockProducts = repository.getLowStock(profile)
        productCount = repository.countProducts(profile)
    }

    // Añadir producto
    fun insert(product: Product) = viewModelScope.launch {
        repository.insert(product)
    }

    // Actualizar producto
    fun update(product: Product) = viewModelScope.launch {
        repository.update(product)
    }

    // Eliminar producto
    fun delete(product: Product) = viewModelScope.launch {
        repository.delete(product)
    }

    // Buscar productos
    fun search(query: String): LiveData<List<Product>> {
        return repository.searchProducts(query, currentProfile)
    }

    // Obtener perfil actual
    fun getCurrentProfile(): String = currentProfile
}

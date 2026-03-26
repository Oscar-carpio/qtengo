package com.example.qtengo.ui.products

import android.app.Application
import androidx.lifecycle.*
import com.example.qtengo.data.local.database.AppDatabase
import com.example.qtengo.data.local.model.Product
import androidx.lifecycle.*
import com.example.qtengo.data.database.AppDatabase
import com.example.qtengo.data.model.Product
import com.example.qtengo.data.repository.ProductRepository
import kotlinx.coroutines.launch
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    private val currentProfile = MutableLiveData<String>()

    val products: LiveData<List<Product>> = currentProfile.switchMap { profile ->
        repository.getByProfile(profile)
    }

    val lowStockProducts: LiveData<List<Product>> = currentProfile.switchMap { profile ->
        repository.getLowStock(profile)
    }

    val productCount: LiveData<Int> = currentProfile.switchMap { profile ->
        repository.countProducts(profile)
    }

    init {
        // 1. Inicializamos el repositorio primero
        val dao = AppDatabase.getDatabase(application).productDao()
        repository = ProductRepository(dao)
        currentProfile.value = "FAMILIA"
    }

    /**
     * Cambia el perfil y dispara la recarga automática de la lista desde la BD.
     */
    fun loadProfile(profile: String) {
        currentProfile.value = profile

        viewModelScope.launch {
            repository.syncProducts(profile)
        }
    }

    fun insert(product: Product) = viewModelScope.launch { repository.insert(product) }
    fun update(product: Product) = viewModelScope.launch { repository.update(product) }
    fun delete(product: Product) = viewModelScope.launch { repository.delete(product) }
}

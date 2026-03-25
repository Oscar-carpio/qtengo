package com.example.qtengo.ui.products

import android.app.Application
import androidx.lifecycle.*
import com.example.qtengo.data.database.AppDatabase
import com.example.qtengo.data.model.Product
import com.example.qtengo.data.repository.ProductRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar productos con persistencia real.
 * Corregido el orden de inicialización para evitar cierres (NPE).
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductRepository
    private val _currentProfile = MutableLiveData<String>("FAMILIA")

    // LiveData reactivas. Se inicializan en el bloque init después del repositorio.
    val products: LiveData<List<Product>>
    val lowStockProducts: LiveData<List<Product>>

    init {
        // 1. Inicializamos el repositorio primero
        val dao = AppDatabase.getDatabase(application).productDao()
        repository = ProductRepository(dao)

        // 2. Vinculamos las LiveData al perfil usando switchMap
        products = _currentProfile.switchMap { profile ->
            repository.getByProfile(profile)
        }
        
        lowStockProducts = _currentProfile.switchMap { profile ->
            repository.getLowStock(profile)
        }
    }

    /**
     * Cambia el perfil y dispara la recarga automática de la lista desde la BD.
     */
    fun loadProfile(profile: String) {
        if (_currentProfile.value != profile) {
            _currentProfile.value = profile
        }
    }

    /**
     * Inserta un producto.
     */
    fun insert(product: Product) = viewModelScope.launch {
        val productWithProfile = product.copy(profile = _currentProfile.value ?: "FAMILIA")
        repository.insert(productWithProfile)
    }

    fun update(product: Product) = viewModelScope.launch {
        repository.update(product)
    }

    fun delete(product: Product) = viewModelScope.launch {
        repository.delete(product)
    }

    fun search(query: String): LiveData<List<Product>> {
        return repository.searchProducts(query, _currentProfile.value ?: "FAMILIA")
    }
}

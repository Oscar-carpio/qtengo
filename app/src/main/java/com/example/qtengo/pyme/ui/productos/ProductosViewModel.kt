package com.example.qtengo.pyme.ui.productos

import androidx.lifecycle.*
import com.example.qtengo.core.domain.models.Product
import com.example.qtengo.core.domain.models.StockMovement
import com.example.qtengo.core.data.repositories.ProductRepository
import com.example.qtengo.core.data.repositories.StockMovementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel encargado de la lógica de negocio para la gestión de productos y stock en el módulo Pyme usando Firebase.
 */
class ProductosViewModel : ViewModel() {

    private val repository = ProductRepository()
    private val stockMovementRepository = StockMovementRepository()
    private val _currentProfile = MutableStateFlow("FAMILIA")

    /**
     * Lista reactiva de todos los productos del perfil actual (Tiempo real desde Firebase).
     */
    val products: LiveData<List<Product>> = _currentProfile.flatMapLatest { profile ->
        repository.getByProfileFlow(profile)
    }.asLiveData()

    /**
     * Lista reactiva de productos con stock por debajo del mínimo.
     */
    val lowStockProducts: LiveData<List<Product>> = products.map { list ->
        list.filter { it.quantity <= it.minStock }
    }

    /**
     * Contador total de productos registrados.
     */
    val productCount: LiveData<Int> = products.map { it.size }

    /**
     * Establece el perfil activo para filtrar los productos en la interfaz.
     */
    fun loadProfile(profile: String) {
        _currentProfile.value = profile
    }

    /**
     * Inserta un nuevo producto en Firebase y registra el movimiento inicial.
     */
    fun insert(product: Product) = viewModelScope.launch {
        repository.insert(product)
        recordMovement(product, product.quantity, product.quantity)
    }

    /**
     * Actualiza la información general de un producto.
     */
    fun update(product: Product) = viewModelScope.launch {
        repository.update(product, product.id)
    }

    /**
     * Actualiza la cantidad de stock y genera un registro de movimiento.
     */
    fun updateQuantity(product: Product, newQuantity: Double) = viewModelScope.launch {
        val diff = newQuantity - product.quantity
        if (diff != 0.0) {
            val updatedProduct = product.copy(quantity = newQuantity)
            repository.update(updatedProduct, product.id)
            recordMovement(updatedProduct, diff, newQuantity)
        }
    }

    private suspend fun recordMovement(product: Product, diff: Double, total: Double) {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val movement = StockMovement(
            productId = product.id.hashCode(),
            productName = product.name,
            quantityChanged = diff,
            newQuantity = total,
            date = date,
            profile = product.profile
        )
        stockMovementRepository.insert(movement)
    }

    /**
     * Elimina permanentemente un producto de Firebase.
     */
    fun delete(product: Product) = viewModelScope.launch { 
        repository.delete(product.id)
    }
}
